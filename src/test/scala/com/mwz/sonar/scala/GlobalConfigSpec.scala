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

import org.http4s.ParseFailure
import org.scalatest.{EitherValues, FlatSpec, Matchers, OptionValues}
import org.sonar.api.config.internal.MapSettings
import scalariform.{ScalaVersion, ScalaVersions}

class GlobalConfigSpec extends FlatSpec with Matchers with OptionValues with EitherValues {

  it should "return a parsing failure if SonarQube base URL is missing" in {
    val conf = new MapSettings().asConfig()
    val uri = new GlobalConfig(conf).baseUrl

    uri.left.value shouldBe ConfigError(
      "Missing SonarQube base URI - please configure the server base URL in your SonarQube instance or set the 'sonar.host.url' property."
    )
  }

  it should "not turn on pr decoration if the provider is not defined" in {
    val conf = new MapSettings().asConfig()
    val globalConf = new GlobalConfig(conf)

    globalConf.pullRequest.value shouldBe empty
    globalConf.prDecoration shouldBe false
    globalConf.issueDecoration shouldBe false
    globalConf.coverageDecoration shouldBe false
  }

  it should "validate the pr provider property" in {
    val conf = new MapSettings().setProperty("sonar.scala.pullrequest.provider", "other").asConfig()
    val globalConf = new GlobalConfig(conf)

    globalConf.pullRequest.value.value shouldBe Left(
      ConfigError("""Currently only "github" provider is supported.""")
    )
  }

  it should "validate the pr number property" in {
    val conf = new MapSettings()
      .setProperty("sonar.scala.pullrequest.provider", "github")
      .asConfig()
    val globalConf = new GlobalConfig(conf)

    globalConf.pullRequest.value.value shouldBe Left(
      ConfigError("""Please provide a pull request number (sonar.scala.pullrequest.number).""")
    )
  }
}
