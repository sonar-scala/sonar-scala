package com.mwz.sonar.scala.scapegoat

import inspections.{Level, ScapegoatInspection}

import org.sonar.api.server.rule.RulesDefinition
import org.sonar.api.rule.RuleStatus
import org.sonar.api.rule.Severity
import org.sonar.api.rules.RuleType

/** Defines a rules repository for the Scapegoat inspections */
class ScapegoatRulesRepository extends RulesDefinition {

  /** Defines the rules in the repository */
  override def define(context: RulesDefinition.Context): Unit =
    ???
}
