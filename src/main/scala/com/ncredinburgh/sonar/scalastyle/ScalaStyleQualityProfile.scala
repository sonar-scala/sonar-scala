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

import org.sonar.api.profiles.{ProfileDefinition, RulesProfile}
import org.sonar.api.utils.ValidationMessages
import scala.collection.JavaConversions._

/**
 * This class creates the default "Scalastyle" quality profile
 */
class ScalaStyleQualityProfile(scalaStyleRepository: ScalaStyleRepository) extends ProfileDefinition {
  
  override def createProfile(validation: ValidationMessages): RulesProfile = {
    val profile = RulesProfile.create(Constants.PROFILE_NAME, Constants.SCALA_KEY)
    val defaultRules = scalaStyleRepository.createRules.filterNot(_.getParams.exists(_.getDefaultValue == ""))
    defaultRules.foreach(rule => profile.activateRule(rule, rule.getSeverity))
    profile
  }
}


