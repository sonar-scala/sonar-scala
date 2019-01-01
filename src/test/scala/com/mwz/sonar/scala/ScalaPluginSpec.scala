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

import org.scalatest.{FlatSpec, Matchers}
import org.sonar.api.internal.SonarRuntimeImpl
import org.sonar.api.utils.Version
import org.sonar.api.{Plugin, SonarQubeSide, SonarRuntime}

class ScalaPluginSpec extends FlatSpec with Matchers {
  val runtime: SonarRuntime = SonarRuntimeImpl.forSonarQube(
    Version.create(7, 3),
    SonarQubeSide.SCANNER
  )
  val context = new Plugin.Context(runtime)
  new ScalaPlugin().define(context)

  "Scala plugin" should "provide scala sensor" in {
    assert(context.getExtensions.contains(classOf[Scala]))
    assert(context.getExtensions.contains(classOf[sensor.ScalaSensor]))
  }

  it should "provide Scalastyle repository, quality profile & sensor" in {
    assert(context.getExtensions.contains(classOf[scalastyle.ScalastyleRulesRepository]))
    assert(context.getExtensions.contains(classOf[scalastyle.ScalastyleQualityProfile]))
    assert(context.getExtensions.contains(classOf[scalastyle.ScalastyleChecker]))
    assert(context.getExtensions.contains(classOf[scalastyle.ScalastyleSensor]))
  }

  it should "provide scapegoat repository, quality profile & sensor" in {
    assert(context.getExtensions.contains(classOf[scapegoat.ScapegoatRulesRepository]))
    assert(context.getExtensions.contains(classOf[scapegoat.ScapegoatQualityProfile]))
    assert(context.getExtensions.contains(classOf[scapegoat.ScapegoatReportParser]))
    assert(context.getExtensions.contains(classOf[scapegoat.ScapegoatSensor]))
  }

  it should "provide additional built-in quality profiles" in {
    assert(context.getExtensions.contains(classOf[qualityprofiles.ScalastyleScapegoatQualityProfile]))
    assert(context.getExtensions.contains(classOf[qualityprofiles.RecommendedQualityProfile]))
  }

  it should "provide scoverage sensor" in {
    assert(context.getExtensions.contains(classOf[scoverage.ScoverageMetrics]))
    assert(context.getExtensions.contains(classOf[scoverage.ScoverageReportParser]))
    assert(context.getExtensions.contains(classOf[scoverage.ScoverageSensor]))
  }

  it should "provide junit sensor" in {
    assert(context.getExtensions.contains(classOf[junit.JUnitReportParser]))
    assert(context.getExtensions.contains(classOf[junit.JUnitSensor]))
  }
}
