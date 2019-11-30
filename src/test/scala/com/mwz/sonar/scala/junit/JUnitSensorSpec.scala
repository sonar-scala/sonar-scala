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
package junit

import java.io.File
import java.nio.file.{Path, Paths}

import org.mockito.{ArgumentMatchersSugar, MockitoSugar}
import org.scalatest.LoneElement
import org.sonar.api.batch.fs.InputFile
import org.sonar.api.batch.fs.internal.{DefaultFileSystem, TestInputFileBuilder}
import org.sonar.api.batch.sensor.internal.{DefaultSensorDescriptor, SensorContextTester}
import org.sonar.api.config.internal.MapSettings
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class JUnitSensorSpec
    extends AnyFlatSpec
    with Matchers
    with LoneElement
    with MockitoSugar
    with ArgumentMatchersSugar
    with SensorContextMatchers {
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

  it should "save test metrics for each input file" in new Ctx {
    val junitSensor = sensor()
    val testFile = TestInputFileBuilder
      .create("", "TestFile.scala")
      .build()
    val report = JUnitReport("TestFile", 5, 3, 2, 1, 0.123f)
    val reports: Map[InputFile, JUnitReport] = Map(testFile -> report)
    context.fileSystem.add(testFile)
    junitSensor.save(context, reports)

    context should have(metric[Integer](testFile.key, "skipped_tests", 1))
    context should have(metric[Integer](testFile.key, "tests", 4))
    context should have(metric[Integer](testFile.key, "test_errors", 3))
    context should have(metric[Integer](testFile.key, "test_failures", 2))
    context should have(metric[java.lang.Long](testFile.key, "test_execution_time", 123L))
  }

  it should "save test metrics for all the parsed reports" in new Ctx {
    val conf = new MapSettings()
      .setProperty("sonar.tests", "tests")
    val reportParser = mock[JUnitReportParserAPI]
    val junitSensor = sensor(settings = conf, parser = reportParser)

    val testFile = TestInputFileBuilder
      .create("", "tests/TestFile.scala")
      .setLanguage("scala")
      .setType(InputFile.Type.TEST)
      .build()
    val report = JUnitReport("TestFile", 5, 3, 2, 1, 0.123f)
    val reports: Map[InputFile, JUnitReport] = Map(testFile -> report)
    context.fileSystem.add(testFile)

    when(reportParser.parse(*, *))
      .thenReturn(reports)
    junitSensor.execute(context)

    context should have(metric[Integer](testFile.key, "skipped_tests", 1))
    context should have(metric[Integer](testFile.key, "tests", 4))
    context should have(metric[Integer](testFile.key, "test_errors", 3))
    context should have(metric[Integer](testFile.key, "test_failures", 2))
    context should have(metric[java.lang.Long](testFile.key, "test_execution_time", 123L))
  }

  it should "save test metrics for a module" in new Ctx {
    val conf = new MapSettings()
      .setProperty("sonar.tests", "tests")
    val reportParser = mock[JUnitReportParserAPI]
    val junitSensor = sensor(settings = conf, parser = reportParser)

    val testFile = TestInputFileBuilder
      .create("module1", "tests/TestFile.scala")
      .setLanguage("scala")
      .setType(InputFile.Type.TEST)
      .build()
    val report = JUnitReport("TestFile", 5, 3, 2, 1, 0.123f)
    val reports: Map[InputFile, JUnitReport] = Map(testFile -> report)
    context.fileSystem.add(testFile)

    when(reportParser.parse(*, *))
      .thenReturn(reports)
    junitSensor.execute(context)

    context should have(metric[Integer](testFile.key, "skipped_tests", 1))
    context should have(metric[Integer](testFile.key, "tests", 4))
    context should have(metric[Integer](testFile.key, "test_errors", 3))
    context should have(metric[Integer](testFile.key, "test_failures", 2))
    context should have(metric[java.lang.Long](testFile.key, "test_execution_time", 123L))
  }
}
