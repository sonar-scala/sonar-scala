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

import cats.instances.int.catsKernelStdGroupForInt
import cats.instances.map.catsKernelStdMonoidForMap
import cats.kernel.Semigroup
import cats.syntax.semigroup.catsSyntaxSemigroup

package object scoverage {

  /**
   *  The coverage information of an entire project.
   *  It is composed of:
   *    - the overall [[ScoverageMetrics]] of the project.
   *    - the coverage information of each file of the project.
   */
  private[scoverage] final case class ProjectCoverage(
    projectScoverage: Scoverage,
    filesCoverage: Map[String, FileCoverage]
  )

  /**
   *  The coverage information of a file.
   *  It is composed of:
   *    - the overall [[ScoverageMetrics]] of the file.
   *    - the coverage information of each line of the file.
   */
  private[scoverage] final case class FileCoverage(
    fileScoverage: Scoverage,
    linesCoverage: LinesCoverage
  )

  /**
   *  The coverage information of the lines of a file.
   *  Linenum -> Line hits
   */
  private[scoverage] type LinesCoverage = Map[Int, Int]

  /** [[ScoverageMetrics]] of a component. */
  private[scoverage] final case class Scoverage(
    totalStatements: Int,
    coveredStatements: Int,
    statementCoverage: Double,
    branchCoverage: Double
  )

  /** Merges two scoverages metrics in one. */
  private[scoverage] final implicit val ScoverageSemigroup: Semigroup[Scoverage] = new Semigroup[Scoverage] {
    // Helper methods used to aggregate scoverage metrics.
    private[this] val Two = BigDecimal("2.0")
    private[this] val Percentage = BigDecimal("100.0")

    private[this] def toFixedPrecision(value: BigDecimal): Double =
      value.setScale(scale = 2, mode = BigDecimal.RoundingMode.HALF_EVEN).toDouble

    private[this] def averagePercentages(a: Double, b: Double): Double =
      toFixedPrecision((BigDecimal.valueOf(a) + BigDecimal.valueOf(b)) / Two)

    private[this] def computePercentage(hits: Double, total: Double): Double =
      toFixedPrecision(BigDecimal.valueOf(hits) / BigDecimal.valueOf(total) * Percentage)

    override def combine(a: Scoverage, b: Scoverage): Scoverage = {
      val mergedTotalStatements = a.totalStatements + b.totalStatements
      val mergedCoveredStatements = a.coveredStatements + b.coveredStatements
      val mergedStatementCoverage = computePercentage(mergedCoveredStatements, mergedTotalStatements)
      val mergedBranchCoverage = averagePercentages(a.branchCoverage, b.branchCoverage)
      Scoverage(mergedTotalStatements, mergedCoveredStatements, mergedStatementCoverage, mergedBranchCoverage)
    }
  }

  /** Merges two file coverages in one. */
  private[scoverage] final implicit val FileCoverageSemigroup: Semigroup[FileCoverage] =
    new Semigroup[FileCoverage] {
      override def combine(a: FileCoverage, b: FileCoverage): FileCoverage = {
        val mergedFileScoverage = a.fileScoverage |+| b.fileScoverage
        val mergedLinesCoverage = a.linesCoverage |+| b.linesCoverage
        FileCoverage(mergedFileScoverage, mergedLinesCoverage)
      }
    }
}
