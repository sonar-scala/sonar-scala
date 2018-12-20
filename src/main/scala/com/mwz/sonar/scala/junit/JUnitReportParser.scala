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
import java.nio.file.Path

import com.mwz.sonar.scala.util.Log
import org.sonar.api.batch.ScannerSide
import org.sonar.api.batch.fs.{FilePredicate, FileSystem, InputFile}

import scala.collection.JavaConverters._
import scala.xml.{Elem, XML}

trait JUnitReportParserAPI {
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

    log.debug("Unit test reports:")
    log.debug(unitTestReports.mkString(", "))

    // Convert package names into files.
    resolveFiles(tests, unitTestReports)
  }

  /**
   * Get report files - xml files starting with "TEST-"
   */
  private[junit] def reportFiles(directories: List[File]): List[File] = {
    val reportFiles: List[File] = directories.filter(_.isDirectory).flatMap { dir =>
      dir.listFiles((_, name) => name.startsWith("TEST-") && name.endsWith(".xml"))
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
  private[junit] def parseReportFiles(reports: List[File]): List[JUnitReport] = {
    reports.map { file =>
      val xml: Elem = XML.loadFile(file)
      JUnitReport(
        name = xml \@ "name",
        tests = (xml \@ "tests").toInt,
        errors = (xml \@ "errors").toInt,
        failures = (xml \@ "failures").toInt,
        skipped = (xml \@ "skipped").toInt,
        time = (xml \@ "time").toFloat
      )
    }
  }

  /**
   * Convert package names into files.
   */
  private[junit] def resolveFiles(
    tests: List[Path],
    reports: List[JUnitReport]
  ): Map[InputFile, JUnitReport] = {
    reports
      .groupBy(_.name)
      .map {
        case (name, reports) =>
          val filePath: String = name.replace(".", "/")
          val predicates: List[FilePredicate] =
            tests.map { path =>
              fileSystem.predicates.hasPath(path.resolve(s"$filePath.scala").toString)
            }
          val files: Iterable[InputFile] =
            fileSystem
              .inputFiles(
                fileSystem.predicates.or(predicates.asJava)
              )
              .asScala

          // Collect the first file here.
          files.headOption.flatMap(file => reports.headOption.map((file, _)))
      }
      .collect { case Some(e) => e } // unNone
      .toMap
  }
}
