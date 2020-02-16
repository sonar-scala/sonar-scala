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

package com.mwz.sonar.scala
package scalastyle

import com.mwz.sonar.scala.metadata.ParamType._
import com.mwz.sonar.scala.metadata.Rule
import com.mwz.sonar.scala.metadata.scalastyle.ScalastyleRulesRepository._
import com.mwz.sonar.scala.scalastyle.ScalastyleRulesRepository._
import org.sonar.api.rule.RuleStatus
import org.sonar.api.rules.RuleType
import org.sonar.api.server.rule.RulesDefinition
import org.sonar.api.server.rule.RulesDefinition.{NewParam, NewRepository, NewRule}

/**
 * Defines a repository for the Scalastyle inspections.
 */
final class ScalastyleRulesRepository extends RulesDefinition {
  override def define(context: RulesDefinition.Context): Unit = {

    // Create an empty repository.
    val repository: NewRepository =
      context
        .createRepository(RepositoryKey, Scala.LanguageKey)
        .setName(RepositoryName)

    // Register each Scalastyle inspection as a repository rule.
    rulesRepository.rules.toList.foreach(rule => createRule(repository, rule))

    // Save the repository.
    repository.done()
  }
}

object ScalastyleRulesRepository {

  /**
   * Create a new rule from the given inspection.
   */
  def createRule(repository: NewRepository, rule: Rule): NewRule = {
    val newRule = repository.createRule(rule.key)
    newRule.setInternalKey(rule.key)
    newRule.setName(rule.name)
    newRule.setMarkdownDescription(rule.description)
    newRule.setActivatedByDefault(true) // scalastyle:ignore
    newRule.setStatus(RuleStatus.READY)
    newRule.setSeverity(rule.severity.entryName.toUpperCase)
    newRule.setType(RuleType.CODE_SMELL)

    // Create parameters.
    rule.params.toList.foreach(param => createParam(newRule, param))

    // Set the rule as a template.
    newRule.setTemplate(rule.template)
  }

  /**
   * Create the parameter for the given rule.
   */
  def createParam(rule: NewRule, param: metadata.Param): NewParam = {
    rule
      .createParam(param.name)
      .setType(param.typ.asSonarRuleParamType)
      .setDescription(param.description)
      .setDefaultValue(param.default)
  }
}
