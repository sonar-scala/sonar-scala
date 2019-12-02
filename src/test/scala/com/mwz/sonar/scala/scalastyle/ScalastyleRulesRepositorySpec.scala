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
package scalastyle

import scala.collection.JavaConverters._

import org.scalastyle._
import org.scalatest.{FlatSpec, Inspectors, LoneElement, Matchers}
import org.sonar.api.batch.rule.Severity
import org.sonar.api.rule.RuleStatus
import org.sonar.api.rules.RuleType
import org.sonar.api.server.rule.RulesDefinition.{Context, Repository, Rule}
import org.sonar.api.server.rule.{RuleParamType, RulesDefinition}

class ScalastyleRulesRepositorySpec extends FlatSpec with Matchers with Inspectors with LoneElement {
  trait Ctx {
    val context = new Context()
    new ScalastyleRulesRepository().define(context)
    val repository: Repository = context.repositories.loneElement
  }

  "ScalastyleRulesRepository" should "define rules repository" in new Ctx {
    context.repositories should have size 1
  }

  it should "correctly define repository properties" in new Ctx {
    repository.key shouldBe "sonar-scala-scalastyle"
    repository.name shouldBe "Scalastyle"
    repository.language shouldBe "scala"
  }

  it should "include all Scalastyle inspections" in new Ctx {
    ScalastyleInspections.AllInspections should have size 69 // 29 templates + 40 default rules
    ScalastyleInspections.AllInspectionsByClass.size shouldBe ScalastyleInspections.AllInspections.size
    repository.rules should have size 95 // 29 templates + 40 default rules + 26 template instances
  }

  it should "have all rules with non-empty properties" in new Ctx {
    forEvery(repository.rules) { rule =>
      rule.key should not be empty
      rule.internalKey should not be empty
      rule.name should not be empty
      rule.markdownDescription should not be empty
      rule.severity should not be empty
    }
  }

  it should "have all rules' keys start with org.scalastyle" in new Ctx {
    forEvery(repository.rules) { rule =>
      rule.key should startWith("org.scalastyle")
    }
  }

  it should "have all rules activated by default" in new Ctx {
    forEvery(repository.rules) { rule =>
      rule.activatedByDefault shouldBe true
    }
  }

  it should "have all rules with READY status" in new Ctx {
    forEvery(repository.rules) { rule =>
      rule.status shouldBe RuleStatus.READY
    }
  }

  it should "have all rules marked as CODE_SMELL" in new Ctx {
    forEvery(repository.rules) { rule =>
      rule.`type` shouldBe RuleType.CODE_SMELL
    }
  }

  it should "have rules with parameters" in new Ctx {
    forAtLeast(1, repository.rules) { rule =>
      rule.params should not be empty
    }
  }

  it should "not have rules with empty parameters" in new Ctx {
    val params: Seq[RulesDefinition.Param] =
      repository.rules.asScala
        .filter(r => !r.params.isEmpty)
        .flatMap(_.params.asScala)

    forAll(params) { param =>
      param.name should not be empty
      param.description should not be empty
    }
  }

  it should "have all rules contain ruleClass parameter" in new Ctx {
    val rules: Seq[Rule] = repository.rules.asScala.filter(r => !r.params.isEmpty)
    forEvery(rules) { rule =>
      rule.params.asScala.exists(p => p.key === "ruleClass" && p.defaultValue.startsWith("org.scalastyle"))
    }
  }

  it should "create rules with correct parameters" in new Ctx {
    val rule: Rule = repository.rule("org.scalastyle.file.FileLineLengthChecker")
    val params: Seq[(String, RuleParamType, String)] =
      rule.params().asScala.map(p => (p.name, p.`type`, p.defaultValue))
    val expected = Seq(
      ("maxLineLength", RuleParamType.INTEGER, "160"),
      ("tabSize", RuleParamType.INTEGER, "4"),
      ("ignoreImports", RuleParamType.BOOLEAN, "false"),
      ("ruleClass", RuleParamType.STRING, "org.scalastyle.file.FileLineLengthChecker")
    )

    params should contain theSameElementsAs expected
  }

  it should "convert Scalastyle inspection level to SonarQube Severity" in {
    ScalastyleRulesRepository.levelToSeverity(InfoLevel) shouldBe Severity.INFO
    ScalastyleRulesRepository.levelToSeverity(WarningLevel) shouldBe Severity.MINOR
    ScalastyleRulesRepository.levelToSeverity(ErrorLevel) shouldBe Severity.MAJOR
  }

  it should "convert Scalastyle parameter type to SonarQube parameter type" in {
    ScalastyleRulesRepository.parameterTypeToRuleParamType(
      "org.scalastyle.file.HeaderMatchesChecker",
      "header",
      StringType
    ) shouldBe RuleParamType.TEXT
    ScalastyleRulesRepository.parameterTypeToRuleParamType("", "", StringType) shouldBe RuleParamType.STRING
    ScalastyleRulesRepository.parameterTypeToRuleParamType("", "", IntegerType) shouldBe RuleParamType.INTEGER
    ScalastyleRulesRepository.parameterTypeToRuleParamType("", "", BooleanType) shouldBe RuleParamType.BOOLEAN
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

    ScalastyleRulesRepository.formatDescription(inspection1) shouldBe "*description*"
    ScalastyleRulesRepository.formatDescription(inspection2) shouldBe "*description*\n\nextraDescription"
    ScalastyleRulesRepository.formatDescription(inspection3) shouldBe "*description*\n\njustification\n\nextraDescription"
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

    ScalastyleRulesRepository.format(text) shouldBe expected
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

    ScalastyleRulesRepository.format(text) shouldBe expected
  }

  it should "correctly reformat text with some code blocks" in {
    val text =
      """
        |line1. next
        |line 2.
        |   code block 1
        |
        |line 3.
        |
        |   code block 2
        |   code block 2
        |line 4.
        |
        |   code block 3
        |
        |   code block 4
        |""".stripMargin.stripPrefix("\n").stripLineEnd

    val expected =
      """
        |line1. next
        |line 2.
        |``
        |   code block 1
        |`` line 3.
        |``
        |   code block 2
        |   code block 2
        |`` line 4.
        |``
        |   code block 3
        |   code block 4
        |``
        |""".stripMargin.stripPrefix("\n").stripLineEnd

    ScalastyleRulesRepository.format(text) shouldBe expected
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

    ScalastyleRulesRepository.format(text) shouldBe expected
  }
}
