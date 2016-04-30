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
import org.sonar.api.profiles.{ProfileDefinition, RulesProfile}
import org.sonar.api.rules.{RuleFinder, ActiveRule}
import org.sonar.api.utils.ValidationMessages
import org.scalastyle.ScalastyleError
import scala.xml.XML
import collection.JavaConversions._
import org.sonar.api.rules.RuleQuery
import org.sonar.api.rules.Rule

/**
 * This class creates the default "Scalastyle" quality profile from Scalastyle's default_config.xml
 */
class ScalastyleQualityProfile(ruleFinder: RuleFinder) extends ProfileDefinition {

  private val log = LoggerFactory.getLogger(classOf[ScalastyleQualityProfile])
  private val defaultConfigRules = xmlFromClassPath("/default_config.xml") \\ "scalastyle" \ "check"

  override def createProfile(validation: ValidationMessages): RulesProfile = {
    val profile = RulesProfile.create(Constants.ProfileName, Constants.ScalaKey)
    val enabledRules = defaultConfigRules filter (x => (x \ "@enabled").text.equals("true"))
    val defaultRuleClasses = enabledRules map (x => (x \ "@class").text)

    // currently findAll is buggy (sonar 4.5-5.1 https://jira.sonarsource.com/browse/SONAR-6390)
    // will still work but won't add all possible rule to the default profile
    val query = RuleQuery.create().withRepositoryKey(Constants.RepositoryKey)
    val repoRules = ruleFinder.findAll(query)

    for {clazz <- defaultRuleClasses} {
      val ruleOption = repoRules.find(clazzMatch(_, clazz))

      ruleOption match {
        case None => validation.addWarningText(s"Rule for $clazz not found in ${Constants.RepositoryKey} repository! Rule won't be activated.")
        case Some(rule) => {
          if (!rule.isTemplate()) {
            val activated = profile.activateRule(rule, rule.getSeverity)
            setParameters(activated, clazz)
          }
        }
      }
    }

    profile
  }

  def setParameters(activeRule: ActiveRule, clazz: String) {
    // set parameters
    defaultConfigRules.find(x => (x \ "@class").text.equals(clazz)) match {
      case Some(rule) => {
        val params = (rule \ "parameters" \ "parameter").map(n => ((n \ "@name").text, n.text)).toMap
        params foreach { case (key, value) => activeRule.setParameter(key, value) }
      }
      case _ => log.warn("Default rule with key " + activeRule.getRuleKey + " could not found in default_config.xml")
    }
    
    // set synthetic parameter
    activeRule.setParameter(Constants.ClazzParam, clazz)
  }

  private def clazzMatch(rule: Rule, clazz: String): Boolean = {
    Option(rule.getParam(Constants.ClazzParam)) match {
      case Some(param) => {
        param.getDefaultValue.equals(clazz)
      }         
      case None => {
        log.warn(s"Could not find required parameter ${Constants.ClazzParam}, rule for $clazz cannot be activated")
        false
      }
    }
  }

  private def xmlFromClassPath(s: String) = XML.load(classOf[ScalastyleError].getResourceAsStream(s))
}
