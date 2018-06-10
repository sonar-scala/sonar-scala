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

import inspections.{Level, ScapegoatInspection}

import org.scalatest.{FlatSpec, Inspectors, LoneElement, Matchers}
import org.sonar.api.server.rule.RulesDefinition.Context
import org.sonar.api.rule.RuleStatus
import org.sonar.api.rule.Severity
import org.sonar.api.rules.RuleType

/** Tests the correct behavior of the Scapegoat Rules Repository */
class ScapegoatRulesRepositorySpec extends FlatSpec with Inspectors with LoneElement with Matchers {
  // tests about properties of the scapegoat repository
  val context = new Context()
  new ScapegoatRulesRepository().define(context)
  behavior of "the Scapegoat Rules Repository"

  it should "define only one repository" in {
    context.repositories should have length 1
  }

  it should "properly define the properties of the repository" in {
    val scapegoatRepository = context.repositories.loneElement

    scapegoatRepository.key shouldBe "sonar-scala-scapegoat-repository"
    scapegoatRepository.name shouldBe "Scapegoat"
    scapegoatRepository.language shouldBe "scala"
  }

  it should "define one rule for each scapegoat inspection" in {
    val scapegoatRepository = context.repositories.loneElement
    val totalInspections = ScapegoatInspection.AllScapegoatInspections.length

    scapegoatRepository.rules should have length totalInspections
  }

  it should "properly define the properties of the AnyUse rule" in {
    val scapegoatRepository = context.repositories.loneElement
    val anyUseRule = scapegoatRepository.rule("com.sksamuel.scapegoat.inspections.AnyUse")

    anyUseRule.internalKey shouldBe "com.sksamuel.scapegoat.inspections.AnyUse"
    anyUseRule.name shouldBe "AnyUse"
    anyUseRule.markdownDescription shouldBe "No Explanation"
    anyUseRule.activatedByDefault shouldBe true
    anyUseRule.status shouldBe RuleStatus.READY
    anyUseRule.severity shouldBe Severity.INFO
    anyUseRule.`type` shouldBe RuleType.CODE_SMELL
  }

  // tests about properties of the scapegoat repository rules
  val scapegoatRepository = context.repository("sonar-scala-scapegoat-repository")
  val rules = scapegoatRepository.rules
  behavior of "all Scapegoat Rules"

  it should "have a valid internal key" in {
    forEvery(rules) { rule =>
      rule.internalKey should startWith("com.sksamuel.scapegoat.inspections")
    }
  }

  it should "have a non-empty name" in {
    forEvery(rules) { rule =>
      rule.name should not be empty
    }
  }

  it should "have a non-empty description" in {
    forEvery(rules) { rule =>
      rule.markdownDescription should not be empty
    }
  }

  it should "be activated by default" in {
    forEvery(rules) { rule =>
      rule.activatedByDefault shouldBe true
    }
  }

  it should "have a READY status" in {
    forEvery(rules) { rule =>
      rule.status shouldBe RuleStatus.READY
    }
  }

  it should "have a valid severity" in {
    forEvery(rules) { rule =>
      val ruleSeverity = rule.severity
      forExactly(1, Severity.ALL) { severity =>
        ruleSeverity shouldBe severity
      }
    }
  }

  it should "be a CODE_SMELL" in {
    forEvery(rules) { rule =>
      rule.`type` shouldBe RuleType.CODE_SMELL
    }
  }
}
