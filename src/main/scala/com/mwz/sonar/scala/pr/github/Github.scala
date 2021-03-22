/*
 * Copyright (C) 2018-2021  All sonar-scala contributors
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
package github

import cats.effect.Sync
import cats.syntax.flatMap._
import com.mwz.sonar.scala.pr.github.Codec._
import io.circe.generic.auto._
import mouse.boolean._
import org.http4s.client.Client
import org.http4s.{Header, Headers, Method, Request, Uri}

trait Github[F[_]] {
  def authenticatedUser: F[User]
  def pullRequest: F[PullRequest]
  def comments: F[List[Comment]]
  def createComment(comment: NewComment): F[Unit]
  def files: F[List[File]]
  def createStatus(sha: String, status: NewStatus): F[Unit]
}

object Github {
  def apply[F[_]: Sync](client: Client[F], pr: GlobalConfig.PullRequest): Github[F] =
    new Github[F] {
      val auth: Header = Header("Authorization", s"token ${pr.github.oauth}")
      val userUri: Uri = pr.github.apiuri / "user"
      val prUri: Uri =
        (pr.github.apiuri / "repos").addPath(pr.github.repository) / "pulls" / pr.prNumber
      val commentsUri: Uri = prUri / "comments"
      val filesUri: Uri = prUri / "files"
      def newStatusUri(sha: String): Uri =
        (pr.github.apiuri / "repos").addPath(pr.github.repository) / "statuses" / sha
      def request(uri: Uri): Request[F] = {
        Request[F](
          uri = uri,
          headers = Headers.of(auth)
        )
      }
      def authenticatedUser: F[User] = client.expect[User](request(userUri))
      def pullRequest: F[PullRequest] = client.expect[PullRequest](request(prUri))
      def comments: F[List[Comment]] = client.expect[List[Comment]](request(commentsUri))
      def createComment(comment: NewComment): F[Unit] = {
        val request: F[Request[F]] = Sync[F].pure(
          Request(Method.POST, commentsUri, headers = Headers.of(auth))
            .withEntity(comment)
        )
        pr.dryRun.fold(Sync[F].unit, client.expect[Comment](request) >> Sync[F].unit)
      }
      def files: F[List[File]] = client.expect[List[File]](request(filesUri))
      def createStatus(sha: String, status: NewStatus): F[Unit] = {
        val request: F[Request[F]] = Sync[F].pure(
          Request(Method.POST, newStatusUri(sha), headers = Headers.of(auth))
            .withEntity(status)
        )
        pr.dryRun.fold(Sync[F].unit, client.expect[Status](request) >> Sync[F].unit)
      }
    }
}
