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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
package com.mwz.sonar.scala.scoverage

import org.scalatest.{FlatSpec, LoneElement, Matchers}
import org.sonar.api.batch.fs.InputFile
import org.sonar.api.batch.sensor.internal.{DefaultSensorDescriptor, SensorContextTester}

/**
 *  Tests the Scoverage Sensor
 *
 *  @author BalmungSan
 */
class ScoverageSensorSpec extends FlatSpec with LoneElement with Matchers {
  val scoverageSensor = new ScoverageSensor()
  behavior of "A ScoverageSensor"

  it should "correctly set descriptor" in {
    val descriptor = new DefaultSensorDescriptor
    scoverageSensor.describe(descriptor)

    descriptor.name() shouldBe "Scoverage Sensor"
    descriptor.languages().loneElement shouldBe "scala"
    descriptor.`type`() shouldBe InputFile.Type.MAIN
  }
}
