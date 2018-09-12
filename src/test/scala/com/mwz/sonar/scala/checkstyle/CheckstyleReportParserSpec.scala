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
package com.mwz.sonar.scala.checkstyle

import java.nio.file.Paths

import org.scalatest.FlatSpec
import org.scalatest.Matchers

/** Tests the correct behavior of the Scapegoat XML reports parser */
class CheckstyleReportParserSpec extends FlatSpec with Matchers {
  val checkstyleReportParser = new CheckstyleReportParser()

  behavior of "the Checkstyle XML Report Parser"

  it should "be able to parse an empty report" in {
    val checkstyleReportPath = Paths.get("src", "test", "resources", "scalastyle", "no-warnings.xml")
    val checkstyleErrors = checkstyleReportParser.parse(checkstyleReportPath)

    checkstyleErrors shouldBe empty
  }

  it should "be able to parse a report with only one warning" in {
    val scapegoatReportPath = Paths.get("src", "test", "resources", "scalastyle", "one-file-one-warning.xml")
    val scapegoatWarnings = checkstyleReportParser.parse(scapegoatReportPath)

    scapegoatWarnings shouldBe Map(
      "com/mwz/sonar/scala/scalastyle/TestFileA.scala" -> Seq(
        CheckstyleIssue(
          line = 15,
          column = Some(15),
          severity = "warning",
          message = "Cyclomatic complexity of 11 exceeds max of 10",
          inspectionClass = "org.scalastyle.scalariform.CyclomaticComplexityChecker"
        )
      )
    )
  }

  it should "be able to parse a report with five warnings in two different files" in {
    val checkstyleReportPath =
      Paths.get("src", "test", "resources", "scalastyle", "two-files-five-warnings.xml")
    val checkstyleErrors = checkstyleReportParser.parse(checkstyleReportPath)

    checkstyleErrors shouldBe Map(
      "com/mwz/sonar/scala/scalastyle/TestFileA.scala" -> Seq(
        CheckstyleIssue(
          line = 39,
          column = Some(37),
          severity = "warning",
          message = "Magic Number",
          inspectionClass = "org.scalastyle.scalariform.MagicNumberChecker"
        ),
        CheckstyleIssue(
          line = 77,
          column = None,
          severity = "warning",
          message = "File line length exceeds 160 characters",
          inspectionClass = "org.scalastyle.file.FileLineLengthChecker"
        )
      ),
      "com/mwz/sonar/scala/scalastyle/TestFileB.scala" -> Seq(
        CheckstyleIssue(
          line = 1,
          column = None,
          severity = "warning",
          message = "Header does not match expected text",
          inspectionClass = "org.scalastyle.file.HeaderMatchesChecker"
        ),
        CheckstyleIssue(
          line = 20,
          column = Some(8),
          severity = "warning",
          message = "Public method must have explicit type",
          inspectionClass = "org.scalastyle.scalariform.PublicMethodsHaveTypeChecker"
        ),
        CheckstyleIssue(
          line = 22,
          column = Some(16),
          severity = "warning",
          message = "Field name does not match the regular expression '^[A-Z][A-Za-z]*$'",
          inspectionClass = "org.scalastyle.scalariform.FieldNamesChecker"
        )
      )
    )
  }
}
