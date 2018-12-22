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
package com.mwz.sonar.scala
package junit

import java.io.File
import java.nio.file.{Path, Paths}

import org.mockito.ArgumentMatchers._
import org.mockito.Mockito._
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{FlatSpec, LoneElement, Matchers}
import org.sonar.api.batch.fs.InputFile
import org.sonar.api.batch.fs.internal.DefaultFileSystem
import org.sonar.api.batch.sensor.internal.{DefaultSensorDescriptor, SensorContextTester}
import org.sonar.api.config.internal.MapSettings

class JUnitSensorSpec extends FlatSpec with Matchers with LoneElement with MockitoSugar {
  trait Ctx {
    val settings = new MapSettings()
    val context = SensorContextTester.create(Paths.get("./"))
    val fs = new DefaultFileSystem(Paths.get("./"))
    val parser = new JUnitReportParserAPI {
      def parse(tests: List[Path], directories: List[File]): Map[InputFile, JUnitReport] = ???
    }

    def sensor(
      settings: MapSettings        = settings,
      fs: DefaultFileSystem        = fs,
      parser: JUnitReportParserAPI = parser
    ): JUnitSensor = {
      context.setFileSystem(fs)
      context.setSettings(settings)
      new JUnitSensor(settings.asConfig(), fs, parser)
    }
  }

  it should "read the 'disable' property" in {
    val confDisabled = new MapSettings()
      .setProperty("sonar.junit.disable", "true")
      .asConfig()
    JUnitSensor.shouldEnableSensor(confDisabled) shouldBe false

    val confEnabled = new MapSettings().asConfig()
    JUnitSensor.shouldEnableSensor(confEnabled) shouldBe true
  }

  it should "get the test paths" in {
    val conf = new MapSettings()
      .setProperty("sonar.tests", "test/path1, test/path2")
      .asConfig()
    JUnitSensor.testPaths(conf) shouldBe List(
      Paths.get("test/path1"),
      Paths.get("test/path2")
    )

    val empty = new MapSettings().asConfig()
    JUnitSensor.testPaths(empty) shouldBe List(Paths.get("src/test/scala"))
  }

  it should "get report paths" in {
    val conf = new MapSettings()
      .setProperty("sonar.junit.reportPaths", "test/path1, test/path2")
      .asConfig()
    JUnitSensor.reportPaths(conf) shouldBe List(
      Paths.get("test/path1"),
      Paths.get("test/path2")
    )

    val empty = new MapSettings().asConfig()
    JUnitSensor.reportPaths(empty) shouldBe List(Paths.get("target/test-reports"))
  }

  it should "correctly set its descriptor" in new Ctx {
    val descriptor = new DefaultSensorDescriptor
    sensor().describe(descriptor)

    descriptor.isGlobal shouldBe false
    descriptor.name shouldBe "Scala JUnit Sensor"
    descriptor.`type` shouldBe InputFile.Type.TEST
    descriptor.languages.loneElement shouldBe "scala"
    descriptor.ruleRepositories shouldBe empty
  }

  it should "execute the sensor if the 'disable' flag wasn't set" in new Ctx {
    val reportParser = mock[JUnitReportParserAPI]
    val junitSensor = sensor(parser = reportParser)

    val descriptor = new DefaultSensorDescriptor
    junitSensor.describe(descriptor)
    descriptor.configurationPredicate.test(context.config) shouldBe true

    when(reportParser.parse(any(), any()))
      .thenReturn(Map.empty[InputFile, JUnitReport])

    junitSensor.execute(context)
    verify(reportParser).parse(any(), any())
  }

  it should "respect the 'disable' config property and and skip sonar execution if set to true" in new Ctx {
    val conf = new MapSettings().setProperty("sonar.junit.disable", "true")
    val junitSensor = sensor(settings = conf)

    val descriptor = new DefaultSensorDescriptor
    junitSensor.describe(descriptor)
    descriptor.configurationPredicate.test(context.config) shouldBe false
  }

  it should "save test metrics for each input file" in new Ctx {
    // TODO: test save in isolation
  }

  it should "save test metrics for all the parsed reports" in new Ctx {
    // TODO: test that executing the sensor saves metrics for the given reports.
  }
}
