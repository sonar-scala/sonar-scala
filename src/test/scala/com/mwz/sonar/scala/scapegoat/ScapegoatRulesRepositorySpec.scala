package com.mwz.sonar.scala.scapegoat

import inspections.{Level, ScapegoatInspection}

import org.scalatest.{FlatSpec, LoneElement, Matchers}
import org.sonar.api.server.rule.RulesDefinition.Context
import org.sonar.api.rule.RuleStatus
import org.sonar.api.rule.Severity
import org.sonar.api.rules.RuleType

/** Tests the correct behavior of the Scapegoat Rules Repository */
class ScapegoatRulesRepositorySpec extends FlatSpec with LoneElement with Matchers {
  behavior of "the Scapegoat Rules Repository"

  it should "define only one repository" in {
    val context = new Context()
    new ScapegoatRulesRepository().define(context)

    context.repositories should have length 1
  }

  it should "properly define the properties of the repository" in {
    val context = new Context()
    new ScapegoatRulesRepository().define(context)
    val scapegoatRepository = context.repositories.loneElement

    scapegoatRepository.key shouldBe "ScapegoatRepository"
    scapegoatRepository.name shouldBe "Scapegoat Repository"
    scapegoatRepository.language shouldBe "scala"
  }

  it should "define one rule for each scapegoat inspection" in {
    val context = new Context()
    new ScapegoatRulesRepository().define(context)
    val scapegoatRepository = context.repositories.loneElement

    val totalInspections = ScapegoatInspection.AllScapegoatInspections.length
    scapegoatRepository.rules should have length totalInspections
  }

  it should "properly define the properties of a rule (AnyUse)" in {
    val context = new Context()
    new ScapegoatRulesRepository().define(context)
    val scapegoatRepository = context.repositories.loneElement
    val anyUseRule = scapegoatRepository.rule("com.sksamuel.scapegoat.inspections.AnyUse")

    anyUseRule.name shouldBe "AnyUse"
    anyUseRule.markdownDescription shouldBe "No Explanation"
    anyUseRule.activatedByDefault shouldBe false
    anyUseRule.status shouldBe RuleStatus.READY
    anyUseRule.severity shouldBe Severity.INFO
    anyUseRule.`type` shouldBe RuleType.CODE_SMELL
  }
}
