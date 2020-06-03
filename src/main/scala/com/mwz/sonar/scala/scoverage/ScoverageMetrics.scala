/*
 * Copyright (C) 2018-2020  All sonar-scala contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU General Lesser Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.mwz.sonar.scala
package scoverage

import scala.jdk.CollectionConverters._

import org.sonar.api.measures.{CoreMetrics, Metric, Metrics}

/** Statement coverage metric definition. */
final class ScoverageMetrics extends Metrics {
  override def getMetrics: java.util.List[Metric[_ <: java.io.Serializable]] =
    List[Metric[_ <: java.io.Serializable]](
      ScoverageMetrics.statements,
      ScoverageMetrics.coveredStatements,
      ScoverageMetrics.statementCoverage,
      ScoverageMetrics.branches,
      ScoverageMetrics.coveredBranches,
      ScoverageMetrics.branchCoverage
    ).asJava
}

object ScoverageMetrics {

  /** Builds a new [[Metric]] */
  private def buildMetric[T <: java.io.Serializable](
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

  val statements: Metric[java.lang.Integer] =
    buildMetric(
      metricKey = "sonar-scala-scoverage-total-statements",
      metricName = "Total statements",
      metricType = Metric.ValueType.INT,
      metricDescription = "Number of all statements",
      metricDirection = Metric.DIRECTION_NONE,
      metricDomain = CoreMetrics.DOMAIN_SIZE
    )

  val coveredStatements: Metric[java.lang.Integer] =
    buildMetric(
      metricKey = "sonar-scala-scoverage-covered-statements",
      metricName = "Covered statements",
      metricType = Metric.ValueType.INT,
      metricDescription = "Number of all statements covered by tests",
      metricDirection = Metric.DIRECTION_BETTER,
      metricDomain = CoreMetrics.DOMAIN_COVERAGE
    )

  val statementCoverage: Metric[java.lang.Double] =
    buildMetric(
      metricKey = "sonar-scala-scoverage-statement-coverage",
      metricName = "Statement coverage",
      metricType = Metric.ValueType.PERCENT,
      metricDescription = "Percentage of all statements covered by tests",
      metricDirection = Metric.DIRECTION_BETTER,
      metricDomain = CoreMetrics.DOMAIN_COVERAGE,
      isMetricQualitative = true,
      metricValues = Some((0.0d, 100.0d))
    )

  val branches: Metric[java.lang.Integer] =
    buildMetric(
      metricKey = "sonar-scala-scoverage-total-branches",
      metricName = "Total branches",
      metricType = Metric.ValueType.INT,
      metricDescription = "Number of all branches",
      metricDirection = Metric.DIRECTION_NONE,
      metricDomain = CoreMetrics.DOMAIN_SIZE
    )

  val coveredBranches: Metric[java.lang.Integer] =
    buildMetric(
      metricKey = "sonar-scala-scoverage-covered-branches",
      metricName = "Covered branches",
      metricType = Metric.ValueType.INT,
      metricDescription = "Number of all branches covered by tests",
      metricDirection = Metric.DIRECTION_BETTER,
      metricDomain = CoreMetrics.DOMAIN_COVERAGE
    )

  val branchCoverage: Metric[java.lang.Double] =
    buildMetric(
      metricKey = "sonar-scala-scoverage-branch-coverage",
      metricName = "Branch coverage",
      metricType = Metric.ValueType.PERCENT,
      metricDescription = "Percentage of all branches covered by tests",
      metricDirection = Metric.DIRECTION_BETTER,
      metricDomain = CoreMetrics.DOMAIN_COVERAGE,
      isMetricQualitative = true,
      metricValues = Some((0.0d, 100.0d))
    )
}
