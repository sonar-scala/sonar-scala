package com.buransky.plugins.scoverage.xml

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{Matchers, FlatSpec}
import scala.io.Source
import com.buransky.plugins.scoverage.xml.data.XmlReportFile1
import scala._
import com.buransky.plugins.scoverage.FileStatementCoverage
import com.buransky.plugins.scoverage.DirectoryStatementCoverage

@RunWith(classOf[JUnitRunner])
class XmlScoverageReportConstructingParserSpec extends FlatSpec with Matchers {
  behavior of "parse source"

  it must "parse file1 correctly" in {
    parseFile1(XmlReportFile1.data)
  }

  it must "parse file1 correctly even without XML declaration" in {
    parseFile1(XmlReportFile1.dataWithoutDeclaration)
  }

  private def parseFile1(data: String) {
    val parser = new XmlScoverageReportConstructingParser(Source.fromString(data))
    val projectCoverage = parser.parse()

    // Assert coverage
    checkRate(24.53, projectCoverage.rate)

    // Assert structure
    projectCoverage.name should equal("")

    val projectChildren = projectCoverage.children.toList
    projectChildren.length should equal(1)
    projectChildren(0) shouldBe a [DirectoryStatementCoverage]

    val aaa = projectChildren(0).asInstanceOf[DirectoryStatementCoverage]
    aaa.name should equal("aaa")
    checkRate(24.53, aaa.rate)

    val aaaChildren = aaa.children.toList.sortBy(_.statementCount)
    aaaChildren.length should equal(2)

    aaaChildren(1) shouldBe a [FileStatementCoverage]
    val errorCode = aaaChildren(1).asInstanceOf[FileStatementCoverage]
    errorCode.name should equal("ErrorCode.scala")
    errorCode.statementCount should equal (46)
    errorCode.coveredStatementsCount should equal (13)

    aaaChildren(0) shouldBe a [FileStatementCoverage]
    val graph = aaaChildren(0).asInstanceOf[FileStatementCoverage]
    graph.name should equal("Graph.scala")
    graph.statementCount should equal (7)
    graph.coveredStatementsCount should equal (0)
  }

  private def checkRate(expected: Double, real: Double) {
    BigDecimal(real).setScale(2, BigDecimal.RoundingMode.HALF_UP).should(equal(BigDecimal(expected)))
  }
}
