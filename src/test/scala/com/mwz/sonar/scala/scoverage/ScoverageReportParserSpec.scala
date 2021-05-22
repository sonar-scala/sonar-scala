/*
 * Copyright (C) 2018-2021  All sonar-scala contributors
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

import java.nio.file.Paths

import com.softwaremill.diffx.generic.auto._
import com.softwaremill.diffx.scalatest.DiffMatcher
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

/** Tests the correct behavior of the Scoverage XML reports parser */
class ScoverageReportParserSpec extends AnyFlatSpec with Matchers with DiffMatcher {
  val modulePath = Paths.get("")
  val scalaSources = List(Paths.get("src/main/scala"))
  val scoverageReportParser = new ScoverageReportParser()

  behavior of "A Scoverage XML Report Parser"

  it should "be able to extract scoverage data from XML" in {
    val node =
      <node statement-count="3" statements-invoked="2" statement-rate="66.67" branch-rate="50.00">
        <methods>
          <method>
            <statements>
              <statement branch="false" invocation-count="1" ignored="false"></statement>
              <statement branch="false" invocation-count="1" ignored="false"></statement>
              <statement branch="false" invocation-count="0" ignored="false"></statement>
              <statement branch="true" invocation-count="1" ignored="false"></statement>
              <statement branch="true" invocation-count="1" ignored="true"></statement>
              <statement branch="true" invocation-count="0" ignored="false"></statement>
            </statements>
          </method>
        </methods>
      </node>
    val expected = Scoverage(
      statements = 3,
      coveredStatements = 2,
      statementCoverage = 66.67,
      branches = 2,
      coveredBranches = 1,
      branchCoverage = 50.00
    )

    scoverageReportParser.extractScoverageFromNode(node) shouldBe expected
  }

  it should "be able to parse the report of an empty project" in {
    val reportFilename = Paths.get("src/test/resources/scoverage/empty-project.xml")
    val moduleCoverage = scoverageReportParser.parse(reportFilename, modulePath, scalaSources)

    val scoverage = Scoverage(
      statements = 0,
      coveredStatements = 0,
      statementCoverage = 0.0,
      branches = 0,
      coveredBranches = 0,
      branchCoverage = 0.0
    )
    val expected = ProjectCoverage(
      projectScoverage = scoverage,
      filesCoverage = Map.empty
    )

    moduleCoverage shouldBe expected
  }

  it should "be able to parse the report of a one file project" in {
    val reportFilename = Paths.get("src/test/resources/scoverage/one-file-project.xml")
    val moduleCoverage = scoverageReportParser.parse(reportFilename, modulePath, scalaSources)

    val scoverage = Scoverage(
      statements = 3,
      coveredStatements = 3,
      statementCoverage = 100.0,
      branches = 1,
      coveredBranches = 1,
      branchCoverage = 100.0
    )
    val expected = ProjectCoverage(
      projectScoverage = scoverage,
      filesCoverage = Map(
        "src/main/scala/com/mwz/sonar/scala/ScalaPlugin.scala" -> FileCoverage(
          fileScoverage = scoverage,
          linesCoverage = Map(66 -> 2, 67 -> 1)
        )
      )
    )

    moduleCoverage should matchTo(expected)
  }

  it should "be able to handle multiple source prefixes" in {
    val reportFilename = Paths.get("src/test/resources/scoverage/one-file-project.xml")
    val sourcePrefixes = List(
      Paths.get("src/main/java"),
      Paths.get("src/main/scala"),
      Paths.get("imaginary/sources")
    )
    val moduleCoverage = scoverageReportParser.parse(reportFilename, modulePath, sourcePrefixes)

    val scoverage = Scoverage(
      statements = 3,
      coveredStatements = 3,
      statementCoverage = 100.0,
      branches = 1,
      coveredBranches = 1,
      branchCoverage = 100.0
    )
    val expected = ProjectCoverage(
      projectScoverage = scoverage,
      filesCoverage = Map(
        "src/main/scala/com/mwz/sonar/scala/ScalaPlugin.scala" -> FileCoverage(
          fileScoverage = scoverage,
          linesCoverage = Map(66 -> 2, 67 -> 1)
        )
      )
    )

    moduleCoverage shouldBe expected
  }

  it should "be able to handle correctly file names with source prefixes" in {
    val reportFilename =
      Paths.get("src/test/resources/scoverage/filenames-with-source-prefixes.xml")
    val sourcePrefixes = List(
      Paths.get("src/main/java"),
      Paths.get("src/main/scala"),
      Paths.get("imaginary/sources")
    )
    val moduleCoverage = scoverageReportParser.parse(reportFilename, modulePath, sourcePrefixes)

    val scoverage = Scoverage(
      statements = 2,
      coveredStatements = 2,
      statementCoverage = 100.0,
      branches = 0,
      coveredBranches = 0,
      branchCoverage = 100.0
    )
    val expected = ProjectCoverage(
      projectScoverage = scoverage,
      filesCoverage = Map(
        "src/main/scala/com/mwz/sonar/scala/ScalaPlugin.scala" -> FileCoverage(
          fileScoverage = scoverage,
          linesCoverage = Map(66 -> 2)
        )
      )
    )

    moduleCoverage shouldBe expected
  }

  it should "be able to handle correctly module files with a sources prefix" in {
    val reportFilename =
      Paths.get("src/test/resources/scoverage/filenames-with-source-prefixes2.xml")

    // I'm going to pretend here for convenience that src is a module path
    // and src/main/scala is sources prefix, which doesn't include the module path
    // in the scoverage report: main/scala/com/mwz/sonar/scala/ScalaPlugin.scala.
    val modulePath = Paths.get("src")
    val scalaSources = List(Paths.get("src/main/scala"))

    val moduleCoverage = scoverageReportParser.parse(reportFilename, modulePath, scalaSources)

    val scoverage = Scoverage(
      statements = 2,
      coveredStatements = 2,
      statementCoverage = 100.0,
      branches = 0,
      coveredBranches = 0,
      branchCoverage = 100.0
    )
    val expected = ProjectCoverage(
      projectScoverage = scoverage,
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
    val reportFilename = Paths.get("src/test/resources/scoverage/multi-class-one-file-project.xml")
    val moduleCoverage = scoverageReportParser.parse(reportFilename, modulePath, scalaSources)

    val scoverage = Scoverage(
      statements = 7,
      coveredStatements = 5,
      statementCoverage = 71.43,
      branches = 2,
      coveredBranches = 1,
      branchCoverage = 50.00
    )
    val expected = ProjectCoverage(
      projectScoverage = scoverage,
      filesCoverage = Map(
        "src/main/scala/com/mwz/sonar/scala/ScalaPlugin.scala" -> FileCoverage(
          fileScoverage = scoverage,
          linesCoverage = Map(38 -> 1, 39 -> 1, 40 -> 0, 56 -> 1, 57 -> 0, 66 -> 2)
        )
      )
    )

    moduleCoverage should matchTo(expected)
  }

  it should "be able to parse a report with two files" in {
    val reportFilename = Paths.get("src/test/resources/scoverage/two-files-project.xml")
    val moduleCoverage = scoverageReportParser.parse(reportFilename, modulePath, scalaSources)

    val scoverageTotal = Scoverage(
      statements = 6,
      coveredStatements = 5,
      statementCoverage = 83.33,
      branches = 3,
      coveredBranches = 2,
      branchCoverage = 66.67
    )
    val scoverage1 = Scoverage(
      statements = 2,
      coveredStatements = 2,
      statementCoverage = 100.0,
      branches = 0,
      coveredBranches = 0,
      branchCoverage = 0
    )
    val scoverage2 = Scoverage(
      statements = 4,
      coveredStatements = 3,
      statementCoverage = 75.0,
      branches = 3,
      coveredBranches = 2,
      branchCoverage = 66.67
    )

    val expected = ProjectCoverage(
      projectScoverage = scoverageTotal,
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

    moduleCoverage should matchTo(expected)
  }
}
