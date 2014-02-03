/*
 * Sonar Scala Plugin
 * Copyright (C) 2011 - 2013 All contributors
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
package org.sonar.plugins.scala.language

import org.junit.runner.RunWith
import org.scalatest.FlatSpec
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class CodeDetectorSpec extends FlatSpec with ShouldMatchers {

  "A code detector" should "detect a simple variable declaration" in {
    CodeDetector.hasDetectedCode("val a = 1") should be (true)
  }

  it should "detect a simple function call" in {
    CodeDetector.hasDetectedCode("list.map(_ + \"Hello World\")") should be (true)
  }

  it should "detect a simple value assignment" in {
    CodeDetector.hasDetectedCode("a = 1 + 2") should be (true)
  }

  it should "detect a simple package declaration" in {
    CodeDetector.hasDetectedCode("package hello.world") should be (true)
  }

  it should "not detect any code in a normal text" in {
    CodeDetector.hasDetectedCode("this is just a normal text") should be (false)
  }

  it should "not detect any code in a normal comment text" in {
    CodeDetector.hasDetectedCode("// this is a normal comment") should be (false)
  }

  it should "detect a while loop" in {
    CodeDetector.hasDetectedCode("while (i == 2) { println(i); }") should be (true)
  }

  it should "detect a for loop" in {
    CodeDetector.hasDetectedCode("for (i <- 1 to 10) { println(i); }") should be (true)
  }
}