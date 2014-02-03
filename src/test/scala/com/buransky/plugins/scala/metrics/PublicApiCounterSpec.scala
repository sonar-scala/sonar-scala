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
package org.sonar.plugins.scala.metrics

import org.junit.runner.RunWith
import org.scalatest.FlatSpec
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class PublicApiCounterSpec extends FlatSpec with ShouldMatchers {

  "A public api counter" should "count a simple function declaration" in {
    PublicApiCounter.countPublicApi("def test = 42") should be (1)
  }

  it should "count a simple method declaration" in {
    PublicApiCounter.countPublicApi("def test { println(42) }") should be (1)
  }

  it should "count a simple value declaration" in {
    PublicApiCounter.countPublicApi("val maybeImportantNumber = 42") should be (1)
  }

  it should "not count a private value declaration" in {
    PublicApiCounter.countPublicApi("private val maybeImportantNumber = 42") should be (0)
  }

  it should "not count a private function declaration" in {
    PublicApiCounter.countPublicApi("private def test = 42") should be (0)
  }

  it should "not count a private method declaration" in {
    PublicApiCounter.countPublicApi("private def test { println(42) }") should be (0)
  }

  it should "count a class declaration" in {
    PublicApiCounter.countPublicApi("class A {}") should be (1)
  }

  it should "count an object declaration" in {
    PublicApiCounter.countPublicApi("object A {}") should be (1)
  }

  it should "count a trait declaration" in {
    PublicApiCounter.countPublicApi("trait A {}") should be (1)
  }

  it should "not count a private class declaration" in {
    PublicApiCounter.countPublicApi("private class A {}") should be (0)
  }

  it should "not count a private object declaration" in {
    PublicApiCounter.countPublicApi("private object A {}") should be (0)
  }

  it should "not count a private trait declaration" in {
    PublicApiCounter.countPublicApi("private trait A {}") should be (0)
  }

  it should "count an undocumented class declaration" in {
    PublicApiCounter.countUndocumentedPublicApi("class A {}") should be (1)
  }

  it should "not count a documented class declaration as undocumented one" in {
    val source = """/**
       * This is a comment of a public api member.
       */
      class A {}"""
    PublicApiCounter.countUndocumentedPublicApi(source) should be (0)
  }

  it should "count an undocumented class declaration with package declaration before" in {
    val source = """package a.b.c

      class A {}"""
    PublicApiCounter.countUndocumentedPublicApi(source) should be (1)
  }

  it should "not count a documented class declaration with package declaration before as undocumented one" in {
    val source = """package a.b.c

      /**
       * This is a comment of a public api member.
       */
      class A {}"""
    PublicApiCounter.countUndocumentedPublicApi(source) should be (0)
  }

  it should "count all public api members of class and its undocumented ones" in {
    val source = """package a.b.c

      /**
       * This is a comment of a public api member.
       */
      class A {

        /**
         * Well, don't panic. ;-)
         */
        val meaningOfLife = 42

        val b = "test"

        def helloWorld { printString("Hello World!") }

        private def printString(str: String) { println(str) }
      }"""

    PublicApiCounter.countPublicApi(source) should be (4)
    PublicApiCounter.countUndocumentedPublicApi(source) should be (2)
  }

  it should "not count nested function and method declarations" in {
    val source ="""def test = {
        def a = 12 + 1
        def b = 13 + 1

        a + b + 42
      }"""
    PublicApiCounter.countPublicApi(source) should be (1)
  }

  it should "not count nested value declarations" in {
    val source ="""val test = {
        def a = 12 + 1
        def b = 13 + 1

        a + b + 42
      }"""
    PublicApiCounter.countPublicApi(source) should be (1)
  }
}