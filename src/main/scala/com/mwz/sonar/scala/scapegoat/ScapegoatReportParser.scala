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
package com.mwz.sonar.scala.scapegoat

import org.sonar.api.batch.ScannerSide
import java.nio.file.{Path, Paths}

import scala.xml.XML

trait ScapegoatReportParserAPI {
  def parse(scapegoatReportPath: Path): Map[String, Seq[ScapegoatIssue]]
}

/** Scapegoat XML reports parser */
@ScannerSide
final class ScapegoatReportParser extends ScapegoatReportParserAPI {
  private[this] val AllDotsButLastRegex = raw"\.(?=.*\.)".r

  /**
   * Replaces all dots '.' except the last one in a scapegoat path with slashes '/'
   * while keeping valid directories which contain '.' in their name.
   */
  private[scapegoat] def replaceAllDotsButLastWithSlashes(path: String): String =
    if (path.startsWith(".")) {
      path.split('.').reduceLeft[String] {
        case (acc, str) =>
          val s = Option(str).filter(_.nonEmpty).getOrElse("/")
          val a = Option(acc).filter(_.nonEmpty).getOrElse("/")
          if (Paths.get(a).toFile.exists)
            Paths.get(a).resolve(s).toString
          else s"$a.$s"
      }
    } else AllDotsButLastRegex.replaceAllIn(target = path, replacement = "/")

  /** Parses the scapegoat xml report and returns all scapegoat issues by filename */
  override def parse(scapegoatReportPath: Path): Map[String, Seq[ScapegoatIssue]] = {
    val scapegoatXMLReport = XML.loadFile(scapegoatReportPath.toFile)

    val scapegoatIssues = for {
      issue <- scapegoatXMLReport \\ "warning"
      line = (issue \@ "line").toInt
      text = issue \@ "text"
      snippet = issue \@ "snippet"
      file = replaceAllDotsButLastWithSlashes(issue \@ "file")
      inspectionId = issue \@ "inspection"
    } yield ScapegoatIssue(line, text, snippet, file, inspectionId)

    scapegoatIssues.groupBy(issue => issue.file)
  }
}
