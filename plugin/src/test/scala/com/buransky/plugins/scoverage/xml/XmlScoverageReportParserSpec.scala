/*
 * Sonar Scoverage Plugin
 * Copyright (C) 2013 Rado Buransky
 * dev@sonar.codehaus.org
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package com.buransky.plugins.scoverage.xml

import org.scalatest.{FlatSpec, Matchers}
import org.scalatest.junit.JUnitRunner
import org.junit.runner.RunWith
import com.buransky.plugins.scoverage.ScoverageException

@RunWith(classOf[JUnitRunner])
class XmlScoverageReportParserSpec extends FlatSpec with Matchers {
  behavior of "parse file path"

  it must "fail for null path" in {
    the[IllegalArgumentException] thrownBy XmlScoverageReportParser().parse(null.asInstanceOf[String])
  }

  it must "fail for empty path" in {
    the[IllegalArgumentException] thrownBy XmlScoverageReportParser().parse("")
  }

  it must "fail for not existing path" in {
    the[ScoverageException] thrownBy XmlScoverageReportParser().parse("/x/a/b/c/1/2/3/4.xml")
  }
}
