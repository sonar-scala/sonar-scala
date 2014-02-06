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
    the[IllegalArgumentException] thrownBy XmlScoverageReportParser().parse(null.asInstanceOf[String])
  }

  it must "fail for empty path" in {
    the[IllegalArgumentException] thrownBy XmlScoverageReportParser().parse("")
  }

  it must "fail for not existing path" in {
    the[ScoverageException] thrownBy XmlScoverageReportParser().parse("/x/a/b/c/1/2/3/4.xml")
  }
}
