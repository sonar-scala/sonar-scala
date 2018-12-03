/*
 * Sonar Scala Plugin
 * Copyright (C) 2018 All contributors
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package com.mwz.sonar.scala

import com.ncredinburgh.sonar.{scalastyle => oldscalastyle}
import org.scalatest.{FlatSpec, Matchers}
import org.sonar.api.internal.SonarRuntimeImpl
import org.sonar.api.utils.Version
import org.sonar.api.{Plugin, SonarQubeSide, SonarRuntime}

class ScalaPluginSpec extends FlatSpec with Matchers {
  val runtime: SonarRuntime = SonarRuntimeImpl.forSonarQube(
    Version.create(6, 7),
    SonarQubeSide.SCANNER
  )
  val context = new Plugin.Context(runtime)
  new ScalaPlugin().define(context)

  "Scala plugin" should "provide scala sensor" in {
    assert(context.getExtensions.contains(classOf[Scala]))
    assert(context.getExtensions.contains(classOf[sensor.ScalaSensor]))
  }

  it should "provide Old Scalastyle (ncredinburgh) repository and quality profile" in {
    assert(context.getExtensions.contains(classOf[oldscalastyle.ScalastyleRepository]))
    assert(context.getExtensions.contains(classOf[oldscalastyle.ScalastyleQualityProfile]))
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
}
