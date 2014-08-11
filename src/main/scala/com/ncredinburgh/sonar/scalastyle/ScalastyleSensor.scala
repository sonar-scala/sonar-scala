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

import org.scalastyle._
import org.slf4j.LoggerFactory
import org.sonar.api.batch.{Sensor, SensorContext}
import org.sonar.api.component.ResourcePerspectives
import org.sonar.api.issue.{Issuable, Issue}
import org.sonar.api.profiles.RulesProfile
import org.sonar.api.resources.Project
import org.sonar.api.rule.RuleKey
import org.sonar.api.rules.{Rule, RuleFinder, RuleQuery}
import org.sonar.api.scan.filesystem.{FileQuery, ModuleFileSystem}

import scala.collection.JavaConversions._


/**
 * Main sensor for return Scalastyle issues to Sonar.
 */
class ScalastyleSensor(resourcePerspectives: ResourcePerspectives,
    runner: ScalastyleRunner,
    moduleFileSystem: ModuleFileSystem,
    rf: RuleFinder)
  extends Sensor {

  def this(resourcePerspectives: ResourcePerspectives,
           rp: RulesProfile,
           moduleFileSystem: ModuleFileSystem,
           rf: RuleFinder) = this(resourcePerspectives, new ScalastyleRunner(rp), moduleFileSystem, rf)

  val ScalaFileQuery = FileQuery.onSource.onLanguage(Constants.ScalaKey)

  private val log = LoggerFactory.getLogger(classOf[ScalastyleSensor])

  override def shouldExecuteOnProject(project: Project): Boolean = {
    moduleFileSystem.files(ScalaFileQuery).nonEmpty
  }

  override def analyse(project: Project, context: SensorContext): Unit = {
    val files = moduleFileSystem.files(ScalaFileQuery)
    val encoding = moduleFileSystem.sourceCharset.name
    val messages = runner.run(encoding, files.toList)

    messages foreach (processMessage(_))
  }

  private def processMessage(message: Message[FileSpec]): Unit = message match {
    case error: StyleError[FileSpec] => processError(error)
    case exception: StyleException[FileSpec] => processException(exception)
    case _ => Unit
  }

  private def processError(error: StyleError[FileSpec]): Unit = {
    log.debug("Error message for rule " + error.clazz.getName)

    val ioFile = new java.io.File(error.fileSpec.name) // We assume that the filespec name is an absolute path
    val resource = org.sonar.api.resources.File.fromIOFile(ioFile, moduleFileSystem.sourceDirs)
    val issuable = Option(resourcePerspectives.as(classOf[Issuable], resource))
    val rule: Rule = findSonarRuleForError(error)

    log.debug("Matched to sonar rule " + rule)

    if (issuable.isDefined) {
      addIssue(issuable.get, error, rule)
    } else {
      log.error("issuable is null, cannot add issue")
    }
  }

  private def addIssue(issuable: Issuable, error: StyleError[FileSpec], rule: Rule): Unit = {
    val lineNum = sanitiseLineNum(error.lineNumber)
    val messageStr = error.customMessage getOrElse rule.getDescription

    val issue: Issue = issuable.newIssueBuilder.ruleKey(rule.ruleKey)
      .line(lineNum).message(messageStr).build
    issuable.addIssue(issue)
  }

  private def findSonarRuleForError(error: StyleError[FileSpec]): Rule = {
    val key = Constants.RepositoryKey
    val errorKey = error.clazz.getName
    log.debug("Looking for sonar rule for " + errorKey)
    rf.find(RuleQuery.create.withKey(errorKey).withRepositoryKey(key))
  }

  private def processException(exception: StyleException[FileSpec]): Unit = {
    log.error("Got exception message from Scalastyle. " +
      "Check you have valid parameters configured for all rules. Exception message was: " + exception.message)
  }

  // sonar claims to accept null or a non zero lines, however if it is passed
  // null it blows up at runtime complaining it was passed 0
  private def sanitiseLineNum(maybeLine: Option[Int]) = if ((maybeLine getOrElse 0) != 0) {
    maybeLine.get
  } else {
    1
  }
}
