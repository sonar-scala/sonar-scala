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
class TypeCounterSpec extends FlatSpec with ShouldMatchers {

  "A type counter" should "count type of a simple class declaration" in {
    TypeCounter.countTypes("class A {}") should be (1)
  }

  it should "count type of a simple object declaration" in {
    TypeCounter.countTypes("object A {}") should be (1)
  }

  it should "count type of a simple trait declaration" in {
    TypeCounter.countTypes("trait A {}") should be (1)
  }

  it should "count type of a simple case class declaration" in {
    TypeCounter.countTypes("case class A {}") should be (1)
  }

  it should "count type of a simple class declaration nested in a package" in {
    val source = """
      package a.b
      class A {}"""
    TypeCounter.countTypes(source) should be (1)
  }

  it should "count type of a simple class declaration nested in a package with imports" in {
    val source = """
      package a.b
      import java.util.List
      class A {}"""
    TypeCounter.countTypes(source) should be (1)
  }

  it should "count type of a simple class declaration nested in a package with import and doc comment" in {
    val source = """
      package a.b
      import java.util.List
      /** Doc comment... */
      class A {}"""
    TypeCounter.countTypes(source) should be (1)
  }

  it should "count type of a simple object declaration nested in a package" in {
    val source = """
      package a.b
      object A {}"""
    TypeCounter.countTypes(source) should be (1)
  }

  it should "count types of a simple class declarations" in {
    val source = """
      class A {}
      class B {}"""
    TypeCounter.countTypes(source) should be (2)
  }

  it should "count type of a simple class declaration nested in a class" in {
    TypeCounter.countTypes("class A { class B {} }") should be (2)
  }

  it should "count type of a simple class declaration nested in an object" in {
    TypeCounter.countTypes("object A { class B {} }") should be (2)
  }

  it should "count type of a simple object declaration nested in a class" in {
    TypeCounter.countTypes("class A { object B {} }") should be (2)
  }

  it should "count type of a simple object declaration nested in an object" in {
    TypeCounter.countTypes("object A { object B {} }") should be (2)
  }

  it should "count type of a simple class declaration nested in a function" in {
    val source = """
      def fooBar(i: Int) = {
        class B { val a = 1 }
        i + new B().a
      }"""
    TypeCounter.countTypes(source) should be (1)
  }

  it should "count type of a simple class declaration nested in a value definition" in {
    val source = """
      val fooBar = {
        class B { val a = 1 }
        1 + new B().a
      }"""
    TypeCounter.countTypes(source) should be (1)
  }

  it should "count type of a simple class declaration nested in an assignment" in {
    val source = """
      fooBar = {
        class B { val a = 1 }
        1 + new B().a
      }"""
    TypeCounter.countTypes(source) should be (1)
  }

  it should "count type of a simple class declaration nested in a code block" in {
    val source = """
      {
        1 + new B().a
        class B { val a = 1 }
      }"""
    TypeCounter.countTypes(source) should be (1)
  }

  it should "count type of a simple class declaration nested in a loop" in {
    val source = """
      var i = 0
      while (i == 2) {
        i = i + new B().a
        class B { val a = 1 }
      }"""
    TypeCounter.countTypes(source) should be (1)
  }

  it should "count type of a simple class declaration nested in a match statement" in {
    val source = """
      var i = 0
      i match {
        case 0 => class B { val a = 1 }
        case _ =>
      }"""
    TypeCounter.countTypes(source) should be (1)
  }

  it should "count type of a simple class declaration nested in a try statement" in {
    val source = """
      try {
        class B { val a = 1 }
      } catch {
        case _ =>
      }"""
    TypeCounter.countTypes(source) should be (1)
  }
}