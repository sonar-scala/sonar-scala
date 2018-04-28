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

import java.nio.file.Paths

import org.scalatest.{FlatSpec, Inside, LoneElement, Matchers}

/** Tests the correct behavior of the Scoverage XML reports parser */
class ScoverageReportParserSpec extends FlatSpec with Inside with LoneElement with Matchers {
  val scalaSources = List(Paths.get("src/main/scala"))
  val scoverageReportParser: ScoverageReportParser = new ScoverageReportParser {}

  behavior of "A Scoverage Report Parser"

  it should "be able to parse the report of an empty project" in {
    val reportFilename = Paths.get("src/test/resources/empty-project-scoverage.xml")
    val moduleCoverage = scoverageReportParser.parse(reportFilename, scalaSources)

    val scoverage = Scoverage(
      totalStatements = 0,
      coveredStatements = 0,
      statementCoverage = 0.0,
      branchCoverage = 0.0
    )
    val expected = ModuleCoverage(
      moduleScoverage = scoverage,
      filesCoverage = Map.empty
    )

    moduleCoverage shouldBe expected
  }

  it should "be able to parse the report of a one file project" in {
    val reportFilename = Paths.get("src/test/resources/one-file-project-scoverage.xml")
    val moduleCoverage = scoverageReportParser.parse(reportFilename, scalaSources)

    val scoverage = Scoverage(
      totalStatements = 2,
      coveredStatements = 2,
      statementCoverage = 100.0,
      branchCoverage = 100.0
    )
    val expected = ModuleCoverage(
      moduleScoverage = scoverage,
      filesCoverage = Map(
        "src/main/scala/com/mwz/sonar/scala/ScalaPlugin.scala" -> FileCoverage(
          fileScoverage = scoverage,
          linesCoverage = Map(66 -> 2)
        )
      )
    )

    moduleCoverage shouldBe expected
  }

  it should "be able to handle multiple source prefixes" in {
    val reportFilename = Paths.get("src/test/resources/one-file-project-scoverage.xml")
    val sourcePrefixes = List(
      Paths.get("src/main/java"),
      Paths.get("src/main/scala"),
      Paths.get("imaginary/sources")
    )
    val moduleCoverage = scoverageReportParser.parse(reportFilename, sourcePrefixes)

    val scoverage = Scoverage(
      totalStatements = 2,
      coveredStatements = 2,
      statementCoverage = 100.0,
      branchCoverage = 100.0
    )
    val expected = ModuleCoverage(
      moduleScoverage = scoverage,
      filesCoverage = Map(
        "src/main/scala/com/mwz/sonar/scala/ScalaPlugin.scala" -> FileCoverage(
          fileScoverage = scoverage,
          linesCoverage = Map(66 -> 2)
        )
      )
    )

    moduleCoverage shouldBe expected
  }

  it should "be able to merge the coverage metric of all classes of the same file" in {
    val reportFilename = Paths.get("src/test/resources/multi-class-one-file-project-scoverage.xml")
    val moduleCoverage = scoverageReportParser.parse(reportFilename, scalaSources)

    val scoverage = Scoverage(
      totalStatements = 7,
      coveredStatements = 5,
      statementCoverage = 71.43,
      branchCoverage = 87.5
    )
    val expected = ModuleCoverage(
      moduleScoverage = scoverage,
      filesCoverage = Map(
        "src/main/scala/com/mwz/sonar/scala/ScalaPlugin.scala" -> FileCoverage(
          fileScoverage = scoverage,
          linesCoverage = Map(38 -> 1, 39 -> 1, 40 -> 0, 56 -> 1, 57 -> 0, 66 -> 2)
        )
      )
    )

    moduleCoverage shouldBe expected
  }

  it should "be able to parse the report of a two files project" in {
    val reportFilename = Paths.get("src/test/resources/two-files-project-scoverage.xml")
    val moduleCoverage = scoverageReportParser.parse(reportFilename, scalaSources)

    val scoverageTotal = Scoverage(
      totalStatements = 6,
      coveredStatements = 5,
      statementCoverage = 83.33,
      branchCoverage = 83.33
    )
    val scoverage1 = Scoverage(
      totalStatements = 2,
      coveredStatements = 2,
      statementCoverage = 100.0,
      branchCoverage = 100.0
    )
    val scoverage2 = Scoverage(
      totalStatements = 4,
      coveredStatements = 3,
      statementCoverage = 75.0,
      branchCoverage = 66.66
    )

    val expected = ModuleCoverage(
      moduleScoverage = scoverageTotal,
      filesCoverage = Map(
        "src/main/scala/com/mwz/sonar/scala/ScalaPlugin.scala" -> FileCoverage(
          fileScoverage = scoverage1,
          linesCoverage = Map(66 -> 2)
        ),
        "src/main/scala/com/mwz/sonar/scala/sensor/ScalaSensor.scala" -> FileCoverage(
          fileScoverage = scoverage2,
          linesCoverage = Map(20 -> 1, 21 -> 0, 22 -> 1, 23 -> 1)
        )
      )
    )

    moduleCoverage shouldBe expected
  }
}
