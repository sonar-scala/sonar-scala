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
package com.buransky.plugins.scoverage.util

import org.scalatest.{FlatSpec, Matchers}
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class PathUtilSpec extends FlatSpec with Matchers {
  
  val osName = System.getProperty("os.name")
  val separator = System.getProperty("file.separator")
  
  behavior of s"splitPath for $osName"
  
  it should "ignore the empty path" in {
    PathUtil.splitPath("") should equal(List.empty[String])
  }

  it should "ignore a separator at the beginning" in {
    PathUtil.splitPath(s"${separator}a") should equal(List("a"))
  }

  it should "work with separator in the middle" in {
    PathUtil.splitPath(s"a${separator}b") should equal(List("a", "b"))
  }
  
  it should "work with an OS dependent absolute path" in {
    if (osName.startsWith("Windows")) {
      PathUtil.splitPath("C:\\test\\2") should equal(List("test", "2"))
    } else {
      PathUtil.splitPath("/test/2") should equal(List("test", "2"))
    }
  }
}