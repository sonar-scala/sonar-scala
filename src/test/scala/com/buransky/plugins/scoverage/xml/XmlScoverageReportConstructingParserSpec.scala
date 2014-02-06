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
