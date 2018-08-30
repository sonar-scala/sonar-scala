/*
 * Sonar Scalastyle Plugin
 * Copyright (C) 2014 All contributors
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package com.ncredinburgh.sonar.scalastyle

import org.sonar.api.rule.{RuleStatus, Severity}
import org.sonar.api.server.rule.RulesDefinition
import org.sonar.api.server.rule.RuleParamType
import org.slf4j.LoggerFactory
import org.sonar.api.server.rule.RulesDefinition.NewRepository
import com.ncredinburgh.sonar.scalastyle.ScalastyleRepository.getStandardKey

import scala.annotation.tailrec

object ScalastyleRepository {
  def getStandardKey(clazz: String): String = {
    val simpleClazz = clazz.reverse.takeWhile(_ != '.').reverse
    s"scalastyle_$simpleClazz"
  }
}

/**
 * Scalastyle rules repository - creates a rule for each checker shipped with Scalastyle based
 * on the scalastyle_definition.xml file that ships with the Scalastyle jar.
 */
final class ScalastyleRepository extends RulesDefinition {

  override def define(context: RulesDefinition.Context): Unit = {
    val repository = context
      .createRepository(Constants.RepositoryKey, Constants.ScalaKey)
      .setName(Constants.RepositoryName)

    ScalastyleResources.allDefinedRules foreach { repoRule =>
      {
        val ruleKey = determineFreeRuleKey(repoRule.clazz, repository)

        // define the rule
        val rule = repository.createRule(ruleKey)
        rule.setName(ScalastyleResources.label(repoRule.id))
        rule.setHtmlDescription(repoRule.description)

        // currently all rules comes with "warning" default level so we can treat with major severity
        rule.setSeverity(Severity.MAJOR)

        // add parameters
        repoRule.params foreach { param =>
          {
            rule
              .createParam(param.name)
              .setDefaultValue(param.defaultVal)
              .setType(param.`type`)
              .setDescription(param.desc)
          }
        }

        // add synthetic parameter as reference to the class
        rule
          .createParam(Constants.ClazzParam)
          .setDefaultValue(repoRule.clazz)
          .setType(RuleParamType.STRING)
          .setDescription("Scalastyle checker that validates the rule.")

        // if a rule has at least one real parameter make it a template
        rule.setTemplate(repoRule.params.nonEmpty)

        // TODO: Set the status of those rules as deprecated3?
        // rule.setStatus(RuleStatus.DEPRECATED)
      }
    }

    repository.done()
  }

  /**
   * determines a free rule key in the repo, in case the key scalastyle-<simple class name> is already
   *  in use the name scalastyle_<simple class name>_<i> is tried i = 1, 2, ....
   */
  private def determineFreeRuleKey(clazz: String, repo: NewRepository): String = {
    @tailrec
    def getFreeRuleKey(key: String, count: Int, repo: NewRepository): String = {
      val ruleKey = if (count == 0) key else s"${key}_${count}"
      // if the repo.rule method returns null, then the option will be empty
      val repoRule = Option(repo.rule(ruleKey))
      if (repoRule.isEmpty) {
        ruleKey
      } else {
        getFreeRuleKey(key, count + 1, repo)
      }
    }

    getFreeRuleKey(getStandardKey(clazz), count = 0, repo)
  }
}
