package com.buransky.plugins.scoverage.xml

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{Matchers, FlatSpec}
import scala.io.Source
import com.buransky.plugins.scoverage.xml.data.XmlReportFile1

@RunWith(classOf[JUnitRunner])
class XmlScoverageReportConstructingParserSpec extends FlatSpec with Matchers {
  behavior of "parse source"

  it must "work" in {
    val parser = new XmlScoverageReportConstructingParser(Source.fromString(XmlReportFile1.data))
    val projectCoverage = parser.parse()

    val expected = BigDecimal(24.53)
    BigDecimal(projectCoverage.rate).setScale(2, BigDecimal.RoundingMode.HALF_UP) should equal(expected)
  }
}
