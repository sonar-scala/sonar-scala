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

import com.mwz.sonar.scala.Scala
import org.scalastyle._
import org.slf4j.LoggerFactory
import org.sonar.api.batch.fs.{FileSystem, InputFile}
import org.sonar.api.batch.sensor.Sensor
import org.sonar.api.batch.sensor.SensorContext
import org.sonar.api.batch.sensor.SensorDescriptor
import org.sonar.api.batch.sensor.issue.NewIssue
import org.sonar.api.profiles.RulesProfile
import org.sonar.api.rules.{Rule, RuleFinder, RuleQuery}

import scala.collection.JavaConverters._

/**
 * Main sensor for return Scalastyle issues to Sonar.
 */
final class ScalastyleSensor(
  runner: ScalastyleRunner,
  fileSystem: FileSystem,
  ruleFinder: RuleFinder
) extends Sensor {

  def this(
    rulesProfile: RulesProfile,
    fileSystem: FileSystem,
    ruleFinder: RuleFinder
  ) = this(new ScalastyleRunner(rulesProfile), fileSystem, ruleFinder)

  private val log = LoggerFactory.getLogger(classOf[ScalastyleSensor])

  private def predicates = fileSystem.predicates()

  private def scalaFilesPredicate =
    predicates.and(predicates.hasType(InputFile.Type.MAIN), predicates.hasLanguage(Constants.ScalaKey))

  override def describe(descriptor: SensorDescriptor): Unit = {
    descriptor
      .createIssuesForRuleRepository("sonar-scala-scalastyle")
      .name("Scalastyle Sensor")
      .onlyOnFileType(InputFile.Type.MAIN)
      .onlyOnLanguage(Scala.LanguageKey)
  }

  override def execute(context: SensorContext): Unit = {
    val files = fileSystem.files(scalaFilesPredicate)
    val encoding = fileSystem.encoding.name
    val messages = runner.run(encoding, files.asScala.toList.asJava)

    messages.foreach(msg => processMessage(msg, context))
  }

  private def processMessage(message: Message[FileSpec], context: SensorContext): Unit = message match {
    case error: StyleError[FileSpec]         => processError(error, context)
    case exception: StyleException[FileSpec] => processException(exception, context)
    case _                                   => Unit
  }

  private def processError(error: StyleError[FileSpec], context: SensorContext): Unit = {
    log.debug("Error message for rule " + error.clazz.getName)

    val inputFile: InputFile = fileSystem.inputFile(predicates.hasPath(error.fileSpec.name))
    val rule = findSonarRuleForError(error)

    Option(inputFile) match {
      case Some(file) =>
        val scalastyleIssue: NewIssue = context.newIssue().forRule(rule.ruleKey())
        addIssue(scalastyleIssue, error, rule, file)

      case None =>
        log.error(s"The file '${error.fileSpec.name}' couldn't be found.")
    }

  }

  private def addIssue(issue: NewIssue, error: StyleError[FileSpec], rule: Rule, file: InputFile): Unit = {
    val lineNum = sanitiseLineNum(error.lineNumber)
    val messageStr = error.customMessage getOrElse rule.getName

    issue.at(
      issue
        .newLocation()
        .on(file)
        .at(file.selectLine(lineNum))
        .message(messageStr)
    )

    issue.save()
  }

  private def findSonarRuleForError(error: StyleError[FileSpec]): Rule = {
    val key = Constants.RepositoryKey
    val errorKey = error.key // == scalastyle ConfigurationChecker.customId
    log.debug("Looking for sonar rule for " + errorKey)
    ruleFinder.find(RuleQuery.create.withKey(errorKey).withRepositoryKey(key))
  }

  private def processException(exception: StyleException[FileSpec], context: SensorContext): Unit = {
    log.error(
      "Got exception message from Scalastyle. " +
      "Check you have valid parameters configured for all rules. Exception message was: " + exception.message
    )
  }

  // sonar claims to accept null or a non zero lines, however if it is passed
  // null it blows up at runtime complaining it was passed 0
  /** Ensures that a line number is valid, if not returns 1 */
  private def sanitiseLineNum(maybeLine: Option[Int]) =
    maybeLine.filter(_ > 0).getOrElse(1) // scalastyle:ignore LiteralArguments
}
