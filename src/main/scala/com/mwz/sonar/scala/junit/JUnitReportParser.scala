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
package junit

import java.io.File
import java.nio.file.Path

import scala.jdk.CollectionConverters._
import scala.util.Try
import scala.xml.{Elem, XML}

import com.mwz.sonar.scala.util.Log
import org.sonar.api.batch.fs.{FilePredicate, FileSystem, InputFile}
import org.sonar.api.scanner.ScannerSide

trait JUnitReportParserAPI {

  /**
   * Parse JUnit report files from the given directory
   * and return a map from input files to the parsed reports.
   */
  def parse(tests: List[Path], directories: List[File]): Map[InputFile, JUnitReport]
}

@ScannerSide
final class JUnitReportParser(fileSystem: FileSystem) extends JUnitReportParserAPI {
  private[this] val log = Log(classOf[JUnitReportParser], "junit")

  def parse(tests: List[Path], directories: List[File]): Map[InputFile, JUnitReport] = {
    // Get report files - xml files starting with "TEST-".
    val reports: List[File] = reportFiles(directories)

    // Parse report files.
    val unitTestReports: List[JUnitReport] = parseReportFiles(reports)
    if (unitTestReports.nonEmpty)
      log.debug(s"JUnit test reports:\n${unitTestReports.mkString(", ")}")

    // Convert package names into files.
    resolveFiles(tests, unitTestReports)
  }

  /**
   * Get report files - xml files starting with "TEST-".
   */
  private[junit] def reportFiles(directories: List[File]): List[File] = {
    val reportFiles: List[File] =
      directories
        .filter(_.isDirectory)
        .flatMap { dir =>
          // Sbt creates files without the "TEST-" prefix unlike the mvn surefire plugin.
          // Also filter out the aggregate report starting with a "TESTS-" prefix.
          dir.listFiles((_, name) => !name.startsWith("TESTS-") && name.endsWith(".xml"))
        }

    if (directories.isEmpty)
      log.error(s"The paths ${directories.mkString(", ")} are not valid directories.")
    else if (reportFiles.isEmpty)
      log.error(s"No report files found in ${directories.mkString(", ")}.")

    reportFiles
  }

  /**
   * Parse report files.
   */
  private[junit] def parseReportFiles(reports: List[File]): List[JUnitReport] =
    reports.map { file =>
      val xml: Elem = XML.loadFile(file)
      JUnitReport(
        name = xml \@ "name",
        tests = Try((xml \@ "tests").toInt).toOption.getOrElse(0),
        errors = Try((xml \@ "errors").toInt).toOption.getOrElse(0),
        failures = Try((xml \@ "failures").toInt).toOption.getOrElse(0),
        skipped = Try((xml \@ "skipped").toInt).toOption.getOrElse(0),
        time = Try((xml \@ "time").toFloat).toOption.getOrElse(0)
      )
    }

  /**
   * Convert package names into files.
   */
  private[junit] def resolveFiles(
    tests: List[Path],
    reports: List[JUnitReport]
  ): Map[InputFile, JUnitReport] =
    reports
      .groupBy(_.name)
      .flatMap { case (name, reports) =>
        val path: String = name.replace(".", "/")
        val files: List[Path] = tests.map(_.resolve(s"$path.scala"))
        val predicates: List[FilePredicate] =
          files.map(f => fileSystem.predicates.hasPath(f.toString))

        val inputFiles: Iterable[InputFile] =
          fileSystem
            .inputFiles(
              fileSystem.predicates.or(predicates.asJava)
            )
            .asScala

        if (files.isEmpty)
          log.error(s"The following files were not found: ${files.mkString(", ")}")

        // Collect all of the input files.
        inputFiles.flatMap(file => reports.headOption.map((file, _)))
      }
}
