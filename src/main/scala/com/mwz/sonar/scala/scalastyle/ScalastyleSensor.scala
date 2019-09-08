/*
 * Copyright (C) 2018-2019  All sonar-scala contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU General Lesser Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.mwz.sonar.scala
package scalastyle

import java.io.File
import java.nio.file.Paths

import scala.collection.JavaConverters._
import scala.collection.immutable.Seq

import cats.instances.string._
import cats.syntax.eq._
import com.mwz.sonar.scala.util.Log
import com.mwz.sonar.scala.util.syntax.Optionals._
import org.scalastyle.{
  ConfigurationChecker,
  Directory,
  ErrorLevel,
  FileSpec,
  InfoLevel,
  Level,
  Message,
  ScalastyleConfiguration,
  StyleError,
  StyleException,
  WarningLevel,
  ScalastyleChecker => Checker
}
import org.sonar.api.batch.fs.{FilePredicates, InputFile}
import org.sonar.api.batch.rule.{ActiveRule, Severity}
import org.sonar.api.batch.sensor.issue.NewIssue
import org.sonar.api.batch.sensor.{Sensor, SensorContext, SensorDescriptor}
import org.sonar.api.config.Configuration
import org.sonar.api.rule.RuleKey

/**
 * Main sensor for executing Scalastyle analysis.
 */
final class ScalastyleSensor(scalastyleChecker: ScalastyleCheckerAPI) extends Sensor {
  private[this] val log = Log(classOf[ScalastyleSensor], "scalastyle")

  override def describe(descriptor: SensorDescriptor): Unit = {
    descriptor
      .createIssuesForRuleRepository(ScalastyleRulesRepository.RepositoryKey)
      .name(ScalastyleSensor.SensorName)
      .onlyOnFileType(InputFile.Type.MAIN)
      .onlyOnLanguage(Scala.LanguageKey)
      .onlyWhenConfiguration(ScalastyleSensor.shouldEnableSensor)
  }

  override def execute(context: SensorContext): Unit = {
    log.info("Initializing the Scalastyle sensor.")

    val activeRules: Seq[ActiveRule] =
      context
        .activeRules()
        .findByRepository(ScalastyleRulesRepository.RepositoryKey)
        .asScala
        .toIndexedSeq

    val checks: Map[String, Option[ConfigurationChecker]] =
      activeRules
        .map(r => r.ruleKey.rule -> ScalastyleSensor.ruleToConfigurationChecker(r))
        .toMap

    // Log a warning for invalid rules.
    checks.filter { case (_, conf) => conf.isEmpty } foreach {
      case (ruleKey, _) =>
        log.warn(
          s"Rule $ruleKey is missing the ${ScalastyleRulesRepository.RuleClassParam} parameter " +
          "and it will be skipped during the analysis."
        )
    }

    val config: ScalastyleConfiguration = new ScalastyleConfiguration(
      "SonarQube",
      commentFilter = true,
      checks.collect { case (_, Some(conf)) => conf }.toList // unNone
    )
    val fileSpecs: Seq[FileSpec] = ScalastyleSensor.fileSpecs(context)

    // Run Scalastyle analysis.
    val messages: Seq[Message[FileSpec]] = scalastyleChecker
      .checkFiles(new Checker(), config, fileSpecs)

    messages foreach {
      // Process each Scalastyle result.
      case styleError: StyleError[_] =>
        log.debug(s"Processing ${styleError.clazz} for file ${styleError.fileSpec}.")

        // Look up an active rule from the Scalastyle style error.
        val rule = ScalastyleSensor.ruleFromStyleError(context, styleError)

        rule.fold(
          log.warn(
            s"Scalastyle rule with key ${styleError.key} was not found in the default quality profile."
          )
        ) { rule =>
          ScalastyleSensor.openIssue(context, ScalastyleInspections.AllInspectionsByClass, styleError, rule)
        }

      case e: StyleException[_] =>
        log.error(s"Scalastyle exception (checker: ${e.clazz}, file: ${e.fileSpec.name}): ${e.message}.")
      case _ =>
        ()
    }
  }
}

private[scalastyle] object ScalastyleSensor {
  final val SensorName = "Scalastyle Sensor"
  final val ScalastyleDisablePropertyKey = "sonar.scala.scalastyle.disable"

  /**
   * Returns a bool flag indicating whether the sensor should be enabled.
   */
  def shouldEnableSensor(conf: Configuration): Boolean =
    conf
      .get(ScalastyleDisablePropertyKey)
      .toOption
      .forall(s => s.toLowerCase =!= "true")

  /**
   * Convert SonarQube rule severity to Scalastyle inspection level.
   */
  def severityToLevel(severity: Severity): Level = severity match {
    case Severity.INFO     => InfoLevel
    case Severity.MINOR    => WarningLevel
    case Severity.MAJOR    => ErrorLevel
    case Severity.CRITICAL => ErrorLevel
    case Severity.BLOCKER  => ErrorLevel
  }

  /**
   * Convert an active SonarQube rule to Scalastyle checker configuration.
   */
  def ruleToConfigurationChecker(rule: ActiveRule): Option[ConfigurationChecker] = {
    val params = rule.params.asScala.map { case (k, v) => k -> v.trim }.toMap
    val className: Option[String] = params.get(ScalastyleRulesRepository.RuleClassParam).filter(_.nonEmpty)
    className.map { className =>
      ConfigurationChecker(
        className,
        severityToLevel(Severity.valueOf(rule.severity)),
        enabled = true,
        params,
        customMessage = None,
        customId = Some(rule.ruleKey.rule)
      )
    }
  }

  /**
   * Get a list of files for analysis.
   */
  def fileSpecs(context: SensorContext): Seq[FileSpec] = {
    val predicates: FilePredicates = context.fileSystem.predicates
    val files: Iterable[File] = context.fileSystem
      .inputFiles(
        predicates.and(
          predicates.hasLanguage(Scala.LanguageKey),
          predicates.hasType(InputFile.Type.MAIN)
        )
      )
      .asScala
      .map(f => new File(f.uri)) // Avoiding here to use InputFile.file, which is deprecated.

    Directory.getFiles(Some(context.fileSystem.encoding.name), files)
  }

  /**
   *  Look up an active rule from the Scalastyle style error.
   */
  def ruleFromStyleError(context: SensorContext, styleError: StyleError[FileSpec]): Option[ActiveRule] =
    Option(
      context
        .activeRules()
        .find(RuleKey.of(ScalastyleRulesRepository.RepositoryKey, styleError.key))
    )

  /**
   * Open and new SonarQube issue for the given style error.
   */
  def openIssue(
    context: SensorContext,
    inspections: Map[String, ScalastyleInspection],
    styleError: StyleError[FileSpec],
    rule: ActiveRule
  ): Unit = {
    val predicates = context.fileSystem.predicates
    val relativized = context.fileSystem.baseDir.toPath.relativize(Paths.get(styleError.fileSpec.name))
    val file: InputFile = context.fileSystem.inputFile(predicates.hasPath(relativized.toString))
    val newIssue: NewIssue = context.newIssue().forRule(rule.ruleKey)
    val line: Int = styleError.lineNumber.filter(_ > 0).getOrElse(1) // scalastyle:ignore org.scalastyle.scalariform.NamedArgumentChecker
    val message: Option[String] = styleError.customMessage orElse inspections
      .get(styleError.clazz.getName)
      .map(_.label)

    // Open a new issue.
    newIssue
      .at(
        newIssue
          .newLocation()
          .on(file)
          .at(file.selectLine(line))
          .message(message.getOrElse(""))
      )
      .save()
  }
}
