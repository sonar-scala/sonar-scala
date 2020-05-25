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

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.sonar.api.internal.SonarRuntimeImpl
import org.sonar.api.utils.Version
import org.sonar.api.{Plugin, SonarEdition, SonarQubeSide, SonarRuntime}

class ScalaPluginSpec extends AnyFlatSpec with Matchers {
  val runtime: SonarRuntime = SonarRuntimeImpl.forSonarQube(
    Version.create(8, 3),
    SonarQubeSide.SCANNER,
    SonarEdition.COMMUNITY
  )
  val context = new Plugin.Context(runtime)
  new ScalaPlugin().define(context)

  it should "provide global config" in {
    context.getExtensions should contain(classOf[GlobalConfig])
  }

  it should "provide scala sensor" in {
    context.getExtensions should contain(classOf[Scala])
    context.getExtensions should contain(classOf[sensor.ScalaSensor])
  }

  it should "provide Github pr review job" in {
    context.getExtensions should contain(classOf[pr.GlobalIssues])
    context.getExtensions should contain(classOf[pr.GithubPrReviewJob])
  }

  it should "provide Scalastyle repository, quality profile & sensor" in {
    context.getExtensions should contain(classOf[scalastyle.ScalastyleRulesRepository])
    context.getExtensions should contain(classOf[scalastyle.ScalastyleQualityProfile])
    context.getExtensions should contain(classOf[scalastyle.ScalastyleChecker])
    context.getExtensions should contain(classOf[scalastyle.ScalastyleSensor])
  }

  it should "provide scapegoat repository, quality profile & sensor" in {
    context.getExtensions should contain(classOf[scapegoat.ScapegoatRulesRepository])
    context.getExtensions should contain(classOf[scapegoat.ScapegoatQualityProfile])
    context.getExtensions should contain(classOf[scapegoat.ScapegoatReportParser])
    context.getExtensions should contain(classOf[scapegoat.ScapegoatSensor])
  }

  it should "provide additional built-in quality profiles" in {
    context.getExtensions should contain(classOf[qualityprofiles.ScalastyleScapegoatQualityProfile])
    context.getExtensions should contain(classOf[qualityprofiles.RecommendedQualityProfile])
  }

  it should "provide scoverage sensor" in {
    context.getExtensions should contain(classOf[scoverage.ScoverageMeasures])
    context.getExtensions should contain(classOf[scoverage.ScoverageMetrics])
    context.getExtensions should contain(classOf[scoverage.ScoverageReportParser])
    context.getExtensions should contain(classOf[scoverage.ScoverageSensor])
  }

  it should "provide junit sensor" in {
    context.getExtensions should contain(classOf[junit.JUnitReportParser])
    context.getExtensions should contain(classOf[junit.JUnitSensor])
  }
}
