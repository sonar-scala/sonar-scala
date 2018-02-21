/*
 * Sonar Scala Plugin
 * Copyright (C) 2014 All contributors
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
package com.sagacify.sonar.scala

import com.buransky.plugins.scoverage.measure.ScalaMetrics
import com.buransky.plugins.scoverage.sensor.ScoverageSensor
import com.ncredinburgh.sonar.scalastyle.{ScalastyleQualityProfile, ScalastyleRepository, ScalastyleSensor}
import org.scalatest.{FlatSpec, Matchers}
import org.sonar.api.utils.Version

/**
 * Tests ScalaPlugin
 */
class ScalaPluginSpec extends FlatSpec with Matchers {

  import org.sonar.api.{Plugin, SonarQubeSide, SonarRuntime}
  import org.sonar.api.internal.SonarRuntimeImpl

  val runtime: SonarRuntime = SonarRuntimeImpl.forSonarQube(Version.create(6, 7), SonarQubeSide.SCANNER)
  val context = new Plugin.Context(runtime)
  new ScalaPlugin().define(context)

  "a scala plugin" should "provide a scala sensor" in {
    assert(context.getExtensions.contains(classOf[ScalaSensor]))
  }

  it should "provide a scalastyle sensor" in {
    assert(context.getExtensions.contains(classOf[ScalastyleSensor]))
  }

  it should "provide a scalastyle repository" in {
    assert(context.getExtensions.contains(classOf[ScalastyleRepository]))
  }

  it should "provide a scala language" in {
    assert(context.getExtensions.contains(classOf[Scala]))
  }

  it should "provide a scalastyle quality profile" in {
    assert(context.getExtensions.contains(classOf[ScalastyleQualityProfile]))
  }

  it should "provide a scoverage sensor" in {
    assert(context.getExtensions.contains(classOf[ScoverageSensor]))
  }

  it should "provide scala metrics" in {
    assert(context.getExtensions.contains(classOf[ScalaMetrics]))
  }
}
