package com.mwz.sonar.scala
package scapegoat

import inspections.{Level, ScapegoatInspection}

import org.sonar.api.server.rule.RulesDefinition
import org.sonar.api.rule.RuleStatus
import org.sonar.api.rule.Severity
import org.sonar.api.rules.RuleType

/** Defines a rules repository for the Scapegoat inspections */
class ScapegoatRulesRepository extends RulesDefinition {

  /** Defines the rules in the repository */
  override def define(context: RulesDefinition.Context): Unit = {
    //crete an empty repository
    val repository =
      context
        .createRepository(ScapegoatRulesRepository.RepositoryKey, Scala.LanguageKey)
        .setName(ScapegoatRulesRepository.RepositoryName)

    //register each scapegoat inspection as a repository rule
    for (inspection <- ScapegoatInspection.AllScapegoatInspections) {
      val rule = repository.createRule(inspection.id)
      val ruleSeverity = ScapegoatRulesRepository.scapegoatLevelToRuleSeverity(inspection.defaultLevel)

      rule.setInternalKey(inspection.id)
      rule.setName(inspection.name)
      rule.setMarkdownDescription(inspection.description)
      rule.setActivatedByDefault(false) // scalastyle:ignore LiteralArguments
      rule.setStatus(RuleStatus.READY)
      rule.setSeverity(ruleSeverity)
      rule.setType(RuleType.CODE_SMELL)
    }

    //save the repository
    repository.done()
  }
}

object ScapegoatRulesRepository {
  private val RepositoryKey = "ScapegoatRepository"
  private val RepositoryName = "Scapegoat Repository"

  private def scapegoatLevelToRuleSeverity(level: Level): String = level match {
    case Level.Info    => Severity.INFO
    case Level.Warning => Severity.MAJOR
    case Level.Error   => Severity.BLOCKER
  }
}
