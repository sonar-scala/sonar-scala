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

import scala.xml.{Elem, NodeSeq, XML}

/**
 *  Scoverage XML reports parser.
 *
 *  @author BalmungSan
 */
object ScoverageReportParser {
  //this method allows us to test the parser without reading an xml file from disk
  /** Parses the scoverage report from an xlm Element and returns the [[ModuleCoverage]] */
  def parse(report: Elem): ModuleCoverage =
    ???

  /** Parses the scoverage report from a file and returns the [[ModuleCoverage]] */
  def parse(reportFilename: String): ModuleCoverage =
    parse(XML.loadFile(reportFilename))
}
