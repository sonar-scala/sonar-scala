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
import org.sonar.api.rules.ActiveRule
import org.sonar.api.utils.ValidationMessages
import scala.collection.JavaConversions._
import org.scalastyle.ScalastyleError
import scala.xml.XML

/**
 * This class creates the default "Scalastyle" quality profile from Scalastyle's default_config.xml
 */
class ScalastyleQualityProfile(scalastyleRepository: ScalastyleRepository) extends ProfileDefinition {

  private val log = LoggerFactory.getLogger(classOf[ScalastyleRepository])
  private val defaultConfigRules = xmlFromClassPath("/default_config.xml") \\ "scalastyle" \ "check"

  override def createProfile(validation: ValidationMessages): RulesProfile = {
    val profile = RulesProfile.create(Constants.ProfileName, Constants.ScalaKey)
    val enabledRules = defaultConfigRules filter (x => (x \ "@enabled").text.equals("true"))
    val defaultKeys = enabledRules map (x => (x \ "@class").text)
    val defaultRules = scalastyleRepository.createRules filter (rule => defaultKeys.contains(rule.getKey) )
    val activeRules = defaultRules map (rule => profile.activateRule(rule, rule.getSeverity))
    activeRules.foreach(setParameters)
    profile
  }

  def setParameters(activeRule: ActiveRule) {
    defaultConfigRules.find(x => (x \ "@class").text.equals(activeRule.getRuleKey) ) match {
      case Some(rule) =>
        val params = (rule \ "parameters" \ "parameter").map(n => ((n \ "@name").text, n.text )).toMap
        params foreach { case (key, value) => activeRule.setParameter(key, value) }
      case _ => log.warn("Default rule with key " + activeRule.getRuleKey + " could not found in default_config.xml")
    }
  }

  private def xmlFromClassPath(s: String) = XML.load(classOf[ScalastyleError].getResourceAsStream(s))
}
