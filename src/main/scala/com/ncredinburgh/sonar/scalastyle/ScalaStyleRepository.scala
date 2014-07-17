/*
 * Sonar Scala Style Plugin
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
 * ScalaStyle rules repository - creates a rule for each checker shipped with ScalaStyle based
 * on the scalastyle_definition.xml file that ships with the ScalaStyle jar.
 */
class ScalaStyleRepository extends RuleRepository(Constants.RepositoryKey, Constants.ScalaKey) {

  private val log = LoggerFactory.getLogger(classOf[ScalaStyleRepository])

  override def createRules: java.util.List[Rule] = ScalaStyleResources.allDefinedRules map (toRule)

  private def toRule(repoRule : RepositoryRule) = {
    val rule = Rule.create
    val key = repoRule.id
    rule.setRepositoryKey(Constants.RepositoryKey)
    rule.setKey(repoRule.clazz)
    rule.setName(ScalaStyleResources.shortDescription(key))
    rule.setDescription(repoRule.description)
    rule.setConfigKey(key)
    rule.setSeverity(RulePriority.MAJOR)

    val params = repoRule.params map { (r: Param) => rule.createParameter
                                                      .setDefaultValue(r.defaultVal)
                                                      .setType(r.typeName)
                                                      .setKey(r.name)
                                                      .setDescription(r.desc)}

    params foreach ( p => log.debug("Created param for " + rule.getKey + " : " + p) )

    rule
  }

}


