package com.buransky.plugins.scoverage

trait ScoverageReportParser {
  def parse(scoverageReportPath: String): ProjectStatementCoverage
}
