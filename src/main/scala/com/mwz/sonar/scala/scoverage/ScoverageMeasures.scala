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

import ScoverageMeasures._
import cats.data.Chain
import cats.data.Chain._
import cats.instances.option._
import cats.instances.string._
import cats.instances.tuple._
import cats.syntax.bitraverse._
import cats.syntax.eq._
import cats.syntax.foldable._
import org.sonar.api.ce.measure.Component
import org.sonar.api.ce.measure.MeasureComputer
import org.sonar.api.ce.measure.MeasureComputer.MeasureComputerContext
import org.sonar.api.ce.measure.MeasureComputer.{MeasureComputerDefinition, MeasureComputerDefinitionContext}

final class ScoverageMeasures extends MeasureComputer {
  override def define(context: MeasureComputerDefinitionContext): MeasureComputerDefinition =
    context
      .newDefinitionBuilder()
      .setInputMetrics(sumMetrics.toList: _*)
      .setOutputMetrics((sumMetrics ++ percentageMetrics.map(_._1)).toList: _*)
      .build()

  override def compute(context: MeasureComputerContext): Unit = {
    if (context.getComponent.getType.name =!= Component.Type.FILE.name) {
      val summed: Map[String, Int] =
        sumMetrics
          .map { metric =>
            (
              metric,
              Chain
                .fromSeq(
                  context
                    .getChildrenMeasures(metric)
                    .asScala
                    .toSeq
                )
                .reduceLeftToOption(_.getIntValue)((acc, measure) => acc + measure.getIntValue)
            )
          }
          .collect { case (key, Some(sum)) => (key, sum) }
          .iterator
          .toMap
      summed.foreach { case (key, sum) => context.addMeasure(key, sum) }

      val percentages: Map[String, BigDecimal] =
        percentageMetrics
          .map {
            case (key, (total, hits)) =>
              (
                key,
                (summed.get(total), summed.get(hits)).bisequence
                  .map {
                    case (total, hits) =>
                      if (total > 0) BigDecimal.valueOf(hits.toLong) / total * 100
                      else BigDecimal(0)
                  }
              )
          }
          .collect { case (key, Some(sum)) => (key, sum) }
          .iterator
          .toMap
      percentages.foreach { case (key, percentage) => context.addMeasure(key, round(percentage)) }
    }
  }
}

object ScoverageMeasures {
  // individual metric -> total metric
  val sumMetrics: Chain[String] = Chain(
    ScoverageMetrics.statements.key,
    ScoverageMetrics.coveredStatements.key,
    ScoverageMetrics.branches.key,
    ScoverageMetrics.coveredBranches.key
  )
  // metric -> (total, hits)
  val percentageMetrics: Chain[(String, (String, String))] =
    Chain(
      (ScoverageMetrics.statementCoverage.key -> (
        (
          ScoverageMetrics.statements.key,
          ScoverageMetrics.coveredStatements.key
        )
      )),
      (ScoverageMetrics.branchCoverage.key -> (
        (
          ScoverageMetrics.branches.key,
          ScoverageMetrics.coveredBranches.key
        )
      ))
    )

  private[scoverage] def round(x: BigDecimal): Double =
    x.setScale(scale = 2, mode = BigDecimal.RoundingMode.HALF_EVEN).toDouble
}
