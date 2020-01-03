/*
 * Copyright (C) 2018-2020  All sonar-scala contributors
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
package scapegoat

import java.nio.file.{Path, Paths}

import scala.util.{Failure, Success, Try}

import cats.instances.string._
import cats.syntax.eq._
import com.mwz.sonar.scala.pr.{GlobalIssues, Issue}
import com.mwz.sonar.scala.scapegoat.ScapegoatSensor._
import com.mwz.sonar.scala.util.Log
import com.mwz.sonar.scala.util.PathUtils._
import com.mwz.sonar.scala.util.syntax.Optionals._
import org.sonar.api.batch.fs.{FileSystem, InputFile}
import org.sonar.api.batch.rule.Severity
import org.sonar.api.batch.sensor.issue.{NewIssue, NewIssueLocation}
import org.sonar.api.batch.sensor.{Sensor, SensorContext, SensorDescriptor}
import org.sonar.api.config.Configuration
import scalariform.ScalaVersion

/** Main sensor for importing Scapegoat reports to SonarQube */
final class ScapegoatSensor(
  globalConfig: GlobalConfig,
  globalIssues: GlobalIssues,
  scapegoatReportParser: ScapegoatReportParserAPI
) extends Sensor {
  private[this] val log = Log(classOf[ScapegoatSensor], "scapegoat")

  /** Populates the descriptor of this sensor */
  override def describe(descriptor: SensorDescriptor): Unit =
    descriptor
      .createIssuesForRuleRepository(ScapegoatRulesRepository.RepositoryKey)
      .name(SensorName)
      .onlyOnFileType(InputFile.Type.MAIN)
      .onlyOnLanguage(Scala.LanguageKey)
      .onlyWhenConfiguration(shouldEnableSensor)

  /** Saves the Scapegoat information of the project. */
  override def execute(context: SensorContext): Unit = {
    val projectPath = getProjectBaseDirectory(context.fileSystem)
    val reportPath = projectPath.resolve(getScapegoatReportPath(context.config))

    log.info("Initializing the Scapegoat sensor.")
    log.info(s"Loading the scapegoat report file: '$reportPath'.")
    log.debug(s"The current working directory is: '$cwd'.")

    Try(scapegoatReportParser.parse(reportPath)) match {
      case Success(scapegoatIssuesByFilename) =>
        log.info("Successfully loaded the scapegoat report file.")
        processScapegoatWarnings(context, scapegoatIssuesByFilename)
      case Failure(ex) =>
        log.error(
          "Aborting the scapegoat sensor execution, " +
          s"cause: an error occurred while reading the scapegoat report file: '$reportPath', " +
          s"the error was: ${ex.getMessage}."
        )
    }
  }

  /** Returns the path to the scapegoat report for this project. */
  private[scapegoat] def getScapegoatReportPath(settings: Configuration): Path = {
    val scalaVersion = Scala.getScalaVersion(settings)
    val defaultScapegoatReportPath = getDefaultScapegoatReportPath(scalaVersion)

    if (!settings.hasKey(ScapegoatReportPathPropertyKey)) {
      log.info(
        s"Missing the property: '$ScapegoatReportPathPropertyKey', " +
        s"using the default value: '$defaultScapegoatReportPath'."
      )
    }

    settings
      .get(ScapegoatReportPathPropertyKey)
      .toOption
      .map(path => Paths.get(path))
      .getOrElse(defaultScapegoatReportPath)
  }

  /** Process all scapegoat warnings */
  private[this] def processScapegoatWarnings(
    context: SensorContext,
    scapegoatIssuesByFilename: Map[String, Seq[ScapegoatIssue]]
  ): Unit = {
    val activeRules = context.activeRules
    val filesystem = context.fileSystem

    scapegoatIssuesByFilename foreach { tuple =>
      val (filename, scapegoatIssues) = tuple
      val relativeFile =
        if (Paths.get(filename).isAbsolute)
          context.fileSystem.baseDir.toPath.relativize(Paths.get(filename)).toString
        else filename
      log.info(s"Saving the scapegoat issues for file '$relativeFile'.")

      getProjectFile(relativeFile, filesystem) match {
        case Some(file) =>
          scapegoatIssues foreach { scapegoatIssue =>
            log.debug(s"Saving the scapegoat issue: $scapegoatIssue.")

            // try to retrieve the SonarQube rule for this scapegoat issue
            Option(
              activeRules.findByInternalKey(
                ScapegoatRulesRepository.RepositoryKey,
                scapegoatIssue.inspectionId
              )
            ) match {
              case Some(rule) =>
                // if the rule was found, create a new sonarqube issue for it
                val newIssue: NewIssue = context.newIssue().forRule(rule.ruleKey)
                val location: NewIssueLocation =
                  newIssue
                    .newLocation()
                    .on(file)
                    .at(file.selectLine(scapegoatIssue.line))
                    .message(scapegoatIssue.message)

                // Open a new issue (if not in pr decoration mode).
                if (!globalConfig.prDecoration)
                  newIssue.at(location).save()

                // Keep track of the issues (if not disabled).
                else if (globalConfig.issueDecoration) {
                  val issue: Issue =
                    Issue(
                      rule.ruleKey,
                      file,
                      scapegoatIssue.line,
                      Severity.valueOf(rule.severity),
                      scapegoatIssue.text
                    )
                  globalIssues.add(issue)
                }
              case None =>
                // if the rule was not found,
                // check if it is because the rule is not activated in the current quality profile,
                // or if it is because the inspection does not exist in the scapegoat rules repository
                val inspectionExists =
                  ScapegoatInspections.AllInspections.exists(inspection =>
                    inspection.id === scapegoatIssue.inspectionId
                  )
                if (inspectionExists)
                  log.debug(
                    s"The rule: ${scapegoatIssue.inspectionId}, " +
                    "was not activated in the current quality profile."
                  )
                else
                  log.warn(
                    s"The inspection: ${scapegoatIssue.inspectionId}, " +
                    "does not exist in the scapegoat rules repository."
                  )
            }
          }
        case None =>
          log.error(s"The file '$relativeFile' couldn't be found.")
      }
    }
  }

  /** Returns the project input file with the given filename. */
  private[scapegoat] def getProjectFile(filename: String, fs: FileSystem): Option[InputFile] = {
    val predicates = fs.predicates
    val pathPattern = s"**/$filename"
    val predicate =
      predicates.and(
        predicates.hasLanguage(Scala.LanguageKey),
        predicates.hasType(InputFile.Type.MAIN),
        predicates.matchesPathPattern(pathPattern)
      )

    // Catch both exceptions and null values.
    Try(fs.inputFile(predicate)).fold(_ => None, file => Option(file))
  }
}

private[scapegoat] object ScapegoatSensor {
  final val SensorName = "Scapegoat Sensor"
  final val ScapegoatDisablePropertyKey = "sonar.scala.scapegoat.disable"
  final val ScapegoatReportPathPropertyKey = "sonar.scala.scapegoat.reportPath"

  def shouldEnableSensor(conf: Configuration): Boolean =
    conf
      .get(ScapegoatDisablePropertyKey)
      .toOption
      .forall(s => s.toLowerCase =!= "true")

  def getDefaultScapegoatReportPath(scalaVersion: ScalaVersion): Path =
    Paths.get(
      "target",
      s"scala-${scalaVersion.major}.${scalaVersion.minor}",
      "scapegoat-report",
      "scapegoat.xml"
    )
}
