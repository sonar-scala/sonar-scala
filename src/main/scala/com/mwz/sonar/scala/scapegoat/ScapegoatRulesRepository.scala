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
package com.mwz.sonar.scala
package scapegoat

import com.mwz.sonar.scala.scapegoat.inspections.ScapegoatInspection.AllScapegoatInspections
import org.sonar.api.rule.RuleStatus
import org.sonar.api.rules.RuleType
import org.sonar.api.server.rule.RulesDefinition

/** Defines a rules repository for the Scapegoat inspections */
final class ScapegoatRulesRepository extends RulesDefinition {

  /** Defines the rules in the repository */
  override def define(context: RulesDefinition.Context): Unit = {
    // create an empty repository
    val repository =
      context
        .createRepository(ScapegoatRulesRepository.RepositoryKey, Scala.LanguageKey)
        .setName(ScapegoatRulesRepository.RepositoryName)

    // register each scapegoat inspection as a repository rule
    AllScapegoatInspections foreach { inspection =>
      val rule = repository.createRule(inspection.id)

      rule.setInternalKey(inspection.id)
      rule.setName(inspection.name)
      rule.setMarkdownDescription(inspection.description)
      rule.setActivatedByDefault(true) // scalastyle:ignore LiteralArguments
      rule.setStatus(RuleStatus.READY)
      rule.setSeverity(inspection.defaultLevel.toRuleSeverity.name)
      rule.setType(RuleType.CODE_SMELL)
    }

    // save the repository
    repository.done()
  }
}

private[scapegoat] object ScapegoatRulesRepository {
  final val RepositoryKey = "sonar-scala-scapegoat"
  final val RepositoryName = "Scapegoat"
}
