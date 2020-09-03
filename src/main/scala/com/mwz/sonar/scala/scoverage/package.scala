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

import scala.util.Try
import cats.kernel.Semigroup
import cats.syntax.semigroup.catsSyntaxSemigroup

package object scoverage {

  /**
   *  The coverage information of the lines of a file.
   *  Line num -> Line hits
   */
  private[scoverage] type LinesCoverage = Map[Int, Int]

  /** Merges two scoverage metrics into one. */
  private[scoverage] final implicit val ScoverageSemigroup: Semigroup[Scoverage] =
    new Semigroup[Scoverage] {
      private[this] def toFixedPrecision(value: BigDecimal): Double =
        value.setScale(scale = 2, mode = BigDecimal.RoundingMode.HALF_EVEN).toDouble

      private[this] def percentage(hits: Double, total: Double): Double =
        Try(toFixedPrecision(BigDecimal.valueOf(hits) / total * 100)).getOrElse(0)

      override def combine(a: Scoverage, b: Scoverage): Scoverage = {
        val mergedStatements = a.statements + b.statements
        val mergedCoveredStatements = a.coveredStatements + b.coveredStatements
        val mergedBranches = a.branches + b.branches
        val mergedCoveredBranches = a.coveredBranches + b.coveredBranches
        Scoverage(
          statements = mergedStatements,
          coveredStatements = mergedCoveredStatements,
          statementCoverage = percentage(mergedCoveredStatements.toDouble, mergedStatements.toDouble),
          branches = mergedBranches,
          coveredBranches = mergedCoveredBranches,
          branchCoverage = percentage(mergedCoveredBranches.toDouble, mergedBranches.toDouble)
        )
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
