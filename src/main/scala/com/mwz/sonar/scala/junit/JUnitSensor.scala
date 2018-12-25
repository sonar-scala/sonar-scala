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
package junit

import java.io.File
import java.nio.file.{Path, Paths}

import cats.instances.list._
import com.mwz.sonar.scala.util.Log
import com.mwz.sonar.scala.util.syntax.SonarConfig._
import com.mwz.sonar.scala.util.syntax.SonarFileSystem._
import com.mwz.sonar.scala.util.syntax.SonarSensorContext._
import org.sonar.api.batch.fs.{FileSystem, InputFile}
import org.sonar.api.batch.sensor.{Sensor, SensorContext, SensorDescriptor}
import org.sonar.api.config.Configuration
import org.sonar.api.measures.CoreMetrics

import scala.collection.JavaConverters._

/**
 * Scala JUnit sensor.
 * Parses JUnit XML reports and saves test metrics.
 */
final class JUnitSensor(
  config: Configuration,
  fs: FileSystem,
  untTestsReportParser: JUnitReportParserAPI
) extends Sensor {
  import JUnitSensor._ // scalastyle:ignore org.scalastyle.scalariform.ImportGroupingChecker

  private[this] val log = Log(classOf[JUnitSensor], "junit")

  override def describe(descriptor: SensorDescriptor): Unit = {
    descriptor
      .name(SensorName)
      .onlyOnLanguage(Scala.LanguageKey)
      .onlyOnFileType(InputFile.Type.TEST)
  }

  override def execute(context: SensorContext): Unit = {
    log.info("Initializing the Scala JUnit sensor.")

    // Get the test paths.
    val tests: List[Path] = testPaths(config)
    log.debug(s"The tests prefixes are: ${tests.mkString("[", ",", "]")}.")

    // Get the junit report paths.
    val reports: List[Path] = reportPaths(config)
    log.debug(s"The JUnit report paths are: ${reports.mkString("[", ",", "]")}.")

    // Get test input files
    val inputFiles: Iterable[InputFile] =
      context.fileSystem
        .inputFiles(
          context.fileSystem.predicates.and(
            context.fileSystem.predicates.hasLanguage(Scala.LanguageKey),
            context.fileSystem.predicates.hasType(InputFile.Type.TEST)
          )
        )
        .asScala

    if (inputFiles.nonEmpty)
      log.debug(s"Input test files: \n${inputFiles.mkString(", ")}")
    else
      log.warn(s"No test files found for module ${context.module.key}.")

    // Resolve test directories.
    val testDirectories: List[File] = fs.resolve(tests)
    if (testDirectories.isEmpty)
      log.error(s"The following test directories were not found: ${reports.mkString(", ")}.")

    // Resolve JUnit report directories.
    val reportDirectories: List[File] = fs.resolve(reports)
    if (reportDirectories.isEmpty)
      log.error(s"The following JUnit test report path(s) were not found : ${reports.mkString(", ")}.")

    // Parse the reports.
    val parsedReports: Map[InputFile, JUnitReport] = untTestsReportParser.parse(tests, reportDirectories)

    // Save test metrics for each file.
    save(context, parsedReports)
  }

  /**
   * Save test metrics.
   */
  private[junit] def save(
    context: SensorContext,
    reports: Map[InputFile, JUnitReport]
  ): Unit = {
    if (reports.nonEmpty)
      log.debug(s"Parsed reports:\n${reports.mkString(", ")}")
    else
      log.info("No test metrics were saved by this sensor.")

    reports.foreach {
      case (file, report) =>
        log.info(s"Saving junit test metrics for $file.")
        context.saveMeasure[Integer](file, CoreMetrics.SKIPPED_TESTS, report.skipped)
        context.saveMeasure[Integer](file, CoreMetrics.TESTS, report.tests - report.skipped)
        context.saveMeasure[Integer](file, CoreMetrics.TEST_ERRORS, report.errors)
        context.saveMeasure[Integer](file, CoreMetrics.TEST_FAILURES, report.failures)
        context.saveMeasure[java.lang.Long](
          file,
          CoreMetrics.TEST_EXECUTION_TIME,
          (report.time * 1000).longValue
        )
    }
  }
}

object JUnitSensor {
  val SensorName = "Scala JUnit Sensor"
  val TestsPropertyKey = "sonar.tests"
  val DefaultTests = List(Paths.get("src/test/scala"))
  val ReportsPropertyKey = "sonar.junit.reportPaths"
  val DefaultReportPaths = List(Paths.get("target/test-reports"))

  private[junit] def testPaths(conf: Configuration): List[Path] =
    conf.getPaths(TestsPropertyKey, DefaultTests)

  private[junit] def reportPaths(conf: Configuration): List[Path] =
    conf.getPaths(ReportsPropertyKey, DefaultReportPaths)
}
