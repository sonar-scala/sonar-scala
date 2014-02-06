package com.buransky.plugins.scoverage.xml

import scala.io.Source
import com.buransky.plugins.scoverage.{ProjectStatementCoverage, ScoverageReportParser, ScoverageException}
import org.apache.log4j.Logger

class XmlScoverageReportParser extends ScoverageReportParser {
  private val log = Logger.getLogger(classOf[XmlScoverageReportParser])

  def parse(reportFilePath: String): ProjectStatementCoverage = {
    require(reportFilePath != null)
    require(!reportFilePath.trim.isEmpty)

    log.info("Parsing Scoverage report. [" + reportFilePath + "]")

    val parser = new XmlScoverageReportConstructingParser(sourceFromFile(reportFilePath))
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