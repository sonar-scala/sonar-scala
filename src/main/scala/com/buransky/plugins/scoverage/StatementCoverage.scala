package com.buransky.plugins.scoverage

/**
 * Statement coverage represents rate at which are statements of a certain source code unit
 * being covered by tests.
 */
sealed trait StatementCoverage {
  /**
   * Percentage rate ranging from 0 up to 100%.
   */
  lazy val rate: Double = (coveredStatementsCount.toDouble / statementCount.toDouble) * 100.0

  /**
   * Total number of all statements within the source code unit,
   */
  val statementCount: Int

  /**
   * Number of statements covered by unit tests.
   */
  val coveredStatementsCount: Int

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
  val children: Iterable[StatementCoverage]
  val statementCount = children.map(_.statementCount).sum
  val coveredStatementsCount = children.map(_.coveredStatementsCount).sum
}

/**
 * Root node. In multi-module projects it can contain other ProjectStatementCoverage
 * elements as children.
 *
 * @param name Name of the project or module.
 * @param children
 */
case class ProjectStatementCoverage(name: String, children: Iterable[StatementCoverage])
  extends NodeStatementCoverage

case class DirectoryStatementCoverage(name: String, children: Iterable[StatementCoverage])
  extends NodeStatementCoverage

case class FileStatementCoverage(name: String, statementCount: Int, coveredStatementsCount: Int,
                                 statements: Iterable[CoveredStatement]) extends StatementCoverage

case class StatementPosition(line: Int, pos: Int)

case class CoveredStatement(start: StatementPosition, end: StatementPosition, hitCount: Int)

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