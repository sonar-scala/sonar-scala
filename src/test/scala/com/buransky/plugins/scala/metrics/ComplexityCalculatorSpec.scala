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
class ComplexityCalculatorSpec extends FlatSpec with ShouldMatchers {

  "A complexity calculator" should "calculate complexity of if expression" in {
    ComplexityCalculator.measureComplexity("if (2 == 3) println(123)") should be (1)
  }

  it should "calculate complexity of for loop" in {
    ComplexityCalculator.measureComplexity("for (i <- 1 to 10) println(i)") should be (1)
  }

  it should "calculate complexity of while loop" in {
    val source = """var i = 0
      while (i < 10) {
        println(i)
        i += 1
      }"""
    ComplexityCalculator.measureComplexity(source) should be (1)
  }

  it should "calculate complexity of do loop" in {
    val source = """var i = 0
      do {
        println(i)
        i += 1
      } while  (i < 10)"""
    ComplexityCalculator.measureComplexity(source) should be (1)
  }

  it should "calculate complexity of throw expression" in {
    ComplexityCalculator.measureComplexity("throw new RuntimeException()") should be (1)
  }

  it should "calculate complexity of while loop with an if condition and throw expression" in {
    val source = """var i = 0
      while (i < 10) {
        println(i)
        i += 1
        if (i == 9)
          throw new RuntimeException()
      }"""
    ComplexityCalculator.measureComplexity(source) should be (3)
  }

  it should "calculate complexity of function definition" in {
    ComplexityCalculator.measureComplexity("def inc(i: Int) = i + 1") should be (1)
  }

  it should "calculate complexity of function definition with an if condition" in {
    val source = """def inc(i: Int) = {
        if (i == 0) {
          i + 2
        } else {
          i + 1
        }
      }"""
    ComplexityCalculator.measureComplexity(source) should be (2)
  }

  it should "calculate complexity of function definition and its whole body" in {
    val source = """def inc(i: Int) = {
        if (i == 0) {
          i + 2
        } else {
          while (i < 10) {
            if (i == 9)
              throw new RuntimeException()
            i + 1
          }
        }
      }"""
    ComplexityCalculator.measureComplexity(source) should be (5)
  }

  it should "calculate complexity distribution of one function" in {
    val source = """def inc(i: Int) = {
        if (i == 0) {
          i + 2
        } else {
          i + 1
        }
      }"""

    ComplexityCalculator.measureComplexityOfFunctions(source).getMeasure.getData should include ("2=1")
  }

  it should "calculate complexity distribution of two functions" in {
    val source = """def inc(i: Int) = {
        if (i == 0) {
          i + 2
        } else {
          i + 1
        }
      }

      def dec(i: Int) = i - 1"""

    ComplexityCalculator.measureComplexityOfFunctions(source).getMeasure.getData should include ("1=1")
    ComplexityCalculator.measureComplexityOfFunctions(source).getMeasure.getData should include ("2=1")
  }

  it should "calculate complexity distribution of all functions" in {
    val source = """def inc(i: Int) = {
        if (i == 0) {
          i + 2
        } else {
          i + 1
        }
      }

      def dec(i: Int) = i - 1
      def dec2(i: Int) = i - 2
      def dec3(i: Int) = i - 3"""

    ComplexityCalculator.measureComplexityOfFunctions(source).getMeasure.getData should include ("1=3")
    ComplexityCalculator.measureComplexityOfFunctions(source).getMeasure.getData should include ("2=1")
  }

  it should "calculate complexity distribution of all functions nested in a class" in {
    val source = """class A {
        def inc(i: Int) = {
          if (i == 0) {
            i + 2
          } else {
            i + 1
          }
        }

        def dec(i: Int) = i - 1
        def dec2(i: Int) = i - 2
        def dec3(i: Int) = i - 3
      }"""

    ComplexityCalculator.measureComplexityOfFunctions(source).getMeasure.getData should include ("1=3")
    ComplexityCalculator.measureComplexityOfFunctions(source).getMeasure.getData should include ("2=1")
  }

  it should "calculate complexity distribution of one class" in {
    val source = """class A {
        def inc(i: Int) = {
          if (i == 0) {
            i + 2
          } else {
            i + 1
          }
        }
      }"""

    ComplexityCalculator.measureComplexityOfClasses(source).getMeasure.getData should include ("0=1")
  }

  it should "calculate complexity distribution of two classes" in {
    val source = """package abc
      class A {
        def inc(i: Int) = {
          if (i == 0) {
            i + 2
          } else {
            i + 1
          }
        }

        def dec(i: Int) = i - 1
      }

      class B {
        def inc(i: Int) = {
          if (i == 0) {
            i + 2
          } else {
            i + 1
          }
        }

        def dec(i: Int) = i - 1
        def dec2(i: Int) = i - 2
        def dec3(i: Int) = i - 3
      }"""

    ComplexityCalculator.measureComplexityOfClasses(source).getMeasure.getData should include ("0=1")
    ComplexityCalculator.measureComplexityOfClasses(source).getMeasure.getData should include ("5=1")
  }
}