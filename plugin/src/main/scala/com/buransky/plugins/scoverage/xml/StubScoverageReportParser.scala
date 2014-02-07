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

import com.buransky.plugins.scoverage._
import com.buransky.plugins.scoverage.ProjectStatementCoverage
import com.buransky.plugins.scoverage.CoveredStatement
import com.buransky.plugins.scoverage.StatementPosition
import com.buransky.plugins.scoverage.FileStatementCoverage
import com.buransky.plugins.scoverage.DirectoryStatementCoverage
import scala.io.Source

/**
 * Stub with some dummy data so that we don't have to parse XML for testing.
 *
 * @author Rado Buransky
 */
class StubScoverageReportParser extends ScoverageReportParser {
  def parse(reportFilePath: String): ProjectStatementCoverage = {
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
