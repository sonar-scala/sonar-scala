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
package com.mwz.sonar.scala.scoverage

import scala.xml.{Node, NodeSeq, XML}

/**
 *  Scoverage XML reports parser.
 *
 *  @author BalmungSan
 */
object ScoverageReportParser {

  /** Parses the scoverage report from a file and returns the [[ModuleCoverage]] */
  def parse(scoverageReportFilename: String): ModuleCoverage = {
    val scoverageXMLReport = XML.loadFile(scoverageReportFilename)

    val moduleScoverage = extractScoverageFromNode(scoverageXMLReport)

    val classCoverages = for {
      classNode <- scoverageXMLReport \\ "class"
      filename = (classNode \ "@filename").text
      classScoverage = extractScoverageFromNode(classNode)
      lines = Seq.empty[(Int, Int)]
      classCoverage = FileCoverage(classScoverage, lines)
    } yield (filename -> classCoverage)

    val files = classCoverages.toMap

    ModuleCoverage(moduleScoverage, files)
  }

  /** Extracts the scoverage metrics form a class or module node */
  private def extractScoverageFromNode(node: Node): Scoverage =
    Scoverage(
      totalStatements = (node \ "@statement-count").text.toInt,
      coveredStatements = (node \ "@statements-invoked").text.toInt,
      statementCoverage = (node \ "@statement-rate").text.toDouble,
      branchCoverage = (node \ "@branch-rate").text.toDouble
    )
}
