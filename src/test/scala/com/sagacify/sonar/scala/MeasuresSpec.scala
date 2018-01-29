package com.sagacify.sonar.scala

import org.scalatest._

class MeasurersSpec extends FlatSpec with Matchers {

    val exampleSourceFile = """/*
 * Sonar Scala Plugin
 * Copyright (C) 2011-2016 SonarSource SA
 * mailto:contact AT sonarsource DOT com
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
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package com.sagacify.example

import collection.mutable.Stack
import org.scalatest._

class ScalaSensorSpec extends FlatSpec with Matchers {

  // Example test
  "A Stack" should "pop values in last-in-first-out order" in {
    val stack = new Stack[Int]
    stack.push(1) // This is
    stack.push(2) // a pointless
    stack.pop() should be (2) // example
    stack.pop() should be (1)
  }

  it should "throw NoSuchElementException if an empty stack is popped" in {
    val emptyStack = new Stack[Int]
    a [NoSuchElementException] should be thrownBy {
      emptyStack.pop()
    }
  }
}
"""

  "A Comment lines counter" should "count line comments" in {
    val tokens = Scala.tokenize("// this is a test", "2.11.8")
    val count = Measures.count_comment_lines(tokens)
    assert(count == 1)
  }

  it should "count multiline comments" in {
    val tokens = Scala.tokenize("/* this\n *is\n *a\n *test*/", "2.11.8")
    val count = Measures.count_comment_lines(tokens)
    assert(count == 4)
  }

  it should "count trailing comments." in {
    val tokens = Scala.tokenize("case class Test() // this is a test", "2.11.8")
    val count = Measures.count_comment_lines(tokens)
    assert(count == 1)
  }

  it should "count the correct number of comments" in {
    val tokens = Scala.tokenize(exampleSourceFile, "2.11.8")
    val count = Measures.count_comment_lines(tokens)
    assert(count == 23)
  }

  "A Non-Comment lines counter" should "count non-comment lines of codes" in {
    val tokens = Scala.tokenize("package com.example", "2.11.8")
    println(tokens)
    val count = Measures.count_ncloc(tokens)
    assert(count == 1)
  }

  it should "count lines of code with a trailing comment" in {
    val tokens = Scala.tokenize("case class Test() /*\n * test\n */", "2.11.8")
    val count = Measures.count_ncloc(tokens)
    assert(count == 1)
  }

  it should "count trailing code." in {
    val tokens = Scala.tokenize("/* this is a test */ case class Test()", "2.11.8")
    val count = Measures.count_ncloc(tokens)
    assert(count == 1)
  }

  it should "count the correct number of comments" in {
    val tokens = Scala.tokenize(exampleSourceFile, "2.11.8")
    val count = Measures.count_ncloc(tokens)
    assert(count == 18)
  }

}
