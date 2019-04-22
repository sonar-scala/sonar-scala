/*
 * Copyright (C) 2018-2019  All sonar-scala contributors
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

import cats.NonEmptyParallel
import cats.data.NonEmptyList
import cats.effect.IO._
import cats.effect.{ContextShift, IO, Sync}
import cats.instances.int._
import cats.instances.list._
import cats.instances.string._
import cats.syntax.either._
import cats.syntax.eq._
import cats.syntax.flatMap._
import cats.syntax.functor._
import cats.syntax.parallel._
import cats.syntax.traverse._
import com.mwz.sonar.scala.pr.GithubPrReviewJob._
import com.mwz.sonar.scala.pr.github._
import com.mwz.sonar.scala.util.Log
import org.http4s.Uri
import org.http4s.client.blaze.BlazeClientBuilder
import org.sonar.api.batch.fs.InputFile
import org.sonar.api.batch.postjob.{PostJob, PostJobContext, PostJobDescriptor}
import org.sonar.api.batch.rule.Severity

import scala.concurrent.ExecutionContext
import scala.language.higherKinds

final class GithubPrReviewJob(
  globalConfig: GlobalConfig,
  globalIssues: GlobalIssues
) extends PostJob {
  // TODO: Pure logging.
  private[this] val logger: Log = Log(classOf[Scala], "github-pr-decorator")
  private implicit val cs: ContextShift[IO] = IO.contextShift(ExecutionContext.global)

  def describe(descriptor: PostJobDescriptor): Unit =
    descriptor.name("Github PR decorator")

  def execute(context: PostJobContext): Unit = {
    BlazeClientBuilder[IO](ExecutionContext.global).resource
      .use { client =>
        for {
          // TODO: Nicer logging syntax.
          _ <- IO.delay(logger.debug(s"Global issues (${globalIssues.allIssues.size}):"))
          _ <- IO.delay(logger.debug(globalIssues.allIssues.mkString(", ")))
          _ <- globalConfig.pullRequest
            .map { config =>
              IO.fromEither(globalConfig.baseUrl)
                .flatMap(baseUrl => run(baseUrl, Github(client, config)))
            }
            .getOrElse(IO.unit)
        } yield ()
      }
      .unsafeRunSync()
  }

  private[pr] def run[F[_]: Sync, M[_]](baseUrl: Uri, github: Github[F])(
    implicit nep: NonEmptyParallel[F, M]
  ): F[Unit] = {
    for {
      // Get the authenticated user (to check the oauth token).
      user <- github.authenticatedUser
      // TODO: Check user auth scope to make sure they have repo push access and fail early if they don't.
      // Fetch the PR (to verify whether it exists).
      pr <- github.pullRequest
      // Create a pending PR status for the review.
      _ <- github.createStatus(
        pr.head.sha,
        NewStatus("pending", "", "SonarQube is reviewing this pull request.", GithubContext)
      )
      // Run the PR review.
      prStatus <- Sync[F].handleError(
        review(baseUrl, github, user, pr).map {
          case status if status.blocker > 0 || status.critical > 0 =>
            Error(status)
          case _ =>
            Success
        }
      )(Failure)
      // Create a new PR status.
      _ <- github.createStatus(pr.head.sha, githubStatus(prStatus))
    } yield ()
  }

  // TODO: Split this up a little bit more.
  private[pr] def review[F[_]: Sync, M[_]](
    baseUrl: Uri,
    github: Github[F],
    user: User,
    pr: PullRequest
  )(implicit nep: NonEmptyParallel[F, M]): F[ReviewStatus] =
    for {
      // Fetch existing PR comments and get PR files along with their patches.
      (allComments, files) <- (github.comments, github.files).parMapN((_, _))
      // Filter comments made by the authed user.
      allUserComments = allComments.filter(_.user.login === user.login).groupBy(_.path)
      _ <- Sync[F].delay(
        logger.debug(
          s"PR: $pr\n" +
          s"Comments: ${allUserComments.mkString(", ")}\n" +
          s"Files: ${files.mkString(", ")}"
        )
      )
      // Group patches by file names - `filename` is the full path relative to the root
      // of the project, so it should be unique. Raise an error when no files are present.
      prPatches <- Sync[F].fromEither(Either.fromOption(NonEmptyList.fromList(files), NoFilesInPR))
      allPatches = prPatches.groupByNem(_.filename).map(_.head).toSortedMap
      // Filter out issues which aren't related to any files in the PR.
      issues = globalIssues.allIssues.filterKeys(f => allPatches.keySet.contains(f.toString))
      // Filter out patches without any issues.
      patches = allPatches.filterKeys(f => issues.keySet.exists(_.toString === f))
      // Map file lines to patch lines.
      mappedPatches = patches.mapValues(file => Patch.parse(file.patch))
      // TODO: Log any patch parsing failures.
      issuesWithComments = allCommentsForIssues(issues, mappedPatches, allUserComments)
      // TODO: Delete comments on lines which are no longer flagged as issues.
      //  Not that important as Github now indicates when comments are outdated.
      // Post new comments.
      _ <- commentsForNewIssues(baseUrl, pr.head.sha, issuesWithComments)
        .traverse(github.createComment)
    } yield reviewStatus(issues)
}

object GithubPrReviewJob {
  final val GithubContext: String = "sonar-scala/review"

  // Lookup existing comments for all the issues.
  // Issues are linked to file lines, comments are linked to patch lines.
  // TODO: This is quite grim.
  def allCommentsForIssues(
    issues: Map[InputFile, List[Issue]],
    mappedPatches: Map[String, Either[PatchError, Map[FileLine, PatchLine]]],
    allUserComments: Map[String, List[Comment]]
  ): Map[InputFile, Map[PatchLine, (Issue, List[Comment])]] =
    issues.collect {
      case (f, issues) =>
        val issuesWithComments: Map[PatchLine, (Issue, List[Comment])] =
          issues.flatMap { issue =>
            // patchLine -> issue
            mappedPatches
              .get(f.toString)
              .flatMap(_.toOption.flatMap { m =>
                m.get(FileLine(issue.line)).map { patchLine =>
                  // patchLine -> comments
                  // Filter comments by the line number.
                  // Those are filtered further later on based on the body.
                  val comments: List[Comment] =
                    allUserComments
                      .get(f.toString)
                      .map(_.filter(_.position === patchLine.value))
                      .getOrElse(List.empty)
                  (patchLine, (issue, comments))
                }
              })
          }.toMap
        (f, issuesWithComments)
    }

  // Comments for new issues (which don't already have a comment).
  // TODO: Should we bundle multiple issues into a single comment?
  def commentsForNewIssues(
    baseUrl: Uri,
    commitId: String,
    issuesWithComments: Map[InputFile, Map[PatchLine, (Issue, List[Comment])]]
  ): List[NewComment] =
    issuesWithComments.flatMap {
      case (f, issuesAndComments) =>
        issuesAndComments.flatMap {
          case (patchLine, (issue, comments)) =>
            // Generate body for each issue to compare to the existing comments.
            val markdown: Markdown = Markdown.inline(baseUrl, issue)
            comments
              .find(comment => comment.body === markdown.text)
              .fold(Option(NewComment(markdown.text, commitId, f.toString, patchLine.value)))(_ => None)
        }
    }.toList

  def reviewStatus(issues: Map[InputFile, List[Issue]]): ReviewStatus = {
    issues.values.flatten
      .groupBy(i => i.severity)
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

  def statusState(prStatus: PrStatus): String = {
    prStatus match {
      case Pending    => "pending"
      case Success    => "success"
      case Error(_)   => "error"
      case Failure(_) => "failure"
    }
  }

  def statusDescription(prStatus: PrStatus): String = {
    prStatus match {
      case Pending       => "SonarQube is reviewing this pull request."
      case Success       => "SonarQube didn't report any critical or blocker issues."
      case Error(status) => s"SonarQube reported ${ReviewStatus.description(status)}."
      case Failure(_)    => "An error occurred during SonarQube review."
    }
  }

  def githubStatus(prStatus: PrStatus): NewStatus =
    NewStatus(statusState(prStatus), "", statusDescription(prStatus), GithubContext)
}
