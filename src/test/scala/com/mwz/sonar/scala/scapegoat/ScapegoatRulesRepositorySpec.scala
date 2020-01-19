/*
 * Copyright (C) 2018-2020  All sonar-scala contributors
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

package com.mwz.sonar.scala.scapegoat

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.{Inspectors, LoneElement}
import org.sonar.api.rule.{RuleStatus, Severity}
import org.sonar.api.rules.RuleType
import org.sonar.api.server.rule.RulesDefinition.Context

/** Tests the correct behavior of the Scapegoat Rules Repository */
class ScapegoatRulesRepositorySpec extends AnyFlatSpec with Inspectors with LoneElement with Matchers {
  trait Ctx {
    val context = new Context()
    new ScapegoatRulesRepository().define(context)
    val repository = context.repositories.loneElement
    val rules = repository.rules
  }

  "ScapegoatRulesRepository" should "define only one repository" in new Ctx {
    context.repositories should have size 1
  }

  it should "properly define the properties of the repository" in new Ctx {
    repository.key shouldBe "sonar-scala-scapegoat"
    repository.name shouldBe "Scapegoat"
    repository.language shouldBe "scala"
  }

  it should "define one rule for each scapegoat inspection" in new Ctx {
    rules should have size ScapegoatInspections.AllInspections.size
  }

  it should "properly define the properties of the ArrayEquals rule" in new Ctx {
    val arrayEqualsRule = repository.rule("com.sksamuel.scapegoat.inspections.collections.ArrayEquals")

    arrayEqualsRule.internalKey shouldBe "com.sksamuel.scapegoat.inspections.collections.ArrayEquals"
    arrayEqualsRule.name shouldBe "Array equals"
    arrayEqualsRule.markdownDescription shouldBe "Array equals is not an equality check. Use a.deep == b.deep or convert to another collection type"
    arrayEqualsRule.activatedByDefault shouldBe true
    arrayEqualsRule.status shouldBe RuleStatus.READY
    arrayEqualsRule.severity shouldBe Severity.INFO
    arrayEqualsRule.`type` shouldBe RuleType.CODE_SMELL
  }

  "All Scapegoat Rules" should "have a valid internal key" in new Ctx {
    forEvery(rules) { rule =>
      rule.internalKey should startWith("com.sksamuel.scapegoat.inspections")
    }
  }

  it should "have a non-empty name" in new Ctx {
    forEvery(rules) { rule =>
      rule.name should not be empty
    }
  }

  it should "have a non-empty description" in new Ctx {
    forEvery(rules) { rule =>
      rule.markdownDescription should not be empty
    }
  }

  it should "be activated by default" in new Ctx {
    forEvery(rules) { rule =>
      rule.activatedByDefault shouldBe true
    }
  }

  it should "have a READY status" in new Ctx {
    forEvery(rules) { rule =>
      rule.status shouldBe RuleStatus.READY
    }
  }

  it should "have a valid severity" in new Ctx {
    forEvery(rules) { rule =>
      val ruleSeverity = rule.severity
      forExactly(1, Severity.ALL) { severity =>
        ruleSeverity shouldBe severity
      }
    }
  }

  it should "be a CODE_SMELL" in new Ctx {
    forEvery(rules) { rule =>
      rule.`type` shouldBe RuleType.CODE_SMELL
    }
  }
}
