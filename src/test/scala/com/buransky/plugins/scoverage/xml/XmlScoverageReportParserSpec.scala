package com.buransky.plugins.scoverage.xml

import org.scalatest.{FlatSpec, Matchers}
import org.scalatest.junit.JUnitRunner
import org.junit.runner.RunWith
import com.buransky.plugins.scoverage.xml.data.XmlReportFile1
import com.buransky.plugins.scoverage.ScoverageException
import scala.io.Source

@RunWith(classOf[JUnitRunner])
class XmlScoverageReportParserSpec extends FlatSpec with Matchers {
  behavior of "parse file path"

  it must "fail for null path" in {
    the[IllegalArgumentException] thrownBy XmlScoverageReportParser(null.asInstanceOf[String])
  }

  it must "fail for empty path" in {
    the[IllegalArgumentException] thrownBy XmlScoverageReportParser("")
  }

  it must "fail for not existing path" in {
    the[ScoverageException] thrownBy XmlScoverageReportParser("/x/a/b/c/1/2/3/4.xml")
  }

  behavior of "parse source"

  it must "work" in {
    val parser = new XmlScoverageReportParser(Source.fromString(XmlReportFile1.data))
    val projectCoverage = parser.parse()

    val expected = BigDecimal(24.53)
    BigDecimal(projectCoverage.rate).setScale(2, BigDecimal.RoundingMode.HALF_UP) should equal(expected)
  }
}
