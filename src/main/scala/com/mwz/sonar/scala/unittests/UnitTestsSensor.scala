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
package unittests

import java.io.File
import java.nio.file.Path

import com.mwz.sonar.scala.util.PathUtils._
import com.mwz.sonar.scala.util.{Log, MetricUtils}
import org.sonar.api.batch.fs.{FileSystem, InputFile}
import org.sonar.api.batch.sensor.{Sensor, SensorContext, SensorDescriptor}
import org.sonar.api.config.Configuration
import org.sonar.api.measures.CoreMetrics
import org.sonar.api.scan.filesystem.PathResolver

import scala.util.Try

final class UnitTestsSensor(
  untTestsReportParser: UnitTestReportParserAPI,
  settings: Configuration,
  fs: FileSystem,
  pathResolver: PathResolver
) extends Sensor {
  import UnitTestsSensor._ // scalastyle:ignore org.scalastyle.scalariform.ImportGroupingChecker

  private[this] val log = Log(classOf[UnitTestsSensor], "unit-tests")

  override def describe(descriptor: SensorDescriptor): Unit = {
    descriptor
      .name(SensorName)
      .onlyOnLanguage(Scala.LanguageKey)
  }

  override def execute(context: SensorContext): Unit = {
    log.info("Initializing the Scala unit tests sensor.")

    val tests: List[Path] = fromConfig(settings, TestsPropertyKey, DefaultTests)
    val reports: List[Path] = fromConfig(settings, ReportsPropertyKey, DefaultReportPaths)

    val inputFiles = context.fileSystem
      .inputFiles(
        context.fileSystem.predicates.and(
          context.fileSystem.predicates.hasLanguage(Scala.LanguageKey),
          context.fileSystem.predicates.hasType(InputFile.Type.TEST)
        )
      )

    log.debug("Input test files:")
    inputFiles.forEach(f => log.debug(f.toString))

    val directories: List[File] =
      reports.flatMap(path => Try(pathResolver.relativeFile(fs.baseDir, path.toString)).toOption)
    // TODO: Is the injected fileSystem different from context.fileSystem?

    if (directories.isEmpty)
      log.warn(s"Unit test report path(s) not found for ${reports.mkString(", ")}.")
    else {
      val parsedReports: Map[InputFile, UnitTestReport] = untTestsReportParser.parse(tests, directories)
      log.debug("Parsed reports:")
      log.debug(parsedReports.mkString(", "))

      // Save test metrics for each file.
      save(context, parsedReports)
    }
  }

  /**
   * Save test metrics.
   */
  private[unittests] def save(
    context: SensorContext,
    reports: Map[InputFile, UnitTestReport]
  ): Unit = {
    reports.foreach {
      case (file, report) =>
        log.debug(s"Saving unit test metrics for $file.")
        MetricUtils.save[Integer](context, file, CoreMetrics.SKIPPED_TESTS, report.skipped)
        MetricUtils.save[Integer](context, file, CoreMetrics.TESTS, report.tests - report.skipped)
        MetricUtils.save[Integer](context, file, CoreMetrics.TEST_ERRORS, report.errors)
        MetricUtils.save[Integer](context, file, CoreMetrics.TEST_FAILURES, report.failures)
        MetricUtils.save[java.lang.Long](
          context,
          file,
          CoreMetrics.TEST_EXECUTION_TIME,
          (report.time * 1000).longValue
        )
    }
  }
}

object UnitTestsSensor {
  val SensorName = "Scala Unit Tests Sensor"
  val TestsPropertyKey = "sonar.tests"
  val DefaultTests = "src/test/scala"
  val ReportsPropertyKey = "sonar.junit.reportPaths"
  val DefaultReportPaths = "target/test-reports"
}
