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
package scoverage

import java.nio.file.{Path, Paths}

import com.mwz.sonar.scala.util.PathUtils._
import com.mwz.sonar.scala.util.syntax.Optionals._
import com.mwz.sonar.scala.util.{Log, PathUtils}
import org.sonar.api.batch.fs.{FileSystem, InputComponent, InputFile}
import org.sonar.api.batch.sensor.{Sensor, SensorContext, SensorDescriptor}
import org.sonar.api.config.Configuration
import scalariform.ScalaVersion

import scala.collection.JavaConverters._
import scala.util.{Failure, Success, Try}

/** Main sensor for importing Scoverage reports into SonarQube. */
final class ScoverageSensor(scoverageReportParser: ScoverageReportParserAPI) extends Sensor {
  import ScoverageSensor._ // scalastyle:ignore org.scalastyle.scalariform.ImportGroupingChecker

  private[this] val log = Log(classOf[ScoverageSensor], "scoverage")

  /** Populates the SensorDescriptor of this sensor. */
  override def describe(descriptor: SensorDescriptor): Unit = {
    descriptor
      .name(SensorName)
      .onlyOnLanguage(Scala.LanguageKey)
      .onlyOnFileType(InputFile.Type.MAIN)
      .onlyWhenConfiguration(shouldEnableSensor)
  }

  /** Saves in SonarQube the scoverage information of the project. */
  override def execute(context: SensorContext): Unit = {
    log.info("Initializing the scoverage sensor.")
    log.debug(s"The current working directory is: '${PathUtils.cwd}'.")
    val settings = context.config
    val filesystem = context.fileSystem

    val projectPath = getProjectBaseDirectory(filesystem)
    val reportPath = projectPath.resolve(getScoverageReportPath(settings))
    val sources = Scala.getSourcesPaths(settings)
    val sourcePrefixes = sources.map(PathUtils.relativize(PathUtils.cwd, projectPath, _))
    log.debug(s"The source prefixes are: ${sourcePrefixes.mkString("[", ",", "]")}.")
    log.info(s"Loading the scoverage report file: '$reportPath'.")

    Try(scoverageReportParser.parse(reportPath, projectPath, sourcePrefixes)) match {
      case Success(projectCoverage) =>
        log.info("Successfully loaded the scoverage report file.")
        log.debug(s"Project scoverage information: $projectCoverage.")

        // Save the coverage information of each file of the project.
        getProjectSourceFiles(filesystem) foreach { file =>
          val filename = PathUtils.cwd.relativize(Paths.get(file.uri())).toString
          log.debug(s"Saving the scoverage information of the file: '$filename'.")

          projectCoverage.filesCoverage.get(filename) match {
            case Some(fileCoverage) =>
              log.debug(s"File scoverage information: $fileCoverage.")

              // save the file overall scoverage information
              saveComponentScoverage(context, file, fileCoverage.fileScoverage)

              // save the coverage of each line of the file
              val coverage = context.newCoverage()
              coverage.onFile(file)
              fileCoverage.linesCoverage foreach {
                case (lineNum, hits) => coverage.lineHits(lineNum, hits)
              }
              coverage.save()
            case None =>
              log.warn(s"The file '$filename' has no scoverage information associated with it.")
          }
        }
      case Failure(ex) =>
        log.error(
          "Aborting the scoverage sensor execution, " +
          s"cause: an error occurred while reading the scoverage report file: '$reportPath', " +
          s"the error was: ${ex.getMessage}."
        )
    }
  }

  /** Returns all scala main files from this project. */
  private[scoverage] def getProjectSourceFiles(fs: FileSystem): Iterable[InputFile] = {
    val predicates = fs.predicates
    val predicate =
      predicates.and(
        predicates.hasLanguage(Scala.LanguageKey),
        predicates.hasType(InputFile.Type.MAIN)
      )

    fs.inputFiles(predicate).asScala
  }

  /** Returns the filename of the scoverage report for this project. */
  private[scoverage] def getScoverageReportPath(settings: Configuration): Path = {
    val scalaVersion = Scala.getScalaVersion(settings)
    val defaultScoverageReportPath = getDefaultScoverageReportPath(scalaVersion)

    Paths.get(
      settings
        .get(ScoverageReportPathPropertyKey)
        .toOption
        .getOrElse(defaultScoverageReportPath.toString)
    )
  }

  /** Saves the [[ScoverageMetrics]] of a component */
  private[scoverage] def saveComponentScoverage(
    context: SensorContext,
    component: InputComponent,
    scoverage: Scoverage
  ): Unit = {
    context
      .newMeasure[java.lang.Integer]()
      .on(component)
      .forMetric(ScoverageMetrics.totalStatements)
      .withValue(scoverage.totalStatements)
      .save()

    context
      .newMeasure[java.lang.Integer]()
      .on(component)
      .forMetric(ScoverageMetrics.coveredStatements)
      .withValue(scoverage.coveredStatements)
      .save()

    context
      .newMeasure[java.lang.Double]()
      .on(component)
      .forMetric(ScoverageMetrics.statementCoverage)
      .withValue(scoverage.statementCoverage)
      .save()

    context
      .newMeasure[java.lang.Double]()
      .on(component)
      .forMetric(ScoverageMetrics.branchCoverage)
      .withValue(scoverage.branchCoverage)
      .save()
  }
}

private[scoverage] object ScoverageSensor {
  final val SensorName = "Scoverage Sensor"
  final val ScoverageDisablePropertyKey = "sonar.scala.scoverage.disable"
  final val ScoverageReportPathPropertyKey = "sonar.scala.scoverage.reportPath"

  def shouldEnableSensor(conf: Configuration): Boolean =
    conf
      .get(ScoverageDisablePropertyKey)
      .toOption
      .forall(s => s.toLowerCase != "true")

  def getDefaultScoverageReportPath(scalaVersion: ScalaVersion): Path =
    Paths.get(s"target/scala-${scalaVersion.major}.${scalaVersion.minor}/scoverage-report/scoverage.xml")
}
