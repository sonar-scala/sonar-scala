package com.buransky.plugins.scoverage


object ScoverageParser {
  def parse(scoverageXmlPath: String): ParentStatementCoverage = {
    val errorCodeFile = FileStatementCoverage("ErrorCode.scala", 17, 13)
    val graphFile = FileStatementCoverage("Graph.scala", 42, 0)

    val file2 = FileStatementCoverage("file2.scala", 2, 1)
    val bbbDir = ParentStatementCoverage("bbb", Seq(file2))

    val file1 = FileStatementCoverage("file1.scala", 100, 33)
    val aaaDir = ParentStatementCoverage("aaa", Seq(file1, errorCodeFile, graphFile, bbbDir))

    val project = ParentStatementCoverage("project", Seq(aaaDir))

    project
  }
}

trait StatementCoverage {
  lazy val rate: Double = (coveredStatementsCount.toDouble / statementsCount.toDouble) * 100.0

  val name: String
  val statementsCount: Int
  val coveredStatementsCount: Int

  require(statementsCount >= 0, "Statements count cannot be negative! [" + statementsCount + "]")
  require(coveredStatementsCount >= 0, "Statements count cannot be negative! [" +
    coveredStatementsCount + "]")
  require(coveredStatementsCount <= statementsCount,
    "Number of covered statements cannot be more than total number of statements! [" +
    statementsCount + ", " + coveredStatementsCount + "]")
}

case class ParentStatementCoverage(name: String, children: Iterable[StatementCoverage])
  extends StatementCoverage {
  val statementsCount = children.map(_.statementsCount).sum
  val coveredStatementsCount = children.map(_.coveredStatementsCount).sum
}

case class FileStatementCoverage(name: String, statementsCount: Int,
  coveredStatementsCount: Int) extends StatementCoverage