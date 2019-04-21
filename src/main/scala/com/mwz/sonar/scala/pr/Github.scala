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

import cats.effect.Sync
import com.mwz.sonar.scala.pr.Codec._
import io.circe.generic.auto._
import org.http4s.client.Client
import org.http4s.{Header, Headers, Method, Request, Uri}

import scala.language.higherKinds

trait Github[F[_]] {
  def authenticatedUser: F[User]
  def pullRequest: F[PullRequest]
  def comments: F[List[Comment]]
  def createComment(comment: NewComment): F[Comment]
  def files: F[List[File]]
  def createStatus(sha: String, status: NewStatus): F[Status]
}

object Github {
  def apply[F[_]: Sync](client: Client[F], pr: GlobalConfig.PullRequest): Github[F] =
    new Github[F] {
      val prUri: String =
        s"https://api.github.com/repos/${pr.github.repository}/pulls/${pr.prNumber}"
      val commentsUri: String =
        s"https://api.github.com/repos/${pr.github.repository}/pulls/${pr.prNumber}/comments"
      val filesUri: String =
        s"https://api.github.com/repos/${pr.github.repository}/pulls/${pr.prNumber}/files"
      def statusesUri(ref: String): String =
        s"https://api.github.com/repos/${pr.github.repository}/commits/$ref/statuses"
      def newStatusUri(sha: String): String =
        s"https://api.github.com/repos/${pr.github.repository}/statuses/$sha"

      def authenticatedUser: F[User] = {
        val auth = Header("Authorization", s"token ${pr.github.oauth}")
        val request = Request[F](
          uri = Uri.uri("https://api.github.com/user"),
          headers = Headers(auth)
        )
        client.expect[User](request)
      }
      def pullRequest: F[PullRequest] = client.expect[PullRequest](prUri)
      def comments: F[List[Comment]] = client.expect[List[Comment]](commentsUri)
      def createComment(comment: NewComment): F[Comment] = {
        val request: F[Request[F]] = Uri
          .fromString(commentsUri)
          .fold(
            Sync[F].raiseError,
            uri => Sync[F].pure(Request(Method.POST, uri).withEntity(comment))
          )
        client.expect[Comment](request)
      }
      def files: F[List[File]] = client.expect[List[File]](filesUri)
      def createStatus(sha: String, status: NewStatus): F[Status] = {
        val request: F[Request[F]] = Uri
          .fromString(newStatusUri(sha))
          .fold(
            Sync[F].raiseError,
            uri => Sync[F].pure(Request(Method.POST, uri).withEntity(status))
          )
        client.expect[Status](request)
      }
    }
}
