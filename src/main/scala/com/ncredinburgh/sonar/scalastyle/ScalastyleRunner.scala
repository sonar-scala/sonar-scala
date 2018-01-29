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

import java.io.File

import org.scalastyle.StyleError
import org.scalastyle.StyleException
import org.scalastyle.Message
import org.scalastyle.FileSpec
import org.scalastyle.Directory
import org.scalastyle.ScalastyleChecker
import org.scalastyle.ErrorLevel
import org.scalastyle.ScalastyleConfiguration
import org.scalastyle.ConfigurationChecker
import org.slf4j.LoggerFactory
import org.sonar.api.profiles.RulesProfile
import org.sonar.api.rules.ActiveRule
import scala.collection.JavaConversions._

/**
 * Runs Scalastyle based on active rules in the given RulesProfile
 */
class ScalastyleRunner(rp: RulesProfile) {
  private val log = LoggerFactory.getLogger(classOf[ScalastyleRunner])

  def run(encoding: String, files: java.util.List[File]): List[Message[FileSpec]] = {
    log.debug("Using config " + config)

    val fileSpecs = Directory.getFilesAsJava(Some(encoding), files)
    val messages = new ScalastyleChecker[FileSpec]().checkFiles(config, fileSpecs)

    // only errors and exceptions are of interest
    messages.collect {
     case e: StyleError[_] => e
     case ex: StyleException[_] => ex
   }

  }

  def config: ScalastyleConfiguration = {
    val sonarRules = rp.getActiveRulesByRepository(Constants.RepositoryKey)
    val checkers = sonarRules.map(ruleToChecker).toList
    new ScalastyleConfiguration("sonar", true, checkers)
  }

  private def ruleToChecker(activeRule: ActiveRule): ConfigurationChecker = {
    val sonarParams = activeRule.getActiveRuleParams.map(p => (p.getKey, p.getValue)).toMap
    
    val checkerParams = sonarParams.filterNot(keyVal => keyVal._1 == Constants.ClazzParam)
    val className = sonarParams(Constants.ClazzParam)
    val sonarKey = activeRule.getRuleKey
    
    ConfigurationChecker(className, ErrorLevel, true, sonarParams, None, Some(sonarKey))
  }
}
