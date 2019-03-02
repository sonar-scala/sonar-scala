/*
 * Copyright (C) 2018-2019  All sonar-scala contributors
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

package com.ncredinburgh.sonar.scalastyle.testUtils

import java.util
import com.ncredinburgh.sonar.scalastyle.{Constants, ScalastyleResources}
import org.sonar.api.rule.RuleKey
import org.sonar.api.rules.{Rule, RuleFinder, RulePriority, RuleQuery}
import scala.collection.JavaConverters._
import com.ncredinburgh.sonar.scalastyle.ScalastyleRepository
import org.sonar.api.server.rule.RuleParamType
import com.ncredinburgh.sonar.scalastyle.RepositoryRule

object TestRuleFinder extends RuleFinder {

  override def findByKey(repositoryKey: String, key: String): Rule =
    findAll(RuleQuery.create()).asScala
      .find(r => r.getRepositoryKey == repositoryKey && r.getKey == key)
      .orNull

  override def findByKey(key: RuleKey): Rule =
    findAll(RuleQuery.create()).asScala
      .find(r => r.getRepositoryKey == key.repository() && r.getKey == key.rule())
      .orNull

  override def findById(ruleId: Int): Rule =
    findAll(RuleQuery.create()).asScala.find(r => r.getId == ruleId).orNull

  override def findAll(query: RuleQuery): util.Collection[Rule] = {
    ScalastyleResources.allDefinedRules.filterNot(r => isTemplate(r)) map { defRule =>
      val rule = Rule.create()
      val key = defRule.id
      rule.setRepositoryKey(Constants.RepositoryKey)
      rule.setLanguage(Constants.ScalaKey)
      rule.setKey(ScalastyleRepository.getStandardKey(defRule.clazz))
      rule.setName(ScalastyleResources.label(key))
      rule.setDescription(defRule.description)
      rule.setConfigKey(ScalastyleRepository.getStandardKey(defRule.clazz))

      // currently all rules comes with "warning" default level so we can treat with major severity
      rule.setSeverity(RulePriority.MAJOR)

      // add parameters
      defRule.params foreach { param =>
        rule.createParameter
          .setDefaultValue(param.defaultVal)
          .setType(param.`type`.`type`())
          .setKey(param.name)
          .setDescription(param.desc)
      }

      // add synthetic parameter as reference to the class
      rule
        .createParameter(Constants.ClazzParam)
        .setDefaultValue(defRule.clazz)
        .setType(RuleParamType.STRING.`type`())
        .setDescription("Scalastyle checker that validates the rule.")

      rule
    }
  }.asJavaCollection

  override def find(query: RuleQuery): Rule = ???

  private def isTemplate(rule: RepositoryRule): Boolean =
    rule.params.nonEmpty
}
