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
package scalastyle

import scala.jdk.CollectionConverters._

import com.mwz.sonar.scala.metadata.scalastyle.ScalastyleRules
import org.scalatest.Inspectors
import org.scalatest.LoneElement
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.sonar.api.rule.RuleStatus
import org.sonar.api.rules.RuleType
import org.sonar.api.server.rule.RuleParamType
import org.sonar.api.server.rule.RulesDefinition
import org.sonar.api.server.rule.RulesDefinition.Context
import org.sonar.api.server.rule.RulesDefinition.Repository
import org.sonar.api.server.rule.RulesDefinition.Rule

class ScalastyleRulesRepositorySpec extends AnyFlatSpec with Matchers with Inspectors with LoneElement {
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
    ScalastyleRules.rules.length shouldBe 74 // 31 templates + 43 default rules
    ScalastyleRules.rules.map(r => r.key -> r).iterator.toMap.size shouldBe ScalastyleRules.rules.length
    repository.rules should have size 102 // 31 templates + 43 default rules + 28 template instances
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
    forEvery(repository.rules)(rule => rule.key should startWith("org.scalastyle"))
  }

  it should "have all rules activated by default" in new Ctx {
    forEvery(repository.rules)(rule => rule.activatedByDefault shouldBe true)
  }

  it should "have all rules with READY status" in new Ctx {
    forEvery(repository.rules)(rule => rule.status shouldBe RuleStatus.READY)
  }

  it should "have all rules marked as CODE_SMELL" in new Ctx {
    forEvery(repository.rules)(rule => rule.`type` shouldBe RuleType.CODE_SMELL)
  }

  it should "have rules with parameters" in new Ctx {
    forAtLeast(1, repository.rules)(rule => rule.params should not be empty)
  }

  it should "not have rules with empty parameters" in new Ctx {
    val params: Seq[RulesDefinition.Param] =
      repository.rules.asScala
        .filter(r => !r.params.isEmpty)
        .flatMap(_.params.asScala)
        .toSeq

    forAll(params) { param =>
      param.name should not be empty
      param.description should not be empty
    }
  }

  it should "have all rules contain ruleClass parameter" in new Ctx {
    val rules: Seq[Rule] = repository.rules.asScala.filter(r => !r.params.isEmpty).toSeq
    forEvery(rules) { rule =>
      rule.params.asScala.exists(p => p.key === "ruleClass" && p.defaultValue.startsWith("org.scalastyle"))
    }
  }

  it should "create rules with correct parameters" in new Ctx {
    val rule: Rule = repository.rule("org.scalastyle.file.FileLineLengthChecker")
    val params: Seq[(String, RuleParamType, String)] =
      rule.params().asScala.map(p => (p.name, p.`type`, p.defaultValue)).toSeq
    val expected = Seq(
      ("maxLineLength", RuleParamType.INTEGER, "160"),
      ("tabSize", RuleParamType.INTEGER, "4"),
      ("ignoreImports", RuleParamType.BOOLEAN, "false"),
      ("ruleClass", RuleParamType.STRING, "org.scalastyle.file.FileLineLengthChecker")
    )

    params should contain theSameElementsAs expected
  }
}
