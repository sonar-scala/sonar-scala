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
package scoverage

import java.nio.file.{Path, Paths}

import com.mwz.sonar.scala.util.JavaOptionals._
import com.mwz.sonar.scala.util.{Log, PathUtils}
import org.sonar.api.batch.fs.{FileSystem, InputComponent, InputFile}
import org.sonar.api.batch.sensor.{Sensor, SensorContext, SensorDescriptor}
import org.sonar.api.config.Configuration
import scalariform.ScalaVersion

import scala.collection.JavaConverters._
import scala.util.{Failure, Success, Try}

/** Main sensor for importing Scoverage reports into SonarQube. */
final class ScoverageSensor(scoverageReportParser: ScoverageReportParserAPI) extends Sensor {
  import ScoverageSensor._ // scalastyle:ignore scalastyle_ImportGroupingChecker

  private[this] val log = Log(classOf[ScoverageSensor], "scoverage")

  /** Populates the SensorDescriptor of this sensor. */
  override def describe(descriptor: SensorDescriptor): Unit = {
    descriptor
      .onlyOnLanguage(Scala.LanguageKey)
      .onlyOnFileType(InputFile.Type.MAIN)
      .name(SensorName)
  }

  /** Saves in SonarQube the scoverage information of a module */
  override def execute(context: SensorContext): Unit = {
    log.info("Initializing the scoverage sensor.")
    log.debug(s"The current working directory (CWD) is: '${PathUtils.cwd}'.")
    val settings = context.config
    val filesystem = context.fileSystem

    val modulePath = getModuleBaseDirectory(filesystem)
    val reportPath = modulePath.resolve(getScoverageReportPath(settings))
    val sources = Scala.getSourcesPaths(settings)
    val sourcePrefixes = sources.map(PathUtils.relativize(PathUtils.cwd, modulePath, _))
    log.debug(s"The source prefixes are: ${sourcePrefixes.mkString("[", ",", "]")}.")
    log.info(s"Loading the scoverage report file: '$reportPath'.")

    Try(scoverageReportParser.parse(reportPath, modulePath, sourcePrefixes)) match {
      case Success(moduleCoverage) =>
        log.info("Successfully loaded the scoverage report file.")
        log.debug(s"Module scoverage information: $moduleCoverage.")

        saveComponentScoverage(context, context.module(), moduleCoverage.moduleScoverage)

        // save the coverage information of each file of the module
        getModuleSourceFiles(filesystem) foreach { file =>
          // toString returns the project relative path of the file
          val filename = file.toString
          log.debug(s"Saving the scoverage information of the file: '$filename'.")

          moduleCoverage.filesCoverage.get(filename) match {
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

  /** Returns all scala main files from this module */
  private[scoverage] def getModuleSourceFiles(fs: FileSystem): Iterable[InputFile] = {
    val predicates = fs.predicates
    val predicate =
      predicates.and(
        predicates.hasLanguage(Scala.LanguageKey),
        predicates.hasType(InputFile.Type.MAIN)
      )

    fs.inputFiles(predicate).asScala
  }

  /** Returns the module base path */
  private[scoverage] def getModuleBaseDirectory(fs: FileSystem): Path = {
    val moduleAbsolutePath = Paths.get(fs.baseDir().getAbsolutePath).normalize
    val currentWorkdirAbsolutePath = PathUtils.cwd
    currentWorkdirAbsolutePath.relativize(moduleAbsolutePath)
  }

  /** Returns the filename of the scoverage report for this module */
  private[scoverage] def getScoverageReportPath(settings: Configuration): Path = {
    val scalaVersion = Scala.getScalaVersion(settings)
    val defaultScoverageReportPath = getDefaultScoverageReportPath(scalaVersion)

    if (settings.hasKey(DeprecatedScoverageReportPathPropertyKey)) {
      log.warn(
        s"The property: '$DeprecatedScoverageReportPathPropertyKey' is deprecated, " +
        s"use the new property '$ScoverageReportPathPropertyKey' instead."
      )
    } else if (!settings.hasKey(ScoverageReportPathPropertyKey)) {
      log.info(
        s"Missing the property: '$ScoverageReportPathPropertyKey', " +
        s"using the default value: '$defaultScoverageReportPath'."
      )
    }

    Paths.get(
      settings
        .get(DeprecatedScoverageReportPathPropertyKey)
        .toOption
        .getOrElse(
          settings
            .get(ScoverageReportPathPropertyKey)
            .toOption
            .getOrElse(defaultScoverageReportPath.toString)
        )
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
  val SensorName = "Scoverage Sensor"
  val DeprecatedScoverageReportPathPropertyKey = "sonar.scoverage.reportPath"
  val ScoverageReportPathPropertyKey = "sonar.scala.scoverage.reportPath"

  def getDefaultScoverageReportPath(scalaVersion: ScalaVersion): Path =
    Paths.get(s"target/scala-${scalaVersion.major}.${scalaVersion.minor}/scoverage-report/scoverage.xml")
}
