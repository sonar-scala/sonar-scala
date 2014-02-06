package com.buransky.plugins.scoverage.xml

import com.buransky.plugins.scoverage._
import com.buransky.plugins.scoverage.ProjectStatementCoverage
import com.buransky.plugins.scoverage.CoveredStatement
import com.buransky.plugins.scoverage.StatementPosition
import com.buransky.plugins.scoverage.FileStatementCoverage
import com.buransky.plugins.scoverage.DirectoryStatementCoverage
import scala.io.Source

class StubScoverageReportParser extends ScoverageReportParser {
  def parse(): ProjectStatementCoverage = {
    val errorCodeFile = FileStatementCoverage("ErrorCode.scala", 17, 13,
      List(simpleStatement(10, 2), simpleStatement(11, 0),
        simpleStatement(25, 1)))

    val graphFile = FileStatementCoverage("Graph.scala", 42, 0,
      List(simpleStatement(33, 0), simpleStatement(3, 1), simpleStatement(1, 0), simpleStatement(2, 2)))

    val file2 = FileStatementCoverage("file2.scala", 2, 1, Nil)
    val bbbDir = DirectoryStatementCoverage("bbb", Seq(file2))

    val file1 = FileStatementCoverage("file1.scala", 100, 33, Nil)
    val aaaDir = DirectoryStatementCoverage("aaa", Seq(file1, errorCodeFile, graphFile, bbbDir))

    val project = ProjectStatementCoverage("project", Seq(aaaDir))

    project
  }

  def parse(source: Source): ProjectStatementCoverage = {
    ProjectStatementCoverage("x", Nil)
  }

  private def simpleStatement(line: Int, hitCount: Int): CoveredStatement =
    CoveredStatement(StatementPosition(line, 0), StatementPosition(line, 0), hitCount)

}
