/*
 * Copyright (C) 2018-2020  All sonar-scala contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU General Lesser Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.mwz.sonar.scala
package pr

import scala.concurrent.ExecutionContext

import cats.NonEmptyParallel
import cats.data.NonEmptyList
import cats.effect.{ContextShift, IO, Sync}
import cats.instances.list._
import cats.instances.string._
import cats.syntax.either._
import cats.syntax.eq._
import cats.syntax.flatMap._
import cats.syntax.functor._
import cats.syntax.parallel._
import cats.syntax.traverse._
import com.mwz.sonar.scala.pr.GithubPrReviewJob._
import com.mwz.sonar.scala.pr.github.{Github, _}
import com.mwz.sonar.scala.util.Logger
import mouse.boolean._
import org.http4s.Uri
import org.http4s.client.blaze.BlazeClientBuilder
import org.sonar.api.batch.fs.InputFile
import org.sonar.api.batch.postjob.{PostJob, PostJobContext, PostJobDescriptor}
import org.sonar.api.batch.rule.Severity

final class GithubPrReviewJob(
  globalConfig: GlobalConfig,
  globalIssues: GlobalIssues
) extends PostJob {
  def describe(descriptor: PostJobDescriptor): Unit =
    descriptor.name("Github PR review job")

  def execute(context: PostJobContext): Unit = {
    import cats.effect.IO._

    implicit val cs: ContextShift[IO] =
      IO.contextShift(ExecutionContext.global)

    BlazeClientBuilder[IO](ExecutionContext.global).resource
      .use { client =>
        for {
          implicit0(log: Logger[IO]) <- Logger.create(classOf[GithubPrReviewJob], "github-pr-review")
          _ <-
            globalConfig.pullRequest.value
              .map { config =>
                for {
                  config <- IO.fromEither(config)
                  _ <- log.debug(
                    s"Found ${globalIssues.allIssues.size} issues:\n" +
                    globalIssues.allIssues.mkString(", ")
                  )
                  baseUrl <- IO.fromEither(globalConfig.baseUrl)
                  _       <- run(baseUrl, Github(client, config))
                } yield ()
              }
              .getOrElse(IO.unit)
        } yield ()
      }
      .unsafeRunSync()
  }

  private[pr] def run[F[_]: Sync: NonEmptyParallel: Logger](
    baseUrl: Uri,
    github: Github[F]
  ): F[PrReviewStatus] = {
    for {
      // Get the authenticated user (to check the oauth token).
      user <- github.authenticatedUser
      // TODO: Check user auth scopes to make sure the account has access to the repo,
      // i.e. (`public_repo` for public repositories or `repo` for private repositories).
      // Fetch the PR (to verify whether it exists).
      pr <- github.pullRequest
      // Create a pending PR status for the review.
      _ <- github.createStatus(
        pr.head.sha,
        NewStatus("pending", "", "SonarQube is reviewing this pull request.", GithubContext)
      )
      // Run PR review.
      prStatus <- Sync[F].handleError(
        review(baseUrl, github, user, pr).map {
          case status if status.blocker > 0 || status.critical > 0 =>
            Error(status)
          case _ =>
            Success
        }
      )(Failure)
      // Create a new PR status.
      _ <- Logger[F].debug(s"Set the PR status to $prStatus.")
      _ <- github.createStatus(pr.head.sha, githubStatus(prStatus))
      // Convert the "Failure" pr status to an error.
      status <- prStatus match {
        case Failure(error) => Sync[F].raiseError(error)
        case other          => Sync[F].pure(other)
      }
    } yield (status)
  }

  private[pr] def review[F[_]: Sync: NonEmptyParallel: Logger](
    baseUrl: Uri,
    github: Github[F],
    user: User,
    pr: PullRequest
  ): F[ReviewStatus] =
    for {
      // Fetch existing PR comments and get PR files along with their patches.
      (allComments, files) <- (github.comments, github.files).parMapN((_, _))
      // Group patches by file names - `filename` is the full path relative to the root
      // of the project, so it should be unique. Raise an error when no files are present.
      prPatches <- Sync[F].fromEither(Either.fromOption(NonEmptyList.fromList(files), NoFilesInPR))
      allPatches = prPatches.groupByNem(_.filename).map(_.head).toSortedMap
      // Filter out issues which aren't related to any files in the PR.
      issues = globalIssues.allIssues.view.filterKeys(f => allPatches.keySet.contains(f.toString)).toMap
      // Get new comments and post them.
      commentsToPost <- newComments(baseUrl, user, pr, allComments, files, allPatches, issues)
      _ <- commentsToPost.nonEmpty.fold(
        Logger[F].info("Posting new comments to Github."),
        Logger[F].info("No new Github comments to post.")
      )
      _ <-
        commentsToPost
          .sortBy(c => (c.path, c.position))
          .traverse { comment =>
            Logger[F].debug(
              s"Posting a new comment for ${comment.path}:${comment.position} - ${comment.body}"
            ) >>
            github.createComment(comment)
          }
    } yield reviewStatus(issues)

  private[pr] def newComments[F[_]: Sync: Logger](
    baseUrl: Uri,
    user: User,
    pr: PullRequest,
    comments: List[Comment],
    files: List[File],
    patches: Map[String, File],
    issues: Map[InputFile, List[Issue]]
  ): F[List[NewComment]] =
    for {
      // Filter comments made by the authed user.
      sonarComments <- Sync[F].pure(comments.filter(_.user.login === user.login).groupBy(_.path))
      _ <- Logger[F].debug(
        s"PR: $pr\n" +
        s"Sonar comments: ${sonarComments.mkString(", ")}\n" +
        s"Files: ${files.mkString(", ")}"
      )
      // Filter out patches without any issues.
      patchesWithIssues = patches.view.filterKeys(f => issues.keySet.exists(_.toString === f))
      // Map file lines to patch lines.
      mappedPatches = patchesWithIssues.view.mapValues(file => Patch.parse(file.patch)).toMap
      _ <-
        mappedPatches
          .collect { case (file, Left(error)) => (file, error) }
          .toList
          .traverse {
            case (file, error) =>
              Logger[F].error(s"Error parsing patch for $file.") >>
              Logger[F].debug(s"""Invalid patch format: "${error.text}".""")
          }
      issuesWithComments = allCommentsForIssues(issues, mappedPatches, sonarComments)
      commentsToPost = commentsForNewIssues(baseUrl, pr.head.sha, issuesWithComments)
    } yield commentsToPost
}

object GithubPrReviewJob {
  final val GithubContext: String = "sonar-scala/review"

  // Lookup existing comments for all the issues.
  // Issues are linked to file lines, comments are linked to patch lines.
  // This gives us a mapping from a patch line to a list of issues and related comments (for each file).
  def allCommentsForIssues(
    issues: Map[InputFile, List[Issue]],
    mappedPatchLines: Map[String, Either[PatchError, Map[FileLine, PatchLine]]],
    allUserComments: Map[String, List[Comment]]
  ): Map[InputFile, Map[PatchLine, List[(Issue, List[Comment])]]] = {
    issues
      .collect {
        case (file, issues) =>
          val issuesWithComments: Map[PatchLine, List[(Issue, List[Comment])]] =
            issues
              .flatMap { issue =>
                // patchLine -> issue
                mappedPatchLines
                  .get(file.toString)
                  .flatMap(_.toOption.flatMap {
                    mapping =>
                      mapping
                        .get(FileLine(issue.line))
                        .map { patchLine =>
                          // patchLine -> comments
                          // Filter comments by the line number.
                          // Those are filtered again later on based on the body text.
                          val comments: List[Comment] =
                            allUserComments
                              .get(file.toString)
                              // Outdated comments are filtered out here
                              // as they don't have a current position.
                              .map(_.filter(_.position.contains(patchLine.value)))
                              .getOrElse(List.empty)
                          (patchLine, List((issue, comments)))
                        }
                  })
              }
              .groupBy { case (patchLine, _) => patchLine }
              .view
              .mapValues(_.flatMap { case (_, issuesAndComments) => issuesAndComments })
              .toMap
          (file, issuesWithComments)
      }
  }

  // Comments for new issues (which don't already have a comment).
  def commentsForNewIssues(
    baseUrl: Uri,
    commitId: String,
    issuesWithComments: Map[InputFile, Map[PatchLine, List[(Issue, List[Comment])]]]
  ): List[NewComment] =
    (for {
      (file, issuesAndComments)      <- issuesWithComments
      (patchLine, issuesAndComments) <- issuesAndComments
      (issue, comments)              <- issuesAndComments
    } yield {
      val markdown: Markdown = Markdown.inline(baseUrl, issue)
      // TODO: It would be good to support text evolution in the future.
      comments
        .find(comment => comment.body === markdown.text)
        .fold(Option(NewComment(markdown.text, commitId, file.toString, patchLine.value)))(_ => None)
    }).toList.flatten

  def reviewStatus(issues: Map[InputFile, List[Issue]]): ReviewStatus = {
    issues.values.flatten
      .groupBy(i => i.severity)
      .view
      .mapValues(_.size)
      .foldLeft(ReviewStatus(0, 0)) {
        case (s, (severity, count)) =>
          severity match {
            case Severity.BLOCKER  => s.copy(blocker = s.blocker + count)
            case Severity.CRITICAL => s.copy(critical = s.critical + count)
            case _                 => s
          }
      }
  }

  def statusState(prStatus: PrReviewStatus): String = {
    prStatus match {
      case Pending    => "pending"
      case Success    => "success"
      case Error(_)   => "error"
      case Failure(_) => "failure"
    }
  }

  def statusDescription(prStatus: PrReviewStatus): String = {
    prStatus match {
      case Pending       => "SonarQube is reviewing this pull request."
      case Success       => "SonarQube didn't report any critical or blocker issues."
      case Error(status) => s"SonarQube reported ${ReviewStatus.description(status)}."
      case Failure(_)    => "An error occurred during SonarQube review."
    }
  }

  def githubStatus(prStatus: PrReviewStatus): NewStatus =
    NewStatus(statusState(prStatus), "", statusDescription(prStatus), GithubContext)
}
