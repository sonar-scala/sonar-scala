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

package com.mwz.sonar.scala.pr

import scala.concurrent.ExecutionContext

import cats.data.NonEmptyList
import cats.effect.ContextShift
import cats.effect.IO
import cats.syntax.flatMap._
import com.mwz.sonar.scala.EmptyLogger
import com.mwz.sonar.scala.GlobalConfig
import com.mwz.sonar.scala.WithLogging
import com.mwz.sonar.scala.WithTracing
import com.mwz.sonar.scala.pr.Generators._
import com.mwz.sonar.scala.pr.github.File
import com.mwz.sonar.scala.pr.github.Github
import com.mwz.sonar.scala.pr.github.PullRequest
import com.mwz.sonar.scala.pr.github.{Comment, NewComment, NewStatus, User}
import com.mwz.sonar.scala.util.Logger
import org.http4s
import org.http4s.Uri
import org.http4s.client.UnexpectedStatus
import org.scalacheck.ScalacheckShapeless._
import org.scalatest.LoneElement
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks
import org.sonar.api.batch.fs.InputFile
import org.sonar.api.batch.fs.internal.TestInputFileBuilder
import org.sonar.api.batch.postjob.internal.DefaultPostJobDescriptor
import org.sonar.api.batch.rule.Severity
import org.sonar.api.config.internal.MapSettings
import org.sonar.api.rule.RuleKey

class GithubPrReviewJobSpec
    extends AnyFlatSpec
    with Matchers
    with LoneElement
    with ScalaCheckDrivenPropertyChecks
    with WithTracing
    with WithLogging {

  trait Ctx {
    val globalConfig = new GlobalConfig(new MapSettings().asConfig)
    val globalIssues = new GlobalIssues
    def githubPrReviewJob(
      globalConfig: GlobalConfig = globalConfig,
      globalIssues: GlobalIssues = globalIssues
    ) = new GithubPrReviewJob(globalConfig, globalIssues)
  }

  trait IOCtx {
    implicit val ec: ExecutionContext = ExecutionContext.global
    implicit val cs: ContextShift[IO] = IO.contextShift(ec)
  }

  trait GithubNotImpl[F[_]] extends Github[F] {
    def authenticatedUser: F[User] = ???
    def pullRequest: F[PullRequest] = ???
    def comments: F[List[Comment]] = ???
    def createComment(comment: NewComment): F[Unit] = ???
    def files: F[List[File]] = ???
    def createStatus(sha: String, status: NewStatus): F[Unit] = ???
  }

  val patch =
    """@@ -2,7 +2,7 @@ scalaVersion := \"2.12.8\"\n libraryDependencies ++= Seq(\n   \"org.sonarsource.update-center\" % \"sonar-update-center-common\" % \"1.23.0.723\",\n   // Scapegoat & scalastyle inspections generator dependencies\n-  \"com.sksamuel.scapegoat\" %% \"scalac-scapegoat-plugin\" % \"1.3.10\",\n+  \"com.sksamuel.scapegoat\" %% \"scalac-scapegoat-plugin\" % \"1.3.11\",\n   \"org.scalastyle\"         %% \"scalastyle\"              % \"1.0.0\",\n   \"org.scalameta\"          %% \"scalameta\"               % \"4.2.3\",\n   \"org.scalatest\"          %% \"scalatest\"               % \"3.0.8\" % Test"""

  it should "correctly set the descriptor" in {
    val conf = new GlobalConfig(new MapSettings().asConfig())
    val job = new GithubPrReviewJob(conf, new GlobalIssues)
    val descriptor = new DefaultPostJobDescriptor
    job.describe(descriptor)

    descriptor.name shouldBe "Github PR review job"
  }

  it should "fail for invalid github token" in new Ctx with IOCtx with EmptyLogger {
    forAll { baseUrl: Uri =>
      withTracing { trace =>
        val github = new GithubNotImpl[IO] {
          override def authenticatedUser =
            trace.update("authenticatedUser" :: _) >>
            IO.raiseError(UnexpectedStatus(http4s.Status.Unauthorized))
        }

        val result = githubPrReviewJob().run[IO](baseUrl, github)

        result.attempt.unsafeRunSync() shouldBe Left(UnexpectedStatus(http4s.Status.Unauthorized))
        trace.get.unsafeRunSync().loneElement shouldBe "authenticatedUser"
      }
    }
  }

  it should "report a failure status if review returns an error" in new Ctx with IOCtx with EmptyLogger {
    forAll { (baseUrl: Uri, user: User, pr: PullRequest) =>
      withTracing { trace =>
        val github = new GithubNotImpl[IO] {
          override def authenticatedUser = trace.update("authenticatedUser" :: _) >> IO.pure(user)
          override def pullRequest = trace.update("pullRequest" :: _) >> IO.pure(pr)
          override def comments = trace.update("comments" :: _) >> IO.pure(List.empty)
          override def files = trace.update("files" :: _) >> IO.pure(List.empty)
          override def createStatus(sha: String, newStatus: NewStatus) =
            trace.update("createStatus - " + newStatus.state :: _) >> IO.unit
        }

        val result = githubPrReviewJob().run[IO](baseUrl, github)

        result.attempt.unsafeRunSync() shouldBe Left(NoFilesInPR)
        trace.get.unsafeRunSync() should contain theSameElementsAs List(
          "authenticatedUser",
          "pullRequest",
          "comments",
          "files",
          "createStatus - pending",
          "createStatus - failure"
        )
      }
    }
  }

  it should "report an error status if review contains any blocker or critical issues" in
  new Ctx with IOCtx with EmptyLogger {
    forAll { (baseUrl: Uri, user: User, pr: PullRequest, prFile: File) =>
      withTracing { trace =>
        val fileWithPatch = prFile.copy(patch = patch)
        val github = new GithubNotImpl[IO] {
          override def authenticatedUser = trace.update("authenticatedUser" :: _) >> IO.pure(user)
          override def pullRequest = trace.update("pullRequest" :: _) >> IO.pure(pr)
          override def comments = trace.update("comments" :: _) >> IO.pure(List.empty)
          override def createComment(comment: NewComment) =
            trace.update("createComment" :: _) >>
            IO.pure(Comment(1, comment.path, Some(comment.position), user, comment.body))
          override def files = trace.update("files" :: _) >> IO.pure(List(fileWithPatch))
          override def createStatus(sha: String, newStatus: NewStatus) =
            trace.update("createStatus - " + newStatus.state :: _) >> IO.unit
        }

        val file: InputFile = TestInputFileBuilder.create("", fileWithPatch.filename).build()
        val issue = Issue(RuleKey.of("repo", "rule"), file, 5, Severity.BLOCKER, "msg")

        val issues = new GlobalIssues
        issues.add(issue)
        val result = githubPrReviewJob(globalIssues = issues).run[IO](baseUrl, github)

        result.attempt.unsafeRunSync() shouldBe Right(Error(ReviewStatus(blocker = 1, critical = 0)))
        trace.get.unsafeRunSync() should contain theSameElementsAs List(
          "authenticatedUser",
          "pullRequest",
          "comments",
          "files",
          "createStatus - pending",
          "createComment",
          "createStatus - error"
        )
      }
    }
  }

  it should "not create a review if no files exist in a PR" in new Ctx with IOCtx with EmptyLogger {
    forAll { (baseUrl: Uri, user: User, pr: PullRequest) =>
      withTracing { trace =>
        val github = new GithubNotImpl[IO] {
          override def comments = trace.update("comments" :: _) >> IO.pure(List.empty)
          override def files = trace.update("files" :: _) >> IO.pure(List.empty)
        }
        val result = githubPrReviewJob().review[IO](baseUrl, github, user, pr)

        result.attempt.unsafeRunSync() shouldBe Left(NoFilesInPR)
        trace.get.unsafeRunSync() should contain theSameElementsAs List("files", "comments")
      }
    }
  }

  it should "not post any comments for no issues" in new Ctx with IOCtx with EmptyLogger {
    forAll { (baseUrl: Uri, user: User, pr: PullRequest, prFiles: NonEmptyList[File]) =>
      withTracing { trace =>
        val github = new GithubNotImpl[IO] {
          override def comments = trace.update("comments" :: _) >> IO.pure(List.empty)
          override def files = trace.update("files" :: _) >> IO.pure(prFiles.toList)
        }
        val result = githubPrReviewJob().review[IO](baseUrl, github, user, pr)

        result.attempt.unsafeRunSync() shouldBe Right(ReviewStatus(blocker = 0, critical = 0))
        trace.get.unsafeRunSync() should contain theSameElementsAs List("files", "comments")
      }
    }
  }

  it should "log and skip patches which can't be parsed" in new Ctx with IOCtx {
    forAll { (baseUrl: Uri, user: User, pr: PullRequest, prFiles: NonEmptyList[File]) =>
      withTracing { trace =>
        withLogging {
          case (logs, implicit0(logger: Logger[IO])) =>
            val github = new GithubNotImpl[IO] {
              override def comments = trace.update("comments" :: _) >> IO.pure(List.empty)
              override def files = trace.update("files" :: _) >> IO.pure(prFiles.toList)
            }
            val file: InputFile = TestInputFileBuilder.create("", prFiles.head.filename).build()
            val issue = Issue(RuleKey.of("repo", "rule"), file, 1, Severity.CRITICAL, "msg")

            val issues = new GlobalIssues
            issues.add(issue)
            val result = githubPrReviewJob(globalIssues = issues).review[IO](baseUrl, github, user, pr)

            result.attempt.unsafeRunSync() shouldBe Right(ReviewStatus(blocker = 0, critical = 1))
            trace.get.unsafeRunSync() should contain theSameElementsAs List("files", "comments")
            logs.get.unsafeRunSync().collect {
              case (level, msg) if level === LogLevel.Error => msg
            } should contain theSameElementsAs List(
              s"Error parsing patch for ${prFiles.head.filename}."
            )
        }
      }
    }
  }

  it should "post comments for pr issues" in new Ctx with IOCtx with EmptyLogger {
    forAll { (baseUrl: Uri, user: User, pr: PullRequest, prFile: File) =>
      withTracing { trace =>
        withLogging {
          case (logs, implicit0(logger: Logger[IO])) =>
            val fileWithPatch = prFile.copy(patch = patch)
            val github = new GithubNotImpl[IO] {
              override def comments = trace.update("comments" :: _) >> IO.pure(List.empty)
              override def createComment(comment: NewComment) =
                trace.update("createComment" :: _) >>
                IO.pure(Comment(1, comment.path, Some(comment.position), user, comment.body))
              override def files = trace.update("files" :: _) >> IO.pure(List(fileWithPatch))
            }
            val file: InputFile = TestInputFileBuilder.create("", fileWithPatch.filename).build()
            val issue = Issue(RuleKey.of("repo", "rule"), file, 5, Severity.BLOCKER, "msg")
            val markdown: Markdown = Markdown.inline(baseUrl, issue)

            val issues = new GlobalIssues
            issues.add(issue)
            val result = githubPrReviewJob(globalIssues = issues).review[IO](baseUrl, github, user, pr)

            result.attempt.unsafeRunSync() shouldBe Right(ReviewStatus(blocker = 1, critical = 0))
            trace.get.unsafeRunSync() should contain theSameElementsAs List(
              "files",
              "comments",
              "createComment"
            )
            logs.get.unsafeRunSync().collect {
              case (level, msg)
                  if level === LogLevel.Info ||
                  (level === LogLevel.Debug && msg.contains("Posting a new comment for")) =>
                msg
            } should contain theSameElementsAs List(
              "Posting new comments to Github.",
              s"Posting a new comment for ${prFile.filename}:${issue.line} - ${markdown.text}"
            )
        }
      }
    }
  }

  it should "not create new comments if there are no issues" in new Ctx with EmptyLogger {
    forAll {
      (
        baseUrl: Uri,
        user: User,
        pr: PullRequest,
        comments: List[Comment],
        files: List[File],
        patches: Map[String, File]
      ) =>
        val issues: Map[InputFile, List[Issue]] = Map.empty
        githubPrReviewJob()
          .newComments[IO](baseUrl, user, pr, comments, files, patches, issues)
          .unsafeRunSync() shouldBe empty
    }
  }

  it should "not create duplicate comments" in new Ctx with EmptyLogger {
    forAll {
      (
        baseUrl: Uri,
        user: User,
        pr: PullRequest,
        issue: Issue
      ) =>
        val issues = Map(
          issue.file -> List(issue.copy(line = 5))
        )
        val files = List(
          File(issue.file.toString, "status", patch)
        )
        val patches = files.groupBy(_.filename).view.mapValues(_.head).toMap
        val markdown = Markdown.inline(baseUrl, issue)
        val comments = List(
          Comment(1, issue.file.toString, Some(5), user, markdown.text)
        )

        githubPrReviewJob()
          .newComments[IO](baseUrl, user, pr, comments, files, patches, issues)
          .unsafeRunSync() shouldBe empty
    }
  }

  it should "create new comments" in new Ctx with EmptyLogger {
    forAll {
      (
        baseUrl: Uri,
        user: User,
        pr: PullRequest,
        issue: Issue
      ) =>
        val issues = Map(
          issue.file -> List(issue.copy(line = 5))
        )
        val files = List(
          File(issue.file.toString, "status", patch)
        )
        val patches = files.groupBy(_.filename).view.mapValues(_.head).toMap
        val markdown = Markdown.inline(baseUrl, issue)

        val expected = NewComment(markdown.text, pr.head.sha, issue.file.toString, 5)

        githubPrReviewJob()
          .newComments[IO](baseUrl, user, pr, List.empty, files, patches, issues)
          .unsafeRunSync()
          .loneElement shouldBe expected
    }
  }

  it should "ignore outdated comments" in new Ctx with EmptyLogger {
    forAll {
      (
        baseUrl: Uri,
        user: User,
        pr: PullRequest,
        issue: Issue
      ) =>
        val issues = Map(
          issue.file -> List(issue.copy(line = 5))
        )
        val files = List(
          File(issue.file.toString, "status", patch)
        )
        val patches = files.groupBy(_.filename).view.mapValues(_.head).toMap
        val markdown = Markdown.inline(baseUrl, issue)
        val comments = List(
          Comment(1, issue.file.toString, None, user, markdown.text)
        )

        val expected = NewComment(markdown.text, pr.head.sha, issue.file.toString, 5)

        githubPrReviewJob()
          .newComments[IO](baseUrl, user, pr, comments, files, patches, issues)
          .unsafeRunSync()
          .loneElement shouldBe expected
    }
  }

  it should "lookup existing comments for issues" in {
    forAll { (issue: Issue, issue2: Issue) =>
      val issues = Map(
        issue.file -> List(issue),
        issue2.file -> List(issue2)
      )
      val patchLineMapping = Map(
        issue.file.toString -> Right(Map(FileLine(issue.line) -> PatchLine(7)))
      )
      val comment = Comment(1, issue.file.toString, Some(7), User("usr"), "comment")
      val comments = Map(
        issue.file.toString -> List(
          comment,
          Comment(2, issue.file.toString, Some(123), User("usr"), "comment"),
          Comment(3, issue.file.toString, Some(123), User("usr"), "comment")
        )
      )

      val expected = Map(
        issue.file -> Map(PatchLine(7) -> List((issue, List(comment)))),
        issue2.file -> Map.empty
      )

      GithubPrReviewJob.allCommentsForIssues(issues, patchLineMapping, comments) shouldBe expected
    }
  }

  it should "return a lookup with empty comments if there are no comments" in {
    forAll { (issue: Issue, issue2: Issue) =>
      val issues = Map(
        issue.file -> List(issue),
        issue2.file -> List(issue2)
      )
      val patchLineMapping = Map(
        issue.file.toString -> Right(Map(FileLine(issue.line) -> PatchLine(7)))
      )
      val comments = Map.empty[String, List[Comment]]

      val expected = Map(
        issue.file -> Map(
          PatchLine(7) -> List((issue, List.empty))
        ),
        issue2.file -> Map.empty
      )

      GithubPrReviewJob.allCommentsForIssues(issues, patchLineMapping, comments) shouldBe expected
    }
  }

  it should "create comments for new issues" in {
    forAll { (commit: String, issuesComments: List[(Issue, List[Comment])]) =>
      val uri = Uri.unsafeFromString("https://hello.com")
      val issues =
        issuesComments
          .groupBy(v => (v._1.file))
          .view
          .mapValues(_.groupBy(v => PatchLine(v._1.line)))
          .toMap
      val newComments = issuesComments.map {
        case (issue, _) =>
          NewComment(Markdown.inline(uri, issue).text, commit, issue.file.toString, issue.line)
      }

      GithubPrReviewJob.commentsForNewIssues(uri, commit, issues) should
      contain theSameElementsAs newComments
    }
  }

  it should "ignore existing comments created by sonar-scala" in {
    forAll { (commit: String, ruleKey: RuleKey, inputFile: InputFile) =>
      val uri = Uri.unsafeFromString("https://hello.com")
      val issues = List(
        (
          Issue(ruleKey, inputFile, 1, Severity.MINOR, "message 1"),
          List(Comment(1, "path", Some(1), User("user"), "body 1"))
        )
      )

      val grouped =
        issues
          .groupBy(v => (v._1.file))
          .view
          .mapValues(_.groupBy(v => PatchLine(v._1.line)))
          .toMap

      val newComments = issues.map {
        case (issue, _) =>
          NewComment(Markdown.inline(uri, issue).text, commit, issue.file.toString, issue.line)
      }

      GithubPrReviewJob.commentsForNewIssues(uri, commit, grouped) should contain theSameElementsAs newComments
    }
  }

  it should "determine a review status based on found issues" in {
    forAll { i: List[Issue] =>
      val blockers = i.filter(_.severity === Severity.BLOCKER).size
      val critical = i.filter(_.severity === Severity.CRITICAL).size
      GithubPrReviewJob.reviewStatus(i.groupBy(_.file)) shouldBe ReviewStatus(blockers, critical)
    }
  }

  it should "infer status state from a pr status" in {
    GithubPrReviewJob.statusState(Pending) shouldBe "pending"
    GithubPrReviewJob.statusState(Success) shouldBe "success"
    GithubPrReviewJob.statusState(Error(ReviewStatus(0, 0))) shouldBe "error"
    GithubPrReviewJob.statusState(Failure(new Throwable)) shouldBe "failure"
  }

  it should "infer status description from a pr status" in {
    GithubPrReviewJob.statusDescription(Pending) shouldBe "SonarQube is reviewing this pull request."
    GithubPrReviewJob.statusDescription(Success) shouldBe "SonarQube didn't report any critical or blocker issues."
    GithubPrReviewJob.statusDescription(Error(ReviewStatus(1, 3))) shouldBe "SonarQube reported 1 blocker and 3 critical issues."
    GithubPrReviewJob.statusDescription(Failure(new Throwable)) shouldBe "An error occurred during SonarQube review."
  }

  it should "infer a new pr status from the result of the review" in {
    GithubPrReviewJob.githubStatus(Pending) shouldBe NewStatus(
      "pending",
      "",
      "SonarQube is reviewing this pull request.",
      "sonar-scala/review"
    )
  }
}
