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
      ScoverageMetrics.totalStatements,
      ScoverageMetrics.coveredStatements,
      ScoverageMetrics.statementCoverage,
      ScoverageMetrics.branchCoverage
    ).asJava
}

object ScoverageMetrics {

  /** Builds a new [[Metric]] */
  private def BuildMetric[T <: java.io.Serializable](
    metricKey: String,
    metricName: String,
    metricType: Metric.ValueType,
    metricDescription: String,
    metricDirection: java.lang.Integer,
    metricDomain: String,
    isMetricQualitative: Boolean           = false,
    metricValues: Option[(Double, Double)] = None
  ): Metric[T] = {
    val metricBuilder = new Metric.Builder(metricKey, metricName, metricType)
      .setDescription(metricDescription)
      .setDirection(metricDirection)
      .setDomain(metricDomain)
      .setQualitative(isMetricQualitative)

    metricValues match {
      case Some((worstValue, bestValue)) =>
        metricBuilder
          .setWorstValue(worstValue)
          .setBestValue(bestValue)
          .create[T]()
      case None => metricBuilder.create[T]()
    }
  }

  val totalStatements: Metric[java.lang.Integer] =
    BuildMetric(
      metricKey = "total_statements",
      metricName = "Total statements",
      metricType = Metric.ValueType.INT,
      metricDescription = "Number of all statements",
      metricDirection = Metric.DIRECTION_BETTER,
      metricDomain = CoreMetrics.DOMAIN_SIZE,
    )

  val coveredStatements: Metric[java.lang.Integer] =
    BuildMetric(
      metricKey = "covered_statements",
      metricName = "Covered statements",
      metricType = Metric.ValueType.INT,
      metricDescription = "Number of statements covered by tests",
      metricDirection = Metric.DIRECTION_BETTER,
      metricDomain = CoreMetrics.DOMAIN_SIZE,
    )

  val statementCoverage: Metric[java.lang.Double] =
    BuildMetric(
      metricKey = "stament_coverage",
      metricName = "Statement coverage",
      metricType = Metric.ValueType.PERCENT,
      metricDescription = "Percentage of statements covered by tests",
      metricDirection = Metric.DIRECTION_BETTER,
      metricDomain = CoreMetrics.DOMAIN_COVERAGE,
      isMetricQualitative = true,
      metricValues = Some((0.0d, 100.0d))
    )

  val branchCoverage: Metric[java.lang.Double] =
    BuildMetric(
      metricKey = "branch_coverage",
      metricName = "Branch coverage",
      metricType = Metric.ValueType.PERCENT,
      metricDescription = "Percentage of branches covered by tests",
      metricDirection = Metric.DIRECTION_BETTER,
      metricDomain = CoreMetrics.DOMAIN_COVERAGE,
      isMetricQualitative = true,
      metricValues = Some((0.0d, 100.0d))
    )
}