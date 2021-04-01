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

package com.mwz.sonar.scala.metadata
package scalastyle

import cats.data.Chain
import org.scalastyle.BooleanType
import org.scalastyle.ErrorLevel
import org.scalastyle.InfoLevel
import org.scalastyle.IntegerType
import org.scalastyle.StringType
import org.scalastyle.WarningLevel
import org.scalatest.Inspectors
import org.scalatest.LoneElement
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class ScalastyleRulesSpec extends AnyFlatSpec with Matchers with Inspectors with LoneElement {

  it should "convert Scalastyle inspection to a Rule" in {
    val inspection = ScalastyleInspection(
      clazz = "class",
      id = "id",
      label = "label",
      description = "description",
      extraDescription = None,
      justification = None,
      defaultLevel = InfoLevel,
      params = List(
        ScalastyleParam(
          name = "paramName",
          typ = IntegerType,
          label = "label",
          description = "desc",
          default = "123"
        )
      )
    )

    val expected = Rule(
      key = "class",
      name = "label",
      mdDescription = "*description*",
      sonarMdDescription = "*description*",
      severity = Severity.Info,
      template = true,
      params = Chain(
        Param(
          name = "paramName",
          typ = ParamType.Integer,
          description = "label: desc",
          default = "123"
        )
      )
    )

    ScalastyleRules.toRule(inspection) shouldBe expected
  }

  it should "compose the full description from description, justification and extraDescription fields" in {
    val inspection1 = ScalastyleInspection(
      clazz = "class",
      id = "id",
      label = "label",
      description = "description",
      extraDescription = None,
      justification = None,
      defaultLevel = InfoLevel,
      params = Seq.empty
    )

    val inspection2 = ScalastyleInspection(
      clazz = "class",
      id = "id",
      label = "label",
      description = "description",
      extraDescription = Some("extraDescription"),
      justification = None,
      defaultLevel = InfoLevel,
      params = Seq.empty
    )

    val inspection3 = ScalastyleInspection(
      clazz = "class",
      id = "id",
      label = "label",
      description = "description",
      extraDescription = Some("extraDescription"),
      justification = Some("justification"),
      defaultLevel = InfoLevel,
      params = Seq.empty
    )

    ScalastyleRules.mdDescription(inspection1) shouldBe "*description*"
    ScalastyleRules.mdDescription(inspection2) shouldBe "*description*\n\nextraDescription"
    ScalastyleRules.mdDescription(inspection3) shouldBe "*description*\n\njustification\n\nextraDescription"
  }

  it should "correctly reformat simple text " in {
    val text =
      """
        |line1. next
        |line2
        |
        |another line
        |""".stripMargin.stripPrefix("\n").stripLineEnd

    val expected =
      """
        |line1. next
        |line2
        |another line
        |""".stripMargin.stripPrefix("\n").stripLineEnd

    ScalastyleRules.format(text) shouldBe expected
  }

  it should "correctly reformat text with some inline code blocks" in {
    val text =
      """
        |line1. next
        |line2
        |
        |line3 `code` next `code 2`
        |""".stripMargin.stripPrefix("\n").stripLineEnd

    val expected =
      """
        |line1. next
        |line2
        |line3 ``code`` next ``code 2``
        |""".stripMargin.stripPrefix("\n").stripLineEnd

    ScalastyleRules.format(text) shouldBe expected
  }

  it should "correctly reformat text with some code blocks" in {
    val text =
      """
        |line1. next
        |line 2.
        |
        |```scala
        |code block 1
        |```
        |
        |line 3.
        |
        |```scala
        |code block 2
        |  code block 2
        |```
        |
        |line 4. `inline` and `inline 2`.
        |
        |```scala
        |code block 3
        |
        |  code block 3
        |```
        |""".stripMargin.stripPrefix("\n").stripLineEnd

    val expected =
      """
        |line1. next
        |line 2.
        |``
        |code block 1
        |`` line 3.
        |``
        |code block 2
        |  code block 2
        |`` line 4. ``inline`` and ``inline 2``.
        |``
        |code block 3
        |  code block 3
        |``
        |""".stripMargin.stripPrefix("\n").stripLineEnd

    ScalastyleRules.format(text) shouldBe expected
  }

  it should "strip out backslashes from the text" in {
    val text =
      """
        |line1. \_
        |line2 \[T\]
        |""".stripMargin.stripPrefix("\n").stripLineEnd

    val expected =
      """
        |line1. _
        |line2 [T]
        |""".stripMargin.stripPrefix("\n").stripLineEnd

    ScalastyleRules.format(text) shouldBe expected
  }

  it should "convert Scalastyle inspection level to SonarQube Severity" in {
    ScalastyleRules.toSeverity(InfoLevel) shouldBe Severity.Info
    ScalastyleRules.toSeverity(WarningLevel) shouldBe Severity.Minor
    ScalastyleRules.toSeverity(ErrorLevel) shouldBe Severity.Major
  }

  it should "convert Scalastyle parameter" in {}

  it should "convert Scalastyle parameter type" in {
    ScalastyleRules.toParamType(
      "org.scalastyle.file.HeaderMatchesChecker",
      "header",
      StringType
    ) shouldBe ParamType.Text
    ScalastyleRules.toParamType("", "", StringType) shouldBe ParamType.String
    ScalastyleRules.toParamType("", "", IntegerType) shouldBe ParamType.Integer
    ScalastyleRules.toParamType("", "", BooleanType) shouldBe ParamType.Boolean
  }
}
