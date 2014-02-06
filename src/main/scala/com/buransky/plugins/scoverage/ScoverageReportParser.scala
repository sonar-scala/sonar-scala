package com.buransky.plugins.scoverage

trait ScoverageReportParser {
  def parse(): ProjectStatementCoverage
}

case class ScoverageException(message: String, source: Throwable = null)
  extends Exception(message, source)
