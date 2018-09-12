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

import java.nio.file.Path
import java.nio.file.Paths

import cats.implicits._
import com.mwz.sonar.scala.checkstyle.CheckstyleIssue
import com.mwz.sonar.scala.checkstyle.CheckstyleReportParserAPI
import com.mwz.sonar.scala.scapegoat.ScapegoatSensor.ScapegoatReportPathPropertyKey
import com.mwz.sonar.scala.scapegoat.inspections.ScapegoatInspection.AllScapegoatInspections
import com.mwz.sonar.scala.sensor.CheckstyleSensor
import com.mwz.sonar.scala.util.JavaOptionals._
import org.sonar.api.batch.fs.InputFile
import org.sonar.api.batch.rule.ActiveRule
import org.sonar.api.batch.rule.ActiveRules
import org.sonar.api.batch.sensor.SensorDescriptor
import org.sonar.api.config.Configuration

/** Main sensor for importing Scapegoat reports to SonarQube */
final class ScapegoatSensor(checkstyleReportParser: CheckstyleReportParserAPI)
    extends CheckstyleSensor(
      "scapegoat",
      ScapegoatReportPathPropertyKey,
      ScapegoatRulesRepository.RepositoryKey
    ) {

  import ScapegoatSensor._ // scalastyle:ignore scalastyle_ImportGroupingChecker

  override def parseReport(reportPath: Path): Map[String, Seq[CheckstyleIssue]] =
    checkstyleReportParser.parse(reportPath)

  override def defaultReportPath(settings: Configuration): Path = {
    val scalaVersion = Scala.getScalaVersion(settings)
    Paths.get(
      "target",
      s"scala-${scalaVersion.major}.${scalaVersion.minor}",
      "scapegoat-report",
      "scapegoat-scalastyle.xml"
    )
  }

  override def findSonarRule(activeRules: ActiveRules, issue: CheckstyleIssue): Option[ActiveRule] =
    Option(
      activeRules.findByInternalKey(
        ScapegoatRulesRepository.RepositoryKey,
        issue.inspectionClass
      )
    )

  override def sonarRuleNotFound(scapegoatIssue: CheckstyleIssue): Unit = {
    // if the rule was not found,
    // check if it is because the rule is not activated in the current quality profile,
    // or if it is because the inspection does not exist in the scapegoat rules repository
    val inspectionExists =
      AllScapegoatInspections.exists(
        inspection => inspection.id === scapegoatIssue.inspectionClass
      )
    if (inspectionExists)
      log.debug(
        s"The rule: ${scapegoatIssue.inspectionClass}, " +
        "was not activated in the current quality profile."
      )
    else
      log.warn(
        s"The inspection: ${scapegoatIssue.inspectionClass}, " +
        "does not exist in the scapegoat rules repository."
      )
  }

  /** Populates the descriptor of this sensor */
  override def describe(descriptor: SensorDescriptor): Unit =
    descriptor
      .createIssuesForRuleRepository(repositoryKey)
      .name(SensorName)
      .onlyOnFileType(InputFile.Type.MAIN)
      .onlyOnLanguage(Scala.LanguageKey)
      .onlyWhenConfiguration(shouldEnableSensor)

}

private[scapegoat] object ScapegoatSensor {
  final val SensorName = "Scapegoat Sensor"
  final val ScapegoatDisablePropertyKey = "sonar.scala.scapegoat.disable"
  final val ScapegoatReportPathPropertyKey = "sonar.scala.scapegoat.reportPath"

  def shouldEnableSensor(conf: Configuration): Boolean =
    conf
      .get(ScapegoatDisablePropertyKey)
      .toOption
      .forall(s => s.toLowerCase != "true")

}
