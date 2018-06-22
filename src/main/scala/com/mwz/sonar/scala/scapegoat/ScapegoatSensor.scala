/*
 * Sonar Scala Plugin
 * Copyright (C) 2018 All contributors
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
package com.mwz.sonar.scala
package scapegoat

import inspections.ScapegoatInspection.AllScapegoatInspections

import cats.implicits._
import com.mwz.sonar.scala.util.JavaOptionals._
import com.mwz.sonar.scala.util.Log
import org.sonar.api.batch.fs.{FileSystem, InputFile}
import org.sonar.api.batch.rule.ActiveRule
import org.sonar.api.batch.sensor.{Sensor, SensorContext, SensorDescriptor}
import org.sonar.api.batch.sensor.issue.NewIssue
import org.sonar.api.config.Configuration
import scalariform.ScalaVersion

import java.nio.file.{Path, Paths}
import scala.util.{Failure, Success, Try}

/** Main sensor for importing Scapegoat reports to SonarQube */
final class ScapegoatSensor extends ScapegoatSensorInternal with ScapegoatReportParser

/** Implementation of the sensor */
private[scapegoat] abstract class ScapegoatSensorInternal extends Sensor {
  // cake pattern to mock the scapegoat report parser in tests
  scapegoatReportParser: ScapegoatReportParserAPI =>

  import ScapegoatSensorInternal._

  private[this] val log = Log(classOf[ScapegoatSensorInternal], "scapegoat")

  /** Populates the descriptor of this sensor */
  override def describe(descriptor: SensorDescriptor): Unit =
    descriptor
      .createIssuesForRuleRepository(ScapegoatRulesRepository.RepositoryKey)
      .name(SensorName)
      .onlyOnFileType(InputFile.Type.MAIN)
      .onlyOnLanguage(Scala.LanguageKey)

  /** Saves the Scapegoat information of a module */
  override def execute(context: SensorContext): Unit = {
    log.info("Initializing the scapegoat sensor.")

    val reportPath = getScapegoatReportPath(context.config)
    log.info(s"Loading the scapegoat report file: '$reportPath'.")
    Try(scapegoatReportParser.parse(reportPath)) match {
      case Success(scapegoatWarnings) =>
        log.info("Successfully loaded the scapegoat report file.")
        processScapegoatWarnings(context, scapegoatWarnings)
      case Failure(ex) =>
        log.error(
          "Aborting the scapegoat sensor execution, " +
          s"cause: an error occurred while reading the scapegoat report file: '$reportPath', " +
          s"the error was: ${ex.getMessage}."
        )
    }
  }

  /** Returns the path to the scapegoat report for this module */
  private[this] def getScapegoatReportPath(settings: Configuration): Path = {
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
    scapegoatWarnings: Map[String, Seq[ScapegoatIssue]]
  ): Unit = {
    val activeRules = context.activeRules
    val filesystem = context.fileSystem

    scapegoatWarnings foreach { tuple =>
      val (filename, warnings) = tuple
      log.info(s"Saving the scapegoat warnings of the file: '$filename'.")

      getModuleFile(filename, filesystem) match {
        case Some(file) =>
          warnings foreach { warning =>
            log.debug(s"Saving the warning: $warning.")

            // try to retrieve the SonarQube rule for this warning
            Option(
              activeRules.findByInternalKey(
                ScapegoatRulesRepository.RepositoryKey,
                warning.inspectionId
              )
            ) match {
              case Some(rule) =>
                // if the rule was found, create a new issue for it
                val issue = context.newIssue().forRule(rule.ruleKey)

                issue.at(
                  issue
                    .newLocation()
                    .on(file)
                    .at(file.selectLine(warning.line))
                    .message(warning.message)
                )

                issue.save()
              case None =>
                // if the rule was not found,
                // check if it is because it is not activated in the current quality profile,
                // or if it does not exist in the scapegoat rules repository
                if (AllScapegoatInspections.exists(inspection => inspection.id === warning.inspectionId))
                  log.debug(
                    s"The warning: ${warning.inspectionId}, " +
                    "was not activated in the current quality profile."
                  )
                else
                  log.warn(
                    s"The warning: ${warning.inspectionId}, " +
                    "does not exist in the scapegoat rules repository."
                  )
            }
          }
        case None =>
          log.error(s"The file: '$filename', couldn't be found.")
      }
    }
  }

  /** Returns the module input file with the given filename */
  private[this] def getModuleFile(filename: String, fs: FileSystem): Option[InputFile] = {
    val predicates = fs.predicates
    val predicate = predicates.and(
      predicates.hasLanguage(Scala.LanguageKey),
      predicates.hasType(InputFile.Type.MAIN),
      predicates.matchesPathPattern(s"**/$filename") // scalastyle:ignore LiteralArguments
    )

    // catch both exceptions and null values
    Try(fs.inputFile(predicate)).fold(_ => None, file => Option(file))
  }
}

private[scapegoat] object ScapegoatSensorInternal {
  private[scapegoat] val SensorName = "Scapegoat Sensor"
  private[scapegoat] val ScapegoatReportPathPropertyKey = "sonar.scala.scapegoat.reportPath"

  private[scapegoat] def getDefaultScapegoatReportPath(scalaVersion: ScalaVersion): Path =
    Paths.get(s"target/scala-${scalaVersion.major}.${scalaVersion.minor}/scapegoat-report/scapegoat.xml")
}
