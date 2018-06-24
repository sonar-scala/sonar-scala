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
package scoverage

import java.nio.file.{Path, Paths}

import com.mwz.sonar.scala.util.PathUtils
import org.scalatest.{FlatSpec, LoneElement, Matchers}
import org.sonar.api.batch.fs.InputFile
import org.sonar.api.batch.fs.internal.{DefaultFileSystem, TestInputFileBuilder}
import org.sonar.api.batch.sensor.internal.{DefaultSensorDescriptor, SensorContextTester}
import org.sonar.api.config.internal.MapSettings

/** Tests the Scoverage Sensor */
class ScoverageSensorSpec extends FlatSpec with SensorContextMatchers with LoneElement with Matchers {
  val scoverageReportParser = new TestScoverageReportParser()
  val scoverageSensor = new ScoverageSensor(scoverageReportParser)

  behavior of "Scoverage Sensor"

  it should "correctly set descriptor" in {
    val descriptor = new DefaultSensorDescriptor
    scoverageSensor.describe(descriptor)

    descriptor.name shouldBe "Scoverage Sensor"
    descriptor.languages.loneElement shouldBe "scala"
    descriptor.`type` shouldBe InputFile.Type.MAIN
  }

  it should "correctly save component scoverage" in {
    val cwd = PathUtils.cwd
    val context = SensorContextTester.create(cwd)
    val scoverage = Scoverage(123, 15, 88.72, 14.17)
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
    context should have(metric[java.lang.Double](fileKey, "sonar-scala-scoverage-branch-scoverage", 14.17))
  }

  it should "return all scala files from the module" in {
    val cwd = PathUtils.cwd
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

    val files = scoverageSensor.getModuleSourceFiles(context.fileSystem)
    files.toSeq should contain theSameElementsAs Seq(mainFile, otherFile)
  }

  it should "get module base directory" in {
    val cwd = PathUtils.cwd

    scoverageSensor.getModuleBaseDirectory(new DefaultFileSystem(cwd)) shouldBe Paths.get("")
    scoverageSensor.getModuleBaseDirectory(
      new DefaultFileSystem(cwd.resolve("module"))
    ) shouldBe Paths.get("module")
  }

  it should "get default scoverage report path" in {
    val path = scoverageSensor.getScoverageReportPath(new MapSettings().asConfig())
    path shouldBe Paths.get(s"target/scala-2.11/scoverage-report/scoverage.xml")

    val path2 = scoverageSensor.getScoverageReportPath(
      new MapSettings().setProperty("sonar.scala.version", "2.12.6").asConfig()
    )
    path2 shouldBe Paths.get(s"target/scala-2.12/scoverage-report/scoverage.xml")
  }

  it should "get scoverage report path set in sonar properties" in {
    val deprecated = scoverageSensor.getScoverageReportPath(
      new MapSettings().setProperty("sonar.scoverage.reportPath", "target/report-path").asConfig()
    )
    deprecated shouldBe Paths.get(s"target/report-path")

    val current = scoverageSensor.getScoverageReportPath(
      new MapSettings().setProperty("sonar.scala.scoverage.reportPath", "target/report-path").asConfig()
    )
    current shouldBe Paths.get(s"target/report-path")
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

    // validate the module scoverage metrics
    val moduleKey = context.module.key
    context should have(metric[java.lang.Integer](moduleKey, "sonar-scala-scoverage-total-statements", 5))
    context should have(metric[java.lang.Integer](moduleKey, "sonar-scala-scoverage-covered-statements", 3))
    context should have(metric[java.lang.Double](moduleKey, "sonar-scala-scoverage-statement-coverage", 60.0))
    context should have(metric[java.lang.Double](moduleKey, "sonar-scala-scoverage-branch-scoverage", 100.0))

    // validate the main file scoverage metrics
    val fileKey = mainFile.key
    context should have(metric[java.lang.Integer](fileKey, "sonar-scala-scoverage-total-statements", 5))
    context should have(metric[java.lang.Integer](fileKey, "sonar-scala-scoverage-covered-statements", 3))
    context should have(metric[java.lang.Double](fileKey, "sonar-scala-scoverage-statement-coverage", 60.0))
    context should have(metric[java.lang.Double](fileKey, "sonar-scala-scoverage-branch-scoverage", 100.0))

    // validate the main file line coverage
    context should have(lineHits(fileKey, 5, 1))
    context should have(lineHits(fileKey, 6, 1))
    context should have(lineHits(fileKey, 7, 0))
    context should have(lineHits(fileKey, 8, 0))
    context should have(lineHits(fileKey, 9, 1))
  }

  it should "handle correctly absolute source paths" in {
    // prepare the sensor context
    val cwd = PathUtils.cwd
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

    //validate the module scoverage metrics
    val moduleKey = context.module.key
    context should have(metric[java.lang.Integer](moduleKey, "sonar-scala-scoverage-total-statements", 5))
    context should have(metric[java.lang.Integer](moduleKey, "sonar-scala-scoverage-covered-statements", 3))
    context should have(metric[java.lang.Double](moduleKey, "sonar-scala-scoverage-statement-coverage", 60.0))
    context should have(metric[java.lang.Double](moduleKey, "sonar-scala-scoverage-branch-scoverage", 100.0))
  }
}

/** Mock of the ScoverageReportParser */
final class TestScoverageReportParser extends ScoverageReportParserAPI {
  override def parse(reportPath: Path, modulePath: Path, sourcePrefixes: List[Path]): ModuleCoverage =
    reportPath.toString match {
      case "target/scala-2.11/scoverage-report/scoverage.xml"
          if sourcePrefixes.contains(Paths.get("src/main/scala")) =>
        ModuleCoverage(
          moduleScoverage = Scoverage(
            totalStatements = 5,
            coveredStatements = 3,
            statementCoverage = 60.0,
            branchCoverage = 100.0
          ),
          filesCoverage = Map(
            "src/main/scala/package/Main.scala" ->
            FileCoverage(
              fileScoverage = Scoverage(
                totalStatements = 5,
                coveredStatements = 3,
                statementCoverage = 60.0,
                branchCoverage = 100.0
              ),
              linesCoverage = Map(5 -> 1, 6 -> 1, 7 -> 0, 8 -> 0, 9 -> 1)
            )
          )
        )
    }
}
