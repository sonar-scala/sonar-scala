/*
 * Sonar Scoverage Plugin
 * Copyright (C) 2013 Rado Buransky
 * dev@sonar.codehaus.org
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package com.buransky.plugins.scoverage.xml

import com.buransky.plugins.scoverage.util.LogUtil
import com.buransky.plugins.scoverage.{ProjectStatementCoverage, ScoverageException, ScoverageReportParser}
import org.sonar.api.utils.log.Loggers

import scala.io.Source
import com.buransky.plugins.scoverage.pathcleaner.PathSanitizer

/**
 * Bridge between parser implementation and coverage provider.
 *
 * @author Rado Buransky
 */
class XmlScoverageReportParser extends ScoverageReportParser {
  private val log = Loggers.get(classOf[XmlScoverageReportParser])

  def parse(reportFilePath: String, pathSanitizer: PathSanitizer): ProjectStatementCoverage = {
    require(reportFilePath != null)
    require(!reportFilePath.trim.isEmpty)

    log.debug(LogUtil.f("Reading report. [" + reportFilePath + "]"))

    val parser = new XmlScoverageReportConstructingParser(sourceFromFile(reportFilePath), pathSanitizer)
    parser.parse()
  }

  private def sourceFromFile(scoverageReportPath: String) = {
    try {
      Source.fromFile(scoverageReportPath)
    }
    catch {
      case ex: Exception => throw ScoverageException("Cannot parse file! [" + scoverageReportPath + "]", ex)
    }
  }
}

object XmlScoverageReportParser {
  def apply() = new XmlScoverageReportParser
}