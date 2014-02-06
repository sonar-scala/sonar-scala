package com.buransky.plugins.scoverage

trait ScoverageReportParser {
  def parse(reportFilePath: String): ProjectStatementCoverage
}

case class ScoverageException(message: String, source: Throwable = null)
  extends Exception(message, source)
