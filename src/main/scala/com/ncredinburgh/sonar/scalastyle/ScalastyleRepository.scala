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

import org.slf4j.LoggerFactory
import org.sonar.api.rules._

import scala.collection.JavaConversions._

/**
 * Scalastyle rules repository - creates a rule for each checker shipped with Scalastyle based
 * on the scalastyle_definition.xml file that ships with the Scalastyle jar.
 */
class ScalastyleRepository extends RuleRepository(Constants.RepositoryKey, Constants.ScalaKey) {

  private val log = LoggerFactory.getLogger(classOf[ScalastyleRepository])

  override def createRules: java.util.List[Rule] = ScalastyleResources.allDefinedRules map toRule

  private def toRule(repoRule : RepositoryRule) = {
    val rule = Rule.create
    val key = repoRule.id
    rule.setRepositoryKey(Constants.RepositoryKey)
    rule.setLanguage(Constants.ScalaKey)
    rule.setKey(repoRule.clazz)
    rule.setName(ScalastyleResources.label(key))
    rule.setDescription(repoRule.description)
    rule.setConfigKey(key)
    // currently all rules comes with "warning" default level so we can treat with major severity
    rule.setSeverity(RulePriority.MAJOR)


    val params = repoRule.params map {
      case param =>
        rule
          .createParameter
          .setDefaultValue(param.defaultVal)
          .setType(param.typeName)
          .setKey(param.name)
          .setDescription(param.desc)
    }

    params foreach ( p => log.debug(s"Created a param for ${rule.getKey} : $p") )

    log.debug(s"Created a rule: $rule")

    rule
  }

}


