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
package com.mwz.sonar.scala.scapegoat

import org.scalatest.{FlatSpec, Matchers}

import java.nio.file.Paths

/** Tests the correct behavior of the Scapegoat XML reports parser */
class ScapegoatReportParserSpec extends FlatSpec with Matchers {
  val scapegoatReportParser = new ScapegoatReportParser {}
  behavior of "the Scapegoat XML Report Parser"

  it should "be able to parse an empty report" in {
    val scapegoatReportPath = Paths.get("src", "test", "resources", "scapegoat", "no-warnings.xml")
    val scapegoatWarnings = scapegoatReportParser.parse(scapegoatReportPath)

    scapegoatWarnings shouldBe empty
  }

  it should "be able to parse a report with only one warning" in {
    val scapegoatReportPath = Paths.get("src", "test", "resources", "scapegoat", "one-file-one-warning.xml")
    val scapegoatWarnings = scapegoatReportParser.parse(scapegoatReportPath)

    scapegoatWarnings shouldBe Map(
      "com/mwz/sonar/scala/scapegoat/TestFileA.scala" ->
      Seq(
        ScapegoatIssue(
          line = 15,
          text = "Empty case class",
          snippet = "Empty case class can be rewritten as a case object",
          file = "com/mwz/sonar/scala/scapegoat/TestFileA.scala",
          inspectionId = "com.sksamuel.scapegoat.inspections.EmptyCaseClass"
        )
      )
    )
  }

  it should "be able to parse a report with two warnings in the same file" in {
    val scapegoatReportPath = Paths.get("src", "test", "resources", "scapegoat", "one-file-two-warnings.xml")
    val scapegoatWarnings = scapegoatReportParser.parse(scapegoatReportPath)

    scapegoatWarnings shouldBe Map(
      "com/mwz/sonar/scala/scapegoat/TestFileA.scala" ->
      Seq(
        ScapegoatIssue(
          line = 15,
          text = "Empty case class",
          snippet = "Empty case class can be rewritten as a case object",
          file = "com/mwz/sonar/scala/scapegoat/TestFileA.scala",
          inspectionId = "com.sksamuel.scapegoat.inspections.EmptyCaseClass"
        ),
        ScapegoatIssue(
          line = 20,
          text = "Array passed to String.format",
          snippet = "scala.Predef.augmentString(\"data is: %s\").format(scala.Array.apply(1, 2, 3))",
          file = "com/mwz/sonar/scala/scapegoat/TestFileA.scala",
          inspectionId = "com.sksamuel.scapegoat.inspections.string.ArraysInFormat"
        ),
      )
    )
  }

  it should "be able to parse a report with five warnings in two different files" in {
    val scapegoatReportPath =
      Paths.get("src", "test", "resources", "scapegoat", "two-files-five-warnings.xml")
    val scapegoatWarnings = scapegoatReportParser.parse(scapegoatReportPath)

    scapegoatWarnings shouldBe Map(
      "com/mwz/sonar/scala/scapegoat/TestFileA.scala" ->
      Seq(
        ScapegoatIssue(
          line = 15,
          text = "Empty case class",
          snippet = "Empty case class can be rewritten as a case object",
          file = "com/mwz/sonar/scala/scapegoat/TestFileA.scala",
          inspectionId = "com.sksamuel.scapegoat.inspections.EmptyCaseClass"
        ),
        ScapegoatIssue(
          line = 20,
          text = "Array passed to String.format",
          snippet = "scala.Predef.augmentString(\"data is: %s\").format(scala.Array.apply(1, 2, 3))",
          file = "com/mwz/sonar/scala/scapegoat/TestFileA.scala",
          inspectionId = "com.sksamuel.scapegoat.inspections.string.ArraysInFormat"
        ),
      ),
      "com/mwz/sonar/scala/scapegoat/TestFileB.scala" ->
      Seq(
        ScapegoatIssue(
          line = 30,
          text = "Lonely sealed trait",
          snippet = "Sealed trait NotUsed has no implementing classes",
          file = "com/mwz/sonar/scala/scapegoat/TestFileB.scala",
          inspectionId = "com.sksamuel.scapegoat.inspections.LonelySealedTrait"
        ),
        ScapegoatIssue(
          line = 45,
          text = "Redundant final modifier on method",
          snippet =
            "com.mwz.sonar.scala.scapegoat.TestFileB.testMethod cannot be overridden, final modifier is redundant",
          file = "com/mwz/sonar/scala/scapegoat/TestFileB.scala",
          inspectionId = "com.sksamuel.scapegoat.inspections.RedundantFinalModifierOnMethod"
        ),
        ScapegoatIssue(
          line = 50,
          text = "Empty case class",
          snippet = "Empty case class can be rewritten as a case object",
          file = "com/mwz/sonar/scala/scapegoat/TestFileB.scala",
          inspectionId = "com.sksamuel.scapegoat.inspections.EmptyCaseClass"
        ),
      )
    )
  }
}
