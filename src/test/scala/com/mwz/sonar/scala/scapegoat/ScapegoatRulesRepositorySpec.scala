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

    scapegoatRepository.key shouldBe "ScapegoatRepository"
    scapegoatRepository.name shouldBe "Scapegoat Repository"
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
    anyUseRule.name shouldBe "scala:scalastyle:AnyUse"
    anyUseRule.markdownDescription shouldBe "No Explanation"
    anyUseRule.activatedByDefault shouldBe true
    anyUseRule.status shouldBe RuleStatus.READY
    anyUseRule.severity shouldBe Severity.INFO
    anyUseRule.`type` shouldBe RuleType.CODE_SMELL
  }

  // tests about properties of the scapegoat repository rules
  val scapegoatRepository = context.repository("ScapegoatRepository")
  val rules = scapegoatRepository.rules
  behavior of "all Scapegoat Rules"

  it should "have a valid internal key" in {
    forEvery(rules) { rule =>
      rule.internalKey should startWith("com.sksamuel.scapegoat.inspections")
    }
  }

  it should "have a valid name" in {
    forEvery(rules) { rule =>
      rule.name should startWith("scala:scalastyle:")
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
