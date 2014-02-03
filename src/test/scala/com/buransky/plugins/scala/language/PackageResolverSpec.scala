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

import com.buransky.plugins.scala.util.FileTestUtils
;

@RunWith(classOf[JUnitRunner])
class PackageResolverSpec extends FlatSpec with ShouldMatchers {

  "A package resolver" should "resolve the package name of a simple package declaration" in {
    getPackageNameOf("SimplePackageDeclaration") should equal ("one")
  }

  it should "resolve the package name of a nested package declaration" in {
    getPackageNameOf("NestedPackageDeclaration") should equal ("one.two.three")
  }

  it should "resolve the package name of a deep nested package declaration" in {
    getPackageNameOf("DeepNestedPackageDeclaration") should equal ("one.two.three.four.five.six." +
        "seven.eight.nine.ten.eleven.twelve.thirteen.fourteen.fifteen.sixteen")
  }

  it should "resolve the upper package name of a nested package declaration with " +
      "an object declaration between" in {
    getPackageNameOf("NestedPackageDeclarationWithObjectBetween") should equal ("one.two")
  }

  it should "resolve the package name of a deep nested package declaration with" +
      "an object declaration between" in {
    getPackageNameOf("DeepNestedPackageDeclarationWithObjectBetween") should equal ("one.two.three." +
        "four.five.six.seven.eight")
  }

  private def getPackageNameOf(fileName: String) = {
    val path = FileTestUtils.getRelativePath("/packageResolver/" + fileName + ".txt")
    PackageResolver.resolvePackageNameOfFile(path)
  }
}