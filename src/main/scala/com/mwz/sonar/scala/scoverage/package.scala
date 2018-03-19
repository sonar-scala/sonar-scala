/*
 * Sonar Scala Plugin
 * Copyright (C) 2018 All contributors
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
package com.mwz.sonar.scala

/**
 *  Coverage data structures
 *
 *  @author BalmungSan
 */
package object scoverage {

  /**
   *  The coverage information of an entire module.
   *  It is composed of:
   *    - the overall [[ScoverageMetrics]] of the module.
   *    - the coverage information of each file of the module.
   */
  private[scoverage] final case class ModuleCoverage(
    moduleScoverage: Scoverage,
    filesCoverage: Map[String, FileCoverage]
  )

  /**
   *  The coverage information of a file.
   *  It is composed of:
   *    - the overall [[ScoverageMetrics]] of the file.
   *    - the coverage information of each line of the module.
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

  /** [[ScoverageMetrics]] of a component */
  private[scoverage] final case class Scoverage(
    totalStatements: Int,
    coveredStatements: Int,
    statementCoverage: Double,
    branchCoverage: Double
  )
}
