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

import cats.data.EitherT
import cats.instances.option._
import cats.instances.string._
import cats.syntax.either._
import cats.syntax.eq._
import com.mwz.sonar.scala.GlobalConfig._
import com.mwz.sonar.scala.util.Log
import com.mwz.sonar.scala.util.syntax.Optionals._
import com.mwz.sonar.scala.util.syntax.SonarConfig._
import org.http4s.{ParseFailure, ParseResult, Uri}
import org.sonar.api.CoreProperties
import org.sonar.api.batch.{InstantiationStrategy, ScannerSide}
import org.sonar.api.config.Configuration

@SuppressWarnings(Array("IncorrectlyNamedExceptions"))
final case class ConfigError(error: String) extends Exception

// TODO: Both @ScannerSide and @InstantiationStrategy are deprecated, we should switch
//  to the org.sonar.api.scanner.ScannerSide in the future.
@ScannerSide
@InstantiationStrategy(InstantiationStrategy.PER_BATCH)
final class GlobalConfig(config: Configuration) {
  private[this] val logger = Log(classOf[Scala], "config")

  val baseUrl: ConfigErrorOr[Uri] =
    config
      .get(CoreProperties.SERVER_BASE_URL)
      .toOption
      .orElse(config.get("sonar.host.url").toOption)
      .fold[ParseResult[Uri]](
        Left(
          ParseFailure(
            "Missing SonarQube base URI - please configure the server base URL in your SonarQube instance or set the 'sonar.host.url' property.",
            ""
          )
        )
      )(Uri.fromString)
      .leftMap(f => ConfigError(f.sanitized))

  val pullRequest: EitherT[Option, ConfigError, PullRequest] = getPullRequest

  /**
   * Pull request mode which enables PR decoration
   * (for both issues and coverage).
   */
  def prDecoration: Boolean =
    pullRequest.isRight.getOrElse(false)

  /**
   * Post issues as PR comments.
   */
  def issueDecoration: Boolean =
    pullRequest.exists(!_.disableIssues).getOrElse(false)

  /**
   * Post coverage data as PR comments.
   */
  def coverageDecoration: Boolean =
    pullRequest.exists(!_.disableCoverage).getOrElse(false)

  private[this] def getPullRequest: EitherT[Option, ConfigError, PullRequest] =
    for {
      provider <- EitherT[Option, ConfigError, String](
        config
          .getAs[String](PR_PROVIDER)
          .map { s =>
            Option(s)
              .filter(_ === "github")
              .toRight(ConfigError("""Currently only "github" provider is supported."""))
          }
      )
      prNumber <- EitherT.fromOption(
        config.getAs[String](PR_NUMBER),
        ConfigError(s"Please provide a pull request number ($PR_NUMBER).")
      )
      githubRepo <- EitherT.fromOption(
        config.getAs[String](PR_GITHUB_REPO),
        ConfigError(
          s"""Please provide a name of the github repository, e.g. "owner/repository" ($PR_GITHUB_REPO)."""
        )
      )
      githubOauth <- EitherT.fromOption(
        config.getAs[String](PR_GITHUB_OAUTH),
        ConfigError(
          s"""Please provide a github oauth token ($PR_GITHUB_OAUTH)."""
        )
      )
      disableIssues = config.getAs[Boolean](PR_DISABLE_ISSUES)
      disableInlineComments = config.getAs[Boolean](PR_DISABLE_INLINE_COMMENTS)
      disableCoverage = true
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
  private val PR_PROVIDER = "sonar.scala.pullrequest.provider"
  private val PR_NUMBER = "sonar.scala.pullrequest.number"
  private val PR_GITHUB_REPO = "sonar.scala.pullrequest.github.repository"
  private val PR_GITHUB_OAUTH = "sonar.scala.pullrequest.github.oauth"
  private val PR_DISABLE_ISSUES = "sonar.scala.pullrequest.issues.disable"
  private val PR_DISABLE_INLINE_COMMENTS = "sonar.scala.pullrequest.issues.disableInlineComments"
  private val PR_DISABLE_COVERAGE = "sonar.scala.pullrequest.coverage.disable"

  /**
   * General PR settings:
   * - sonar.scala.pullrequest.provider=github
   * - sonar.scala.pullrequest.number - pull request number
   *
   * Github settings:
   * - sonar.scala.pullrequest.github.repository - org/project, e.g. mwz/sonar-scala
   * - sonar.scala.pullrequest.github.oauth - Github oauth token
   *
   * Issues:
   * - sonar.scala.pullrequest.issues.disable - disable posting issues
   * - sonar.scala.pullrequest.issues.disableInlineComments - disable inline comments and post a summary instead (currently not used)
   *
   * Coverage:
   * - sonar.scala.pullrequest.coverage.disable - disable posting coverage summary (currently not used)
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
