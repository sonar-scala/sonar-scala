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
package com.buransky.plugins.scoverage

/**
 * Statement coverage represents rate at which are statements of a certain source code unit
 * being covered by tests.
 *
 * @author Rado Buransky
 */
sealed trait StatementCoverage {
  /**
   * Percentage rate ranging from 0 up to 100%.
   */
  lazy val rate: Double =
    if (statementCount == 0)
      0.0
    else
      (coveredStatementsCount.toDouble / statementCount.toDouble) * 100.0

  /**
   * Total number of all statements within the source code unit,
   */
  def statementCount: Int

  /**
   * Number of statements covered by unit tests.
   */
  def coveredStatementsCount: Int

  require(statementCount >= 0, "Statements count cannot be negative! [" + statementCount + "]")
  require(coveredStatementsCount >= 0, "Statements count cannot be negative! [" +
    coveredStatementsCount + "]")
  require(coveredStatementsCount <= statementCount,
    "Number of covered statements cannot be more than total number of statements! [" +
      statementCount + ", " + coveredStatementsCount + "]")
}

/**
 * Allows to build tree structure from state coverage values.
 */
trait NodeStatementCoverage extends StatementCoverage {
  def name: String
  def children: Iterable[NodeStatementCoverage]
  def statementSum: Int = children.map(_.statementSum).sum
  def coveredStatementsSum: Int = children.map(_.coveredStatementsSum).sum
}

/**
 * Root node. In multi-module projects it can contain other ProjectStatementCoverage
 * elements as children.
 */
case class ProjectStatementCoverage(name: String, children: Iterable[NodeStatementCoverage])
  extends NodeStatementCoverage {
  // projects' coverage values are defined as sums of their child values
  val statementCount = statementSum
  val coveredStatementsCount = coveredStatementsSum
}

/**
 * Physical directory in file system.
 */
case class DirectoryStatementCoverage(name: String, children: Iterable[NodeStatementCoverage])
  extends NodeStatementCoverage {
  // directories' coverage values are defined as sums of their DIRECT child values 
  val statementCount = children.filter(_.isInstanceOf[FileStatementCoverage]).map(_.statementCount).sum
  val coveredStatementsCount = children.filter(_.isInstanceOf[FileStatementCoverage]).map(_.coveredStatementsCount).sum
}  

/**
 * Scala source code file.
 */
case class FileStatementCoverage(name: String, statementCount: Int, coveredStatementsCount: Int,
                                 statements: Iterable[CoveredStatement]) extends NodeStatementCoverage {
  // leaf implementation sums==values
  val children = List.empty[NodeStatementCoverage]
  override val statementSum = statementCount
  override val coveredStatementsSum = coveredStatementsCount
}

/**
 * Position a Scala source code file.
 */
case class StatementPosition(line: Int, pos: Int)

/**
 * Coverage information about the Scala statement.
 *
 * @param start Starting position of the statement.
 * @param end Ending position of the statement.
 * @param hitCount How many times has the statement been hit by unit tests. Zero means
 *                 that the statement is not covered.
 */
case class CoveredStatement(start: StatementPosition, end: StatementPosition, hitCount: Int)

/**
 * Aggregated statement coverage for a given source code line.
 */
case class CoveredLine(line: Int, hitCount: Int)

object StatementCoverage {
  /**
   * Utility method to transform statement coverage to line coverage. Pessimistic logic is used
   * meaning that line hit count is minimum of hit counts of all statements on the given line.
   *
   * Example: If a line contains two statements, one is covered by 3 hits, the other one is
   * without any hits, then the whole line is treated as uncovered.
   *
   * @param statements Statement coverage.
   * @return Line coverage.
   */
  def statementCoverageToLineCoverage(statements: Iterable[CoveredStatement]): Iterable[CoveredLine] = {
    // Handle statements that end on a different line than start
    val multilineStatements = statements.filter { s => s.start.line != s.end.line }
    val extraStatements = multilineStatements.flatMap { s =>
      for (i <- (s.start.line + 1) to s.end.line)
        yield CoveredStatement(StatementPosition(i, 0), StatementPosition(i, 0), s.hitCount)
    }

    // Group statements by starting line
    val lineStatements = (statements ++ extraStatements).groupBy(_.start.line)

    // Pessimistic approach: line hit count is a minimum of hit counts of all statements on the line
    lineStatements.map { lineStatement =>
      CoveredLine(lineStatement._1, lineStatement._2.map(_.hitCount).min)
    }
  }
}