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

import org.scalatest.{FlatSpec, Inside, LoneElement, Matchers}

/**
 *  Tests the correct behavior of the Scoverage XML reports parser
 *
 *  @author BalmungSan
 */
class ScoverageReportParserSpec extends FlatSpec with Inside with LoneElement with Matchers {
  val scoverageReportParser = new ScoverageReportParser()
  behavior of "A Scoverage Report Parser"

  it should "be able to parse the report of an empty project" in {
    val reportFilename = "src/test/resources/empty-project-scoverage.xml"
    val moduleCoverage = scoverageReportParser.parse(reportFilename)
    inside(moduleCoverage) {
      case ModuleCoverage(moduleScoverage, files) =>
        inside(moduleScoverage) {
          case Scoverage(totalStatements, coveredStatements, statementCoverage, branchCoverage) =>
            totalStatements shouldBe 0
            coveredStatements shouldBe 0
            statementCoverage shouldBe 0.0
            branchCoverage shouldBe 0.0
        }
        files shouldBe empty
    }
  }

  it should "be able to parse the report of a one file project" in {
    val reportFilename = "src/test/resources/one-file-project-scoverage.xml"
    val moduleCoverage = scoverageReportParser.parse(reportFilename)
    inside(moduleCoverage) {
      case ModuleCoverage(moduleScoverage, files) =>
        inside(moduleScoverage) {
          case Scoverage(totalStatements, coveredStatements, statementCoverage, branchCoverage) =>
            totalStatements shouldBe 2
            coveredStatements shouldBe 2
            statementCoverage shouldBe 100.0
            branchCoverage shouldBe 100.0
        }
        val (filename, fileCoverage) = files.loneElement
        filename shouldBe "com/mwz/sonar/scala/ScalaPlugin.scala"
        inside(fileCoverage) {
          case FileCoverage(fileScoverage, lines) =>
            inside(fileScoverage) {
              case Scoverage(totalStatements, coveredStatements, statementCoverage, branchCoverage) =>
                totalStatements shouldBe 2
                coveredStatements shouldBe 2
                statementCoverage shouldBe 100.0
                branchCoverage shouldBe 100.0
            }
            lines.loneElement shouldBe (66 -> 2)
        }
    }
  }

  it should "be able to merge the coverage metric of all classes of the same file" in {
    val reportFilename = "src/test/resources/multi-class-one-file-project-scoverage.xml"
    val moduleCoverage = scoverageReportParser.parse(reportFilename)
    inside(moduleCoverage) {
      case ModuleCoverage(moduleScoverage, files) =>
        inside(moduleScoverage) {
          case Scoverage(totalStatements, coveredStatements, statementCoverage, branchCoverage) =>
            totalStatements shouldBe 7
            coveredStatements shouldBe 5
            statementCoverage shouldBe 71.43
            branchCoverage shouldBe 87.5
        }
        val (filename, fileCoverage) = files.loneElement
        filename shouldBe "com/mwz/sonar/scala/ScalaPlugin.scala"
        inside(fileCoverage) {
          case FileCoverage(fileScoverage, lines) =>
            inside(fileScoverage) {
              case Scoverage(totalStatements, coveredStatements, statementCoverage, branchCoverage) =>
                totalStatements shouldBe 7
                coveredStatements shouldBe 5
                statementCoverage shouldBe 71.43
                branchCoverage shouldBe 87.5
            }
            lines should contain theSameElementsAs Map(
              38 -> 1,
              39 -> 1,
              40 -> 0,
              56 -> 1,
              57 -> 0,
              66 -> 2
            )
        }
    }
  }

  it should "be able to parse the report of a two files project" in {
    val reportFilename = "src/test/resources/two-files-project-scoverage.xml"
    val moduleCoverage = scoverageReportParser.parse(reportFilename)
    inside(moduleCoverage) {
      case ModuleCoverage(moduleScoverage, files) =>
        inside(moduleScoverage) {
          case Scoverage(totalStatements, coveredStatements, statementCoverage, branchCoverage) =>
            totalStatements shouldBe 6
            coveredStatements shouldBe 5
            statementCoverage shouldBe 83.33
            branchCoverage shouldBe 83.33
        }
        files should have size 2
        val file1name = "com/mwz/sonar/scala/ScalaPlugin.scala"
        files should contain key file1name
        val file1Coverage = files(file1name)
        inside(file1Coverage) {
          case FileCoverage(fileScoverage, lines) =>
            inside(fileScoverage) {
              case Scoverage(totalStatements, coveredStatements, statementCoverage, branchCoverage) =>
                totalStatements shouldBe 2
                coveredStatements shouldBe 2
                statementCoverage shouldBe 100.0
                branchCoverage shouldBe 100.0
            }
            lines.loneElement shouldBe (66 -> 2)
        }
        val file2name = "com/mwz/sonar/scala/sensor/ScalaSensor.scala"
        files should contain key file2name
        val file2Coverage = files(file2name)
        inside(file2Coverage) {
          case FileCoverage(fileScoverage, lines) =>
            inside(fileScoverage) {
              case Scoverage(totalStatements, coveredStatements, statementCoverage, branchCoverage) =>
                totalStatements shouldBe 4
                coveredStatements shouldBe 3
                statementCoverage shouldBe 75.0
                branchCoverage shouldBe 66.66
            }
            lines should contain theSameElementsAs Map(
              20 -> 1,
              21 -> 0,
              22 -> 1,
              23 -> 1
            )
        }
    }
  }
}
