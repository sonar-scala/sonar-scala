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
class FunctionCounterSpec extends FlatSpec with ShouldMatchers {

  "A function counter" should "count a simple function declaration" in {
    FunctionCounter.countFunctions("def test = 42") should be (1)
  }

  it should "count a simple method declaration" in {
    FunctionCounter.countFunctions("def test { println(42) }") should be (1)
  }

  it should "not count a simple function declared as a function literal" in {
    FunctionCounter.countFunctions("(i: Int) => i + 1") should be (0)
  }

  it should "count a simple function declaration nested in another function" in {
    val source = """
      def test = {
        def inc(i: Int) = i + 1
      }"""
    FunctionCounter.countFunctions(source) should be (2)
  }

  it should "count a simple function declaration nested in another method" in {
    val source = """
      def test {
        def inc(i: Int) = i + 1
      }"""
    FunctionCounter.countFunctions(source) should be (2)
  }

  it should "not count an empty constructor as a function declaration" in {
    val source = "class Person(val name: String) { }"
    FunctionCounter.countFunctions(source) should be (0)
  }

  it should "count a constructor as a function declaration" in {
    val source = """
      class Person(val name: String) {
    		def this(name: String) {
    		  super(name)
    		  println(name)
    		}
    	}"""
    FunctionCounter.countFunctions(source) should be (1)
  }

  it should "count a simple function declaration nested in an object" in {
    val source = """
      object Test {
        def inc(i: Int) = { i + 1 }
      }"""
    FunctionCounter.countFunctions(source) should be (1)
  }

  it should "count a simple function declaration nested in a trait" in {
    val source = """
      trait Test {
        def inc(i: Int) = { i + 1 }
      }"""
    FunctionCounter.countFunctions(source) should be (1)
  }

  it should "count a function declaration with two parameter lists" in {
    val source = "def sum(x: Int)(y: Int) = { x + y }"
    FunctionCounter.countFunctions(source) should be (1)
  }

  it should "count a simple function declaration nested in a trait with self-type annotation" in {
    val source = """
      trait Test {
        self: HelloWorld =>
        def inc(i: Int) = { i + 1 }
      }"""
    FunctionCounter.countFunctions(source) should be (1)
  }

  it should "count a function declaration with two parameter lists nested in a trait with self-type annotation" in {
    val source = """
      trait Test {
        self: HelloWorld =>
        def sum(x: Int)(y: Int) = { x + y }
      }"""
    FunctionCounter.countFunctions(source) should be (1)
  }

  it should "count a function declaration with if else block in its body" in {
    val source = """
      def test(number: Int) : Int =
        if (number < 42)
          23
        else
          42"""
    FunctionCounter.countFunctions(source) should be (1)
  }
}