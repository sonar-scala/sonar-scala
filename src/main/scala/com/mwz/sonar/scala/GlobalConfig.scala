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

import cats.instances.string._
import cats.syntax.eq._
import com.mwz.sonar.scala.GlobalConfig.{Github, PullRequest}
import com.mwz.sonar.scala.util.Log
import com.mwz.sonar.scala.util.syntax.Optionals._
import org.http4s.{ParseFailure, ParseResult, Uri}
import org.sonar.api.CoreProperties
import org.sonar.api.batch.{InstantiationStrategy, ScannerSide}
import org.sonar.api.config.Configuration

// TODO: Both @ScannerSide and @InstantiationStrategy are deprecated, we should switch
//  to the org.sonar.api.scanner.ScannerSide in the future.
@ScannerSide
@InstantiationStrategy(InstantiationStrategy.PER_BATCH)
final class GlobalConfig(config: Configuration) {
  private[this] val logger = Log(classOf[Scala], "config")

  val baseUrl: ParseResult[Uri] =
    config
      .get(CoreProperties.SERVER_BASE_URL)
      .toOption
      .orElse(config.get("sonar.host.url").toOption)
      .fold[ParseResult[Uri]](
        Left(
          ParseFailure(
            "Missing SonarQube base URI",
            "Please configure the server base URL in your SonarQube instance or set the 'sonar.host.url' property."
          )
        )
      )(Uri.fromString)

  val pullRequest: Option[PullRequest] = getPullRequest

  /**
   * Pull request mode which enables PR decoration
   * (for both issues and coverage).
   */
  def prDecoration: Boolean = pullRequest.nonEmpty

  /**
   * Post issues as PR comments.
   */
  def issueDecoration: Boolean = pullRequest.exists(!_.disableIssues)

  /**
   * Post coverage data as PR comments.
   */
  def coverageDecoration: Boolean = false

  // TODO: Use Either to catch and log errors.
  private[this] def getPullRequest: Option[PullRequest] =
    for {
      provider    <- config.get("sonar.scala.pullrequest.provider").toOption.filter(_ === "github")
      prNumber    <- config.get("sonar.scala.pullrequest.key").toOption
      githubRepo  <- config.get("sonar.scala.pullrequest.github.repository").toOption
      githubOauth <- config.get("sonar.scala.pullrequest.github.oauth").toOption
      // TODO: Use the new config syntax.
      disableIssues = config.get("sonar.scala.pullrequest.issues.disable").toOption.contains("true")
      disableInlineComments = config
        .get("sonar.scala.pullrequest.issues.disableInlineComments")
        .toOption
        .contains("true")
      disableCoverage = config.get("sonar.scala.pullrequest.coverage.disable").toOption.contains("true")
    } yield PullRequest(
      provider,
      prNumber,
      Github(githubRepo, githubOauth),
      disableIssues,
      disableInlineComments,
      disableCoverage
    )
}

object GlobalConfig {

  /**
   * General PR settings:
   * - sonar.scala.pullrequest.provider=github
   * - sonar.scala.pullrequest.key - pr number
   *
   * Github settings:
   * - sonar.scala.pullrequest.github.repository - org/project, e.g. mwz/sonar-scala
   * - sonar.scala.pullrequest.github.oauth - Github oauth token
   *
   * Issues:
   * - sonar.scala.pullrequest.issues.disable - disable posting issues
   * - TODO: sonar.scala.pullrequest.issues.disableInlineComments - disable inline comments and post a summary instead
   *
   * Coverage:
   * - TODO: sonar.scala.pullrequest.coverage.disable - disable posting coverage summary
   */
  final case class PullRequest(
    provider: String,
    prNumber: String,
    github: Github,
    disableIssues: Boolean,
    disableInlineComments: Boolean,
    disableCoverage: Boolean
  )
  final case class Github(
    repository: String,
    oauth: String
  )
}
