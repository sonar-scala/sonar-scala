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
trait ScoverageReportParser extends ScoverageReportParserAPI {

  /** Parses the scoverage report from a file and returns the [[ModuleCoverage]] */
  override def parse(scoverageReportFilename: String): ModuleCoverage = {
    val scoverageXMLReport = XML.loadFile(scoverageReportFilename)

    val moduleScoverage = extractScoverageFromNode(scoverageXMLReport)

    val classCoverages = for {
      classNode <- scoverageXMLReport \\ "class"
      filename = s"src/main/scala/${classNode \@ "filename"}"
      classScoverage = extractScoverageFromNode(classNode)
      lines = for {
        statement <- classNode \\ "statement"
        if (!(statement \@ "ignored").toBoolean)
        linenum = (statement \@ "line").toInt
        count = (statement \@ "invocation-count").toInt
      } yield (linenum -> count)
      linesCoverage = lines.groupBy(_._1).mapValues(_.map(_._2).sum)
      classCoverage = FileCoverage(classScoverage, linesCoverage)
    } yield (filename -> classCoverage)

    //merge the class coverages by filename
    val files = classCoverages.groupBy(_._1).mapValues(_.map(_._2).reduce(_ + _))

    ModuleCoverage(moduleScoverage, files)
  }

  /** Extracts the scoverage metrics form a class or module node */
  private def extractScoverageFromNode(node: Node): Scoverage =
    Scoverage(
      totalStatements = (node \@ "statement-count").toInt,
      coveredStatements = (node \@ "statements-invoked").toInt,
      statementCoverage = (node \@ "statement-rate").toDouble,
      branchCoverage = (node \@ "branch-rate").toDouble
    )
}
