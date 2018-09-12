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
package com.mwz.sonar.scala.checkstyle

import org.sonar.api.batch.ScannerSide
import java.nio.file.Path

import scala.xml.XML
import com.mwz.sonar.scala.checkstyle.CheckstyleReportParserAPI._

import scala.collection.immutable

trait CheckstyleReportParserAPI {
  def parse(checkstyleReportPath: Path): Map[FILEPATH, Seq[CheckstyleIssue]]
}

object CheckstyleReportParserAPI {
  type FILEPATH = String
}

/** Checkstyle XML reports parser */
@ScannerSide
final class CheckstyleReportParser extends CheckstyleReportParserAPI {

  /** Parses the checkstyle *.xml report and returns all checkstyle issues by filename */
  override def parse(scapegoatReportPath: Path): Map[String, Seq[CheckstyleIssue]] = {
    val checkstyleXMLReport = XML.loadFile(scapegoatReportPath.toFile)

    (checkstyleXMLReport \ "file").map { fileNode =>
      val errors: immutable.Seq[CheckstyleIssue] = (fileNode \ "error").map { error =>
        CheckstyleIssue(
          line = (error \@ "line").toInt,
          column = Option(error \@ "column").filterNot(_.isEmpty).map(_.toInt),
          snippet = error \@ "source",
          severity = error \@ "severity",
          message = error \@ "message"
        )
      }

      (fileNode \@ "name") -> errors
    }.toMap
  }
}
