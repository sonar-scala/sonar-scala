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

import com.mwz.sonar.scala.util.PathUtils

import scala.xml.{Node, XML}

/** Scoverage XML reports parser */
trait ScoverageReportParser extends ScoverageReportParserAPI {

  /** Parses the scoverage report from a file and returns the ModuleCoverage. */
  override def parse(
    scoverageReportPath: Path,
    modulePath: Path,
    sourcePrefixes: List[Path]
  ): ModuleCoverage = {
    val scoverageXMLReport = XML.loadFile(scoverageReportPath.toFile)
    val moduleScoverage = extractScoverageFromNode(scoverageXMLReport)

    val classCoverages = for {
      classNode <- scoverageXMLReport \\ "class"
      scoverageFilename = Paths.get(classNode \@ "filename")
      filename <- sourcePrefixes map { prefix =>
        // We call stripOutPrefix twice here to get the full path to the filenames from Scoverage report,
        // relative to the current module and the sources prefix.
        // E.g. both module1/sources/File.scala as well as sources/File.scala will return File.scala as a result.
        val filename = PathUtils.stripOutPrefix(
          PathUtils.stripOutPrefix(modulePath, prefix),
          scoverageFilename
        )
        (prefix, filename)
      } collectFirst {
        case (prefix, filename) if PathUtils.cwd.resolve(prefix).resolve(filename).toFile.exists =>
          prefix.resolve(filename).toString
      }
      classScoverage = extractScoverageFromNode(classNode)

      lines = for {
        statement <- classNode \\ "statement"
        if !(statement \@ "ignored").toBoolean
        lineNum = (statement \@ "line").toInt
        count = (statement \@ "invocation-count").toInt
      } yield lineNum -> count

      linesCoverage = lines groupBy {
        case (lineNum, _) => lineNum
      } mapValues { group =>
        val countsByLine = group map { case (_, count) => count }
        countsByLine.sum
      }

      classCoverage = FileCoverage(classScoverage, linesCoverage)
    } yield filename -> classCoverage

    // merge the class coverages by filename
    val files = classCoverages groupBy {
      case (fileName, _) => fileName
    } mapValues { group =>
      val classCoveragesByFilename = group map { case (_, classCoverage) => classCoverage }
      classCoveragesByFilename.reduce(_ + _)
    }

    ModuleCoverage(moduleScoverage, files)
  }

  /** Extracts the scoverage metrics form a class or module node */
  private def extractScoverageFromNode(node: Node): Scoverage =
    Scoverage(
      totalStatements = (node \@ "statement-count").toInt,
      coveredStatements = (node \@ "statements-invoked").toInt,
      statementCoverage = (node \@ "statement-rate").toDouble,
      branchCoverage = (node \@ "branch-rate").toDouble
    )
}
