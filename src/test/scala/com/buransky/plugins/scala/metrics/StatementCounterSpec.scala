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
class StatementCounterSpec extends FlatSpec with ShouldMatchers {

  "A statement counter" should "count a simple assignment as a statement" in {
    StatementCounter.countStatements("a = 1") should be (1)
  }

  it should "count a simple method call as a statement" in {
    StatementCounter.countStatements("println(123)") should be (1)
  }

  it should "not count a simple variable declaration as a statement" in {
    StatementCounter.countStatements("var a") should be (0)
  }

  it should "count a simple variable declaration with assignment as a statement" in {
    StatementCounter.countStatements("var a = 2") should be (1)
  }

  it should "count a while loop as a statement" in {
    StatementCounter.countStatements("while (1 == 1) {}") should be (1)
  }

  it should "count a for loop as a statement" in {
    StatementCounter.countStatements("for (i <- 1 to 10) {}") should be (1)
  }

  it should "count a while loop as a statement and all statements in loop body" in {
    val source = """
      while (1 == 1) {
    		val a = inc(2)
      }"""
    StatementCounter.countStatements(source) should be (2)
  }

  it should "count a for loop as a statement and all statements in loop body" in {
    val source = """
      for (i <- 1 to 10) {
        val a = inc(2)
      }"""
    StatementCounter.countStatements(source) should be (2)
  }

  it should "count if as a statement" in {
    val source = """
      if (1 == 1)
        println()"""
    StatementCounter.countStatements(source) should be (2)
  }

  it should "count an if block as a statement and all statements in its body" in {
    val source = """
      if (1 + 2 < 4) {
        val a = inc(2)
        println(3)
        def test = { 1 + 2 }
      }"""
    StatementCounter.countStatements(source) should be (4)
  }

  it should "count a simple if else block as a statement" in {
    val source = """
      if (1+2 < 4)
        println("Hello World")
      else
        println("123")"""
    StatementCounter.countStatements(source) should be (4)
  }

  it should "count an if else block as a statement and all statements in its body" in {
    val source = """
      if (1 + 2 < 4) {
        val a = inc(2)
        println("Hello World")
        def test = 1 + 2
      } else {
        def test2 = 1
        val b = test2
      }"""
    StatementCounter.countStatements(source) should be (7)
  }

  it should "count all statements in body of a function definition" in {
    val source = """
      def test(i: Int) = {
        val a = i + 42
        println(a)
        println(i + 42)
        a
      }"""
    StatementCounter.countStatements(source) should be (4)
  }

  it should "count all statements in body of a value definition" in {
    val source = """
      val test = {
        val a = i + 42
        println(a)
        println(i + 42)
        a
      }"""
    StatementCounter.countStatements(source) should be (4)
  }

  it should "count for comprehension with yield statement" in {
    val source = "for (x <- List(1, 2, 3, 4, 5) if (x % 2 != 0)) yield x"
    StatementCounter.countStatements(source) should be (2)
  }

  it should "count for comprehension with more complex yield statement" in {
    val source = "for (x <- List(1, 2, 3, 4, 5) if (x % 2 != 0)) yield x + inc(x)"
    StatementCounter.countStatements(source) should be (2)
  }

  it should "count for comprehension with yield statement where return value is only a literal" in {
    val source = "for (x <- List(1, 2, 3, 4, 5) if (x % 2 != 0)) yield 2"
    StatementCounter.countStatements(source) should be (2)
  }

  it should "count foreach function call on a list as a statement" in {
    val source = """
      myList.foreach {i =>
    		println(i)
        val a = i + 1
        println("inc: " + i)
    	}"""
    StatementCounter.countStatements(source) should be (4)
  }

  it should "count foreach function call and all statements in its body" in {
    val source = """
      def foo() = {
        List("Hello", "World", "!").foreach(word =>
          if (find(By(name, word)).isEmpty)
            create.name(word).save
        )
      }"""
    StatementCounter.countStatements(source) should be (3)
  }

  it should "count function call in a function definition nested in an object" in {
    val source = """
      object name extends MappedPoliteString(this, 100) {
        override def validations = valMinLen(1, S.?("attributeName")) _ :: Nil
      }"""
    StatementCounter.countStatements(source) should be (2)
  }
}