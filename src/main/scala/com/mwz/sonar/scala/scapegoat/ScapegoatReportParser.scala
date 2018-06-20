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

import inspections.Level
import java.nio.file.Path
import scala.xml.XML

/** Used to mock the scoverage report parser in tests */
private[scapegoat] trait ScapegoatReportParserAPI {

  /** Parses the scapegoat xml report and returns all scapegoat warnings by filename */
  def parse(scapegoatReportPath: Path): Map[String, Seq[ScapegoatWarning]]
}

/** Scapegoat XML reports parser */
private[scapegoat] trait ScapegoatReportParser extends ScapegoatReportParserAPI {
  override final def parse(scapegoatReportPath: Path): Map[String, Seq[ScapegoatWarning]] = {
    val scapegoatXMLReport = XML.loadFile(scapegoatReportPath.toFile)

    val scapegoatWarnings = for {
      warning <- scapegoatXMLReport \\ "warning"
      line = (warning \@ "line").toInt
      text = warning \@ "text"
      snippet = warning \@ "snippet"
      level = Level.fromName(warning \@ "level")
      file = (warning \@ "file").replace(".", "/") // scalastyle:ignore LiteralArguments
      inspectionId = warning \@ "inspection"
    } yield ScapegoatWarning(line, text, snippet, level, file, inspectionId)

    scapegoatWarnings.groupBy(warning => warning.file)
  }
}
