/*
 * Copyright (C) 2018-2022  All sonar-scala contributors
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
package scoverage

import java.nio.file.Path
import java.nio.file.Paths

import com.mwz.sonar.scala.util.PathUtils._
import org.scalatest.LoneElement
import org.scalatest.flatspec.AnyFlatSpec
import org.sonar.api.batch.fs.InputFile
import org.sonar.api.batch.fs.internal.TestInputFileBuilder
import org.sonar.api.batch.sensor.internal.DefaultSensorDescriptor
import org.sonar.api.batch.sensor.internal.SensorContextTester
import org.sonar.api.config.internal.MapSettings

/** Tests the Scoverage Sensor */
class ScoverageSensorSpec extends AnyFlatSpec with SensorContextMatchers with LoneElement {
  val globalConfig = new GlobalConfig(new MapSettings().asConfig)
  val scoverageReportParser = new TestScoverageReportParser()
  val scoverageSensor = new ScoverageSensor(globalConfig, scoverageReportParser)

  it should "correctly set descriptor" in {
    val descriptor = new DefaultSensorDescriptor
    scoverageSensor.describe(descriptor)

    descriptor.isGlobal shouldBe false
    descriptor.name shouldBe "Scoverage Sensor"
    descriptor.languages.loneElement shouldBe "scala"
    descriptor.`type` shouldBe InputFile.Type.MAIN
  }

  it should "read the 'disable' config property" in {
    val context = SensorContextTester.create(cwd)
    ScoverageSensor.shouldEnableSensor(context.config) shouldBe true

    val context2 = SensorContextTester.create(cwd)
    context2.setSettings(new MapSettings().setProperty("sonar.scala.scoverage.disable", "maybe"))
    ScoverageSensor.shouldEnableSensor(context2.config) shouldBe true

    val context3 = SensorContextTester.create(cwd)
    context3.setSettings(new MapSettings().setProperty("sonar.scala.scoverage.disable", "true"))
    ScoverageSensor.shouldEnableSensor(context3.config) shouldBe false
  }

  it should "execute the sensor if the 'disable' flag wasn't set" in {
    val context = SensorContextTester.create(cwd)
    val descriptor = new DefaultSensorDescriptor
    scoverageSensor.describe(descriptor)
    descriptor.configurationPredicate.test(context.config) shouldBe true
  }

  it should "respect the 'disable' config property and skip scapegoat analysis if set to true" in {
    val context = SensorContextTester.create(cwd)
    context.setSettings(new MapSettings().setProperty("sonar.scala.scoverage.disable", "true"))
    val descriptor = new DefaultSensorDescriptor
    scoverageSensor.describe(descriptor)
    descriptor.configurationPredicate.test(context.config) shouldBe false
  }

  it should "correctly save component scoverage" in {
    val context = SensorContextTester.create(cwd)
    val scoverage = Scoverage(
      statements = 123,
      coveredStatements = 15,
      statementCoverage = 88.72,
      branches = 2,
      coveredBranches = 1,
      branchCoverage = 50.00
    )
    val mainFile = TestInputFileBuilder
      .create("", "src/main/scala/package/Main.scala")
      .setLanguage("scala")
      .setLines(10)
      .setType(InputFile.Type.MAIN)
      .build()

    context.fileSystem.add(mainFile)
    scoverageSensor.saveComponentScoverage(context, mainFile, scoverage)

    // validate the module scoverage metrics
    val fileKey = mainFile.key
    context should have(metric[java.lang.Integer](fileKey, "sonar-scala-scoverage-total-statements", 123))
    context should have(metric[java.lang.Integer](fileKey, "sonar-scala-scoverage-covered-statements", 15))
    context should have(metric[java.lang.Double](fileKey, "sonar-scala-scoverage-statement-coverage", 88.72))
    context should have(metric[java.lang.Integer](fileKey, "sonar-scala-scoverage-total-branches", 2))
    context should have(metric[java.lang.Integer](fileKey, "sonar-scala-scoverage-covered-branches", 1))
    context should have(metric[java.lang.Double](fileKey, "sonar-scala-scoverage-branch-coverage", 50.00))
  }

  it should "return all scala files from the module" in {
    val context = SensorContextTester.create(cwd)

    context.setSettings(new MapSettings().setProperty("sonar.sources", "src/main/scala"))
    val mainFile = TestInputFileBuilder
      .create("", "src/main/scala/package/Main.scala")
      .setLanguage("scala")
      .setLines(10)
      .setType(InputFile.Type.MAIN)
      .build()
    val otherFile = TestInputFileBuilder
      .create("", "src/main/scala/Other.scala")
      .setLanguage("scala")
      .setLines(10)
      .setType(InputFile.Type.MAIN)
      .build()
    val testFile = TestInputFileBuilder
      .create("", "src/test/scala/Test.scala")
      .setLanguage("scala")
      .setLines(10)
      .setType(InputFile.Type.TEST)
      .build()
    val javaFile = TestInputFileBuilder
      .create("", "src/main/java/Other.java")
      .setLanguage("java")
      .setLines(10)
      .setType(InputFile.Type.MAIN)
      .build()
    context.fileSystem.add(mainFile)
    context.fileSystem.add(otherFile)
    context.fileSystem.add(testFile)
    context.fileSystem.add(javaFile)

    val files = scoverageSensor.getProjectSourceFiles(context.fileSystem)
    files.toSeq should contain theSameElementsAs Seq(mainFile, otherFile)
  }

  it should "get default scoverage report path" in {
    val path = scoverageSensor.getScoverageReportPath(new MapSettings().asConfig())
    path shouldBe Paths.get(s"target/scala-2.13/scoverage-report/scoverage.xml")

    val path2 = scoverageSensor.getScoverageReportPath(
      new MapSettings().setProperty("sonar.scala.version", "2.11.12").asConfig()
    )
    path2 shouldBe Paths.get(s"target/scala-2.11/scoverage-report/scoverage.xml")
  }

  it should "get scoverage report path set in sonar properties" in {
    val path = scoverageSensor.getScoverageReportPath(
      new MapSettings().setProperty("sonar.scala.scoverage.reportPath", "target/report-path").asConfig()
    )
    path shouldBe Paths.get(s"target/report-path")
  }

  it should "save the coverage metrics of a one file module" in {
    // prepare the sensor context
    val context = SensorContextTester.create(Paths.get("./"))
    context.setSettings(new MapSettings().setProperty("sonar.sources", "src/main/scala"))
    val mainFile = TestInputFileBuilder
      .create("", "src/main/scala/package/Main.scala")
      .setLanguage("scala")
      .setLines(10)
      .setType(InputFile.Type.MAIN)
      .build()
    context.fileSystem().add(mainFile)

    // execute the sensor
    scoverageSensor.execute(context)

    // validate the main file scoverage metrics
    val fileKey = mainFile.key
    context should have(metric[java.lang.Integer](fileKey, "sonar-scala-scoverage-total-statements", 5))
    context should have(metric[java.lang.Integer](fileKey, "sonar-scala-scoverage-covered-statements", 3))
    context should have(metric[java.lang.Double](fileKey, "sonar-scala-scoverage-statement-coverage", 60.0))
    context should have(metric[java.lang.Integer](fileKey, "sonar-scala-scoverage-total-branches", 2))
    context should have(metric[java.lang.Integer](fileKey, "sonar-scala-scoverage-covered-branches", 1))
    context should have(metric[java.lang.Double](fileKey, "sonar-scala-scoverage-branch-coverage", 50.0))

    // validate the main file line coverage
    context should have(lineHits(fileKey, 5, 1))
    context should have(lineHits(fileKey, 6, 1))
    context should have(lineHits(fileKey, 7, 0))
    context should have(lineHits(fileKey, 8, 0))
    context should have(lineHits(fileKey, 9, 1))
  }

  it should "handle correctly absolute source paths" in {
    // prepare the sensor context
    val context = SensorContextTester.create(cwd)
    context.setSettings(
      new MapSettings().setProperty(
        "sonar.sources",
        cwd.resolve("src/main/scala").toString + "," + cwd.resolve("i/dont/exist")
      )
    )
    val mainFile = TestInputFileBuilder
      .create("", "src/main/scala/package/Main.scala")
      .setLanguage("scala")
      .setLines(10)
      .setType(InputFile.Type.MAIN)
      .build()
    context.fileSystem().add(mainFile)

    // execute the sensor
    scoverageSensor.execute(context)

    // validate the main file scoverage metrics
    val fileKey = mainFile.key
    context should have(metric[java.lang.Integer](fileKey, "sonar-scala-scoverage-total-statements", 5))
    context should have(metric[java.lang.Integer](fileKey, "sonar-scala-scoverage-covered-statements", 3))
    context should have(metric[java.lang.Double](fileKey, "sonar-scala-scoverage-statement-coverage", 60.0))
    context should have(metric[java.lang.Integer](fileKey, "sonar-scala-scoverage-total-branches", 2))
    context should have(metric[java.lang.Integer](fileKey, "sonar-scala-scoverage-covered-branches", 1))
    context should have(metric[java.lang.Double](fileKey, "sonar-scala-scoverage-branch-coverage", 50.0))
  }
}

/** Mock of the ScoverageReportParser */
final class TestScoverageReportParser extends ScoverageReportParserAPI {
  override def parse(reportPath: Path, modulePath: Path, sourcePrefixes: List[Path]): ProjectCoverage =
    reportPath.toString match {
      case "target/scala-2.13/scoverage-report/scoverage.xml"
          if sourcePrefixes.contains(Paths.get("src/main/scala")) =>
        ProjectCoverage(
          projectScoverage = Scoverage(
            statements = 5,
            coveredStatements = 3,
            statementCoverage = 60.0,
            branches = 2,
            coveredBranches = 1,
            branchCoverage = 50.0
          ),
          filesCoverage = Map(
            "src/main/scala/package/Main.scala" ->
            FileCoverage(
              fileScoverage = Scoverage(
                statements = 5,
                coveredStatements = 3,
                statementCoverage = 60.0,
                branches = 2,
                coveredBranches = 1,
                branchCoverage = 50.0
              ),
              linesCoverage = Map(5 -> 1, 6 -> 1, 7 -> 0, 8 -> 0, 9 -> 1)
            )
          )
        )
    }
}
