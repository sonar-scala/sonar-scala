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

package com.mwz.sonar.scala.pr.github

import cats.data.Kleisli
import cats.data.OptionT
import cats.effect.IO
import com.mwz.sonar.scala.GlobalConfig
import com.mwz.sonar.scala.pr.github.Codec._
import io.circe.syntax._
import org.http4s.circe.CirceEntityCodec._
import org.http4s.circe.CirceEntityEncoder._
import org.http4s.AuthedService
import org.http4s.client.Client
import org.http4s.dsl.io._
import org.http4s.headers.Authorization
import org.http4s.HttpApp
import org.http4s.HttpRoutes
import org.http4s.implicits._
import org.http4s.Request
import org.http4s.server._
import org.scalacheck._
import org.scalacheck.ScalacheckShapeless._
import org.scalatest.{FlatSpec, Matchers}
import org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks

class GithubSpec extends FlatSpec with Matchers with ScalaCheckDrivenPropertyChecks {
  val user = User("user123")
  val conf = GlobalConfig.PullRequest(
    provider = "github",
    prNumber = "123",
    github = GlobalConfig.Github("owner/repo", "oauthToken"),
    disableIssues = false,
    disableInlineComments = true,
    disableCoverage = true
  )

  val authUser: Kleisli[OptionT[IO, ?], Request[IO], String] =
    Kleisli { req =>
      OptionT.fromOption(
        req.headers
          .get(Authorization)
          .find(_.value === s"token ${conf.github.oauth}")
          .map(_ => user.login)
      )
    }

  val auth: AuthMiddleware[IO, String] =
    AuthMiddleware(authUser)

  it should "get the authenticated user" in {
    val service = AuthedService[String, IO] {
      case req @ GET -> Root / "user" as _ =>
        Ok(user)
    }

    val client = Client.fromHttpApp(HttpApp(auth(service).orNotFound.run))
    Github(client, conf).authenticatedUser.unsafeRunSync() shouldBe user
  }

  it should "get a pull request" in {
    forAll { (pr: PullRequest) =>
      val prNumber = pr.number.toString
      val http = HttpRoutes.of[IO] {
        case req @ GET -> Root / "repos" / "owner" / "repo" / "pulls" / prNumber =>
          Ok(pr)
      }

      val client = Client.fromHttpApp(http.orNotFound)
      Github(client, conf).pullRequest.unsafeRunSync() shouldBe pr
    }
  }

  it should "get comments" in {
    forAll { (comments: List[Comment]) =>
      val http = HttpRoutes.of[IO] {
        case req @ GET -> Root / "repos" / "owner" / "repo" / "pulls" / "123" / "comments" =>
          Ok(comments)
      }

      val client = Client.fromHttpApp(http.orNotFound)
      Github(client, conf).comments.unsafeRunSync() shouldBe comments
    }
  }

  it should "create new comments" in {
    forAll { (newComment: NewComment, comment: Comment) =>
      val response = comment.copy(
        path = newComment.path,
        user = user,
        body = newComment.body
      )
      val http = AuthedService[String, IO] {
        case req @ POST -> Root / "repos" / "owner" / "repo" / "pulls" / "123" / "comments" as _ =>
          Ok(response)
      }

      val client = Client.fromHttpApp(HttpApp(auth(http).orNotFound.run))
      Github(client, conf).createComment(newComment).unsafeRunSync() shouldBe response
    }
  }

  it should "get pr files" in {
    forAll { (files: List[File]) =>
      val http = HttpRoutes.of[IO] {
        case req @ GET -> Root / "repos" / "owner" / "repo" / "pulls" / "123" / "files" =>
          Ok(files)
      }

      val client = Client.fromHttpApp(http.orNotFound)
      Github(client, conf).files.unsafeRunSync() shouldBe files
    }
  }

  it should "create a new pr status" in {
    val strGen = Gen.nonEmptyListOf(Gen.alphaNumChar).map(_.mkString)
    forAll(strGen, implicitly[Arbitrary[NewStatus]].arbitrary) { (sha: String, newStatus: NewStatus) =>
      val response = Status(
        state = newStatus.state,
        targetUrl = newStatus.targetUrl,
        description = newStatus.description,
        context = newStatus.context
      )
      val http = AuthedService[String, IO] {
        case req @ POST -> Root / "repos" / "owner" / "repo" / "statuses" / sha as _ =>
          Ok(response)
      }

      val client = Client.fromHttpApp(HttpApp(auth(http).orNotFound.run))
      Github(client, conf).createStatus(sha, newStatus).unsafeRunSync() shouldBe response
    }
  }
}