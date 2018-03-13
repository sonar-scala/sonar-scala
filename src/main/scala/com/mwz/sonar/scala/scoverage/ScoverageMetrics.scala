package com.mwz.sonar.scala.scoverage

import org.sonar.api.measures.{CoreMetrics, Metric, Metrics}
import scala.collection.JavaConverters._

/**
 * Statement coverage metric definition.
 *
 * @author Rado Buransky
 * @author BalmungSan
 */
class ScoverageMetrics extends Metrics {
  override def getMetrics: java.util.List[Metric[_ <: java.io.Serializable]] =
    List[Metric[_ <: java.io.Serializable]](
      ScoverageMetrics.statementCoverage,
      ScoverageMetrics.coveredStatements,
      ScoverageMetrics.totalStatements
    ).asJava
}

object ScoverageMetrics {
  private val TOTAL_STATEMENTS_KEY = "total_statements"
  private val COVERED_STATEMENTS_KEY = "covered_statements"
  private val STATEMENT_COVERAGE_KEY = "scoverage"

  val totalStatements: Metric[java.lang.Integer] =
    new Metric.Builder(TOTAL_STATEMENTS_KEY, "Total statements", Metric.ValueType.INT)
      .setDescription("Number of all statements")
      .setDirection(Metric.DIRECTION_BETTER)
      .setQualitative(false)
      .setDomain(CoreMetrics.DOMAIN_SIZE)
      .create()

  val coveredStatements: Metric[java.lang.Integer] =
    new Metric.Builder(COVERED_STATEMENTS_KEY, "Covered statements", Metric.ValueType.INT)
      .setDescription("Number of statements covered by tests")
      .setDirection(Metric.DIRECTION_BETTER)
      .setQualitative(false)
      .setDomain(CoreMetrics.DOMAIN_SIZE)
      .create()

  val statementCoverage: Metric[java.lang.Double] =
    new Metric.Builder(STATEMENT_COVERAGE_KEY, "Statement coverage", Metric.ValueType.PERCENT)
      .setDescription("Percentage of statements covered by tests")
      .setDirection(Metric.DIRECTION_BETTER)
      .setQualitative(true)
      .setDomain(CoreMetrics.DOMAIN_COVERAGE)
      .setWorstValue(0.0)
      .setBestValue(100.0)
      .create()
}
