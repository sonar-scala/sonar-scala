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
package scoverage

import java.nio.file.{Path, Paths}

import scala.xml.{Node, XML}

import cats.syntax.semigroup.catsSyntaxSemigroup
import com.mwz.sonar.scala.util.PathUtils
import org.sonar.api.scanner.ScannerSide

trait ScoverageReportParserAPI {
  def parse(scoverageReportPath: Path, projectPath: Path, sourcePrefixes: List[Path]): ProjectCoverage
}

/** Scoverage XML reports parser. */
@ScannerSide
final class ScoverageReportParser extends ScoverageReportParserAPI {

  /** Parses the scoverage report from a file and returns the ProjectCoverage. */
  override def parse(
    scoverageReportPath: Path,
    projectPath: Path,
    sourcePrefixes: List[Path]
  ): ProjectCoverage = {
    val scoverageXMLReport = XML.loadFile(scoverageReportPath.toFile)
    val projectScoverage = extractScoverageFromNode(scoverageXMLReport)

    val classCoverages = for {
      classNode <- scoverageXMLReport \\ "class"
      scoverageFilename = Paths.get(classNode \@ "filename")
      filename <- sourcePrefixes map { prefix =>
        // We call stripOutPrefix twice here to get the full path to the filenames from Scoverage report,
        // relative to the current project and the sources prefix.
        // E.g. both module1/sources/File.scala as well as sources/File.scala will return File.scala as a result.
        val filename = PathUtils.stripOutPrefix(
          PathUtils.stripOutPrefix(projectPath, prefix),
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

      linesCoverage = lines
        .groupMapReduce { case (lineNum, _) => lineNum } {
          case (_, count) => count
        }(_ + _)

      classCoverage = FileCoverage(classScoverage, linesCoverage)
    } yield filename -> classCoverage

    // Merge the class coverages by filename.
    val files =
      classCoverages
        .groupMapReduce { case (fileName, _) => fileName } {
          case (_, classCoverage) => classCoverage
        }(_ |+| _)

    ProjectCoverage(projectScoverage, files)
  }

  /** Extracts the scoverage metrics form a class or project node. */
  private[scoverage] def extractScoverageFromNode(node: Node): Scoverage =
    Scoverage(
      totalStatements = (node \@ "statement-count").toInt,
      coveredStatements = (node \@ "statements-invoked").toInt,
      statementCoverage = (node \@ "statement-rate").toDouble,
      branchCoverage = (node \@ "branch-rate").toDouble
    )
}
