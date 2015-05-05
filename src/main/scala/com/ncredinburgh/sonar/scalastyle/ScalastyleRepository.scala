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

import org.sonar.api.rule.Severity
import org.sonar.api.server.rule.RulesDefinition


/**
 * Scalastyle rules repository - creates a rule for each checker shipped with Scalastyle based
 * on the scalastyle_definition.xml file that ships with the Scalastyle jar.
 */
class ScalastyleRepository extends RulesDefinition {

  override def define(context: RulesDefinition.Context): Unit = {
    val repository = context
      .createRepository(Constants.RepositoryKey, Constants.ScalaKey)
      .setName(Constants.RepositoryName)
    ScalastyleResources.allDefinedRules map {
      case resRule =>
        val rule = repository.createRule(resRule.clazz)
        rule.setName(ScalastyleResources.label(resRule.id))
        rule.setHtmlDescription(resRule.description)
        // currently all rules comes with "warning" default level so we can treat with major severity
        rule.setSeverity(Severity.MAJOR)

        resRule.params map {
          case param =>
            rule
              .createParam(param.name)
              .setDefaultValue(param.defaultVal)
              .setType(param.`type`)
              .setDescription(param.desc)
        }
    }

    repository.done()
  }

}


