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
package scapegoat

import com.mwz.sonar.scala.metadata.Rule
import com.mwz.sonar.scala.metadata.scapegoat.ScapegoatRulesRepository._
import com.mwz.sonar.scala.scapegoat.ScapegoatRulesRepository._
import org.sonar.api.rule.RuleStatus
import org.sonar.api.rules.RuleType
import org.sonar.api.server.rule.RulesDefinition
import org.sonar.api.server.rule.RulesDefinition.NewRepository
import org.sonar.api.server.rule.RulesDefinition.NewRule

/** Defines a rules repository for the Scapegoat inspections */
final class ScapegoatRulesRepository extends RulesDefinition {

  /** Defines the rules in the repository */
  override def define(context: RulesDefinition.Context): Unit = {
    // create an empty repository
    val repository =
      context
        .createRepository(RepositoryKey, Scala.LanguageKey)
        .setName(RepositoryName)

    // register each scapegoat inspection as a repository rule
    rulesRepository.rules.iterator.foreach(rule => createRule(repository, rule))

    // save the repository
    repository.done()
  }
}

object ScapegoatRulesRepository {
  def createRule(repository: NewRepository, rule: Rule): NewRule = {
    val newRule = repository.createRule(rule.key)
    newRule.setInternalKey(rule.key)
    newRule.setName(rule.name)
    newRule.setMarkdownDescription(rule.sonarMdDescription)
    newRule.setActivatedByDefault(true) // scalastyle:ignore
    newRule.setStatus(RuleStatus.READY)
    newRule.setSeverity(rule.severity.entryName.toUpperCase)
    newRule.setType(RuleType.CODE_SMELL)
  }
}
