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
import com.buransky.plugins.scoverage.{ProjectStatementCoverage, FileStatementCoverage, DirectoryStatementCoverage}
import com.buransky.plugins.scoverage.pathcleaner.PathSanitizer
import com.buransky.plugins.scoverage.StatementCoverage
import com.buransky.plugins.scoverage.NodeStatementCoverage

@RunWith(classOf[JUnitRunner])
class XmlScoverageReportConstructingParserSpec extends FlatSpec with Matchers {
  behavior of "parse source"

  it must "parse empty coverage file correctly" in {
    val sanitizer = new PathSanitizer() {
      def getSourceRelativePath(path: Seq[String]): Option[Seq[String]] = {
        // do nothing
        Some(path)
      }
    }
    assertReportFile(XmlReportFile1.emptyCoverage, 0, sanitizer)(assertEmptyCoverage)
  }

  it must "parse old broken Scoverage 0.95 file correctly" in {
    val sanitizer = new PathSanitizer() {
      def getSourceRelativePath(path: Seq[String]): Option[Seq[String]] = {
        // do nothing
        Some(path)
      }      
    }
    assertReportFile(XmlReportFile1.scoverage095Data, 24.53, sanitizer)(assertScoverage095Data)
  }

  it must "parse new fixed Scoverage 1.0.4 file correctly" in {
    val sanitizer = new PathSanitizer() {
      def getSourceRelativePath(path: Seq[String]): Option[Seq[String]] = {
        // drop first 6 = /a1b2c3/workspace/sonar-test/src/main/scala
        Some(path.drop(6))
      }      
    }    
    assertReportFile(XmlReportFile1.scoverage104Data, 50.0, sanitizer) { projectCoverage =>
      assert(projectCoverage.name === "")
      assert(projectCoverage.children.size.toInt === 1)

      projectCoverage.children.head match {
        case rootDir: DirectoryStatementCoverage => { 
          val rr = checkNode(rootDir, "com", 0, 0, 0.0).head
          val test = checkNode(rr, "rr", 0, 0, 0.0).head
          val sonar = checkNode(test, "test", 0, 0, 0.0).head
          val mainClass = checkNode(sonar, "sonar", 2, 1, 50.0).head
      
          checkNode(mainClass, "MainClass.scala", 2, 1, 50.0)
        }
        case other => fail(s"This is not a directory statement coverage! [$other]")
      }
    }
  }

  it must "parse file1 correctly even without XML declaration" in {
    val sanitizer = new PathSanitizer() {
      def getSourceRelativePath(path: Seq[String]): Option[Seq[String]] = {
        // do nothing
        Some(path)
      }      
    }    
    assertReportFile(XmlReportFile1.dataWithoutDeclaration, 24.53, sanitizer)(assertScoverage095Data)
  }

  private def assertReportFile(data: String, expectedCoverage: Double, pathSanitizer: PathSanitizer)(f: (ProjectStatementCoverage) => Unit) {
    val parser = new XmlScoverageReportConstructingParser(Source.fromString(data), pathSanitizer)
    val projectCoverage = parser.parse()

    // Assert coverage
    checkRate(expectedCoverage, projectCoverage.rate)

    f(projectCoverage)
  }

  private def assertScoverage095Data(projectCoverage: ProjectStatementCoverage): Unit = {
    // Assert structure
    projectCoverage.name should equal("")

    val projectChildren = projectCoverage.children.toList
    projectChildren.length should equal(1)
    projectChildren.head shouldBe a [DirectoryStatementCoverage]

    val aaa = projectChildren.head.asInstanceOf[DirectoryStatementCoverage]
    aaa.name should equal("aaa")
    checkRate(24.53, aaa.rate)

    val aaaChildren = aaa.children.toList.sortBy(_.statementCount)
    aaaChildren.length should equal(2)

    aaaChildren(1) shouldBe a [FileStatementCoverage]
    val errorCode = aaaChildren(1).asInstanceOf[FileStatementCoverage]
    errorCode.name should equal("ErrorCode.scala")
    errorCode.statementCount should equal (46)
    errorCode.coveredStatementsCount should equal (13)

    aaaChildren.head shouldBe a [FileStatementCoverage]
    val graph = aaaChildren.head.asInstanceOf[FileStatementCoverage]
    graph.name should equal("Graph.scala")
    graph.statementCount should equal (7)
    graph.coveredStatementsCount should equal (0)
  }

  private def checkRate(expected: Double, real: Double) {
    BigDecimal(real).setScale(2, BigDecimal.RoundingMode.HALF_UP).should(equal(BigDecimal(expected)))
  }
  
  private def checkNode(node: NodeStatementCoverage, name: String, count: Int, covered: Int, rate: Double): Iterable[NodeStatementCoverage] = {
    node.name shouldEqual name
    node.statementCount shouldEqual count
    node.coveredStatementsCount shouldEqual covered
    
    checkRate(rate, node.rate)
    
    node.children
  }

  private def assertEmptyCoverage(projectCoverage: ProjectStatementCoverage): Unit = {
    // Assert structure
    projectCoverage.name should equal("")

    val projectChildren = projectCoverage.children.toList
    projectChildren.length should equal(0)
    projectCoverage.coveredStatementsCount should equal(0)
    projectCoverage.statementCount should equal(0)
    projectCoverage.coveredStatementsCount should equal(0)
  }
}
