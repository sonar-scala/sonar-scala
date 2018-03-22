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

import com.mwz.sonar.scala.SensorContextMatchers
import java.nio.file.Paths
import org.scalatest.{FlatSpec, LoneElement, Matchers}
import org.sonar.api.batch.fs.InputFile
import org.sonar.api.batch.fs.internal.TestInputFileBuilder
import org.sonar.api.batch.sensor.internal.{DefaultSensorDescriptor, SensorContextTester}

/** Tests the Scoverage Sensor */
class ScoverageSensorSpec extends FlatSpec with SensorContextMatchers with LoneElement with Matchers {
  val scoverageSensor = new ScoverageSensorInternal with TestScoverageReportParser
  behavior of "A ScoverageSensor"

  it should "correctly set descriptor" in {
    val descriptor = new DefaultSensorDescriptor
    scoverageSensor.describe(descriptor)

    descriptor.name() shouldBe "Scoverage Sensor"
    descriptor.languages().loneElement shouldBe "scala"
    descriptor.`type`() shouldBe InputFile.Type.MAIN
  }

  it should "save the coverage metrics of a one file module" in {
    //prepare the sensor context
    val context = SensorContextTester.create(Paths.get("./"))
    val mainFile = TestInputFileBuilder
      .create("", "src/main/scala/package/Main.scala")
      .setLanguage("scala")
      .setLines(10)
      .setType(InputFile.Type.MAIN)
      .build()
    context.fileSystem().add(mainFile)

    //execute the sensor
    scoverageSensor.execute(context)

    //validate the module scoverage metrics
    val moduleKey = context.module().key()
    context should have(metric[java.lang.Integer](moduleKey, "total_statements", 5))
    context should have(metric[java.lang.Integer](moduleKey, "covered_statements", 3))
    context should have(metric[java.lang.Double](moduleKey, "scoverage", 60.0))
    context should have(metric[java.lang.Double](moduleKey, "branch_scoverage", 100.0))

    //validate the main file scoverage metrics
    val mainFileKey = mainFile.key()
    context should have(metric[java.lang.Integer](mainFileKey, "total_statements", 5))
    context should have(metric[java.lang.Integer](mainFileKey, "covered_statements", 3))
    context should have(metric[java.lang.Double](mainFileKey, "scoverage", 60.0))
    context should have(metric[java.lang.Double](mainFileKey, "branch_scoverage", 100.0))

    //validate the main file line coverage
    context should have(lineHits(mainFileKey, 5, 1))
    context should have(lineHits(mainFileKey, 6, 1))
    context should have(lineHits(mainFileKey, 7, 0))
    context should have(lineHits(mainFileKey, 8, 0))
    context should have(lineHits(mainFileKey, 9, 1))
  }
}

/** Mock of the ScoverageReportParser */
trait TestScoverageReportParser extends ScoverageReportParserAPI {
  override def parse(reportFilename: String): ModuleCoverage = reportFilename match {
    case "target/scala-2.11/scoverage-report/scoverage.xml" =>
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
            linesCoverage = Map(
              5 -> 1,
              6 -> 1,
              7 -> 0,
              8 -> 0,
              9 -> 1
            )
          )
        )
      )
  }
}
