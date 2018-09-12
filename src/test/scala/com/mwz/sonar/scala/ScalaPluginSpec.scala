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

import com.mwz.sonar.scala.checkstyle.CheckstyleReportParser
import com.mwz.sonar.scala.scalastyle.ScalastyleQualityProfile
import com.mwz.sonar.scala.scalastyle.ScalastyleRulesRepository
import com.mwz.sonar.scala.scalastyle.ScalastyleSensor
import com.mwz.sonar.scala.scapegoat.ScapegoatQualityProfile
import com.mwz.sonar.scala.scapegoat.ScapegoatRulesRepository
import com.mwz.sonar.scala.scapegoat.ScapegoatSensor
import com.mwz.sonar.scala.scoverage.ScoverageMetrics
import com.mwz.sonar.scala.scoverage.ScoverageReportParser
import com.mwz.sonar.scala.scoverage.ScoverageSensor
import com.mwz.sonar.scala.sensor.ScalaSensor
import org.scalatest.FlatSpec
import org.scalatest.Matchers
import org.sonar.api.internal.SonarRuntimeImpl
import org.sonar.api.utils.Version
import org.sonar.api.Plugin
import org.sonar.api.SonarQubeSide
import org.sonar.api.SonarRuntime

/** Tests the Scala SonarQube plugin extension points */
class ScalaPluginSpec extends FlatSpec with Matchers {
  val runtime: SonarRuntime = SonarRuntimeImpl.forSonarQube(Version.create(6, 7), SonarQubeSide.SCANNER)
  val context = new Plugin.Context(runtime)
  new ScalaPlugin().define(context)
  behavior of "the scala plugin"

  it should "provide scala sensor" in {
    assert(context.getExtensions.contains(classOf[Scala]))
    assert(context.getExtensions.contains(classOf[ScalaSensor]))
  }

  it should "provide base checkstyle parser" in {
    assert(context.getExtensions.contains(classOf[CheckstyleReportParser]))
  }

  it should "provide scalastyle sensor" in {
    assert(context.getExtensions.contains(classOf[ScalastyleRulesRepository]))
    assert(context.getExtensions.contains(classOf[ScalastyleQualityProfile]))
    assert(context.getExtensions.contains(classOf[ScalastyleSensor]))
  }

  it should "provide scoverage sensor" in {
    assert(context.getExtensions.contains(classOf[ScoverageMetrics]))
    assert(context.getExtensions.contains(classOf[ScoverageReportParser]))
    assert(context.getExtensions.contains(classOf[ScoverageSensor]))
  }

  it should "provide scapegoat sensor" in {
    assert(context.getExtensions.contains(classOf[ScapegoatRulesRepository]))
    assert(context.getExtensions.contains(classOf[ScapegoatQualityProfile]))
    assert(context.getExtensions.contains(classOf[ScapegoatSensor]))
  }
}
