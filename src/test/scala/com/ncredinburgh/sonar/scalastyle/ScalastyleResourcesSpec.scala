/*
 * Sonar Scalastyle Plugin
 * Copyright (C) 2014 All contributors
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
package com.ncredinburgh.sonar.scalastyle

import java.io.InputStream

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{FlatSpec, Inspectors, Matchers, PrivateMethodTester}
import org.sonar.api.PropertyType

import scala.xml.Elem

/**
 * Tests ScalastyleResources
 */
@RunWith(classOf[JUnitRunner])
class ScalastyleResourcesSpec  extends FlatSpec with Matchers with Inspectors with PrivateMethodTester {

  it should "get default_config.xml from Scalastyle jar" in {
    val xmlFromClassPath = PrivateMethod[Elem]('xmlFromClassPath)
    val definitions = ScalastyleResources invokePrivate xmlFromClassPath("/scalastyle_definition.xml")
    assert(definitions.isInstanceOf[Elem])
  }

  it should "get scalastyle_definition.xml from Scalastyle jar" in {
    val xmlFromClassPath = PrivateMethod[Elem]('xmlFromClassPath)
    val scalastyleDefinitions = ScalastyleResources invokePrivate xmlFromClassPath("/scalastyle_definition.xml")
    assert(scalastyleDefinitions.isInstanceOf[Elem])
  }

  it should "get scalastyle_documentation.xml from Scalastyle jar" in {
    val xmlFromClassPath = PrivateMethod[Elem]('xmlFromClassPath)
    val scalastyleDocumentation = ScalastyleResources invokePrivate xmlFromClassPath("/scalastyle_documentation.xml")
    assert(scalastyleDocumentation.isInstanceOf[Elem])
  }

  "the configuration" should "allow access to description in documentation for a checker" in {
    ScalastyleResources.description("line.size.limit") shouldEqual "Lines that are too long can be hard to read and horizontal scrolling is annoying."
  }

  it should "return all defined checkers" in {
    ScalastyleResources.allDefinedRules.size shouldEqual 56
  }

  it should "give rules a description" in {
    forAll(ScalastyleResources.allDefinedRules) {r: RepositoryRule => r.description.length should be > 0}
  }

  it should "give rules an id" in {
    forAll(ScalastyleResources.allDefinedRules) {r: RepositoryRule => r.id should not be empty}
  }

  it should "get all parameters of rules with a parameter" in {
    val rule = ScalastyleResources.allDefinedRules.find(_.clazz == "org.scalastyle.scalariform.ParameterNumberChecker")
    rule.get.params map (_.name) shouldEqual List("maxParameters")
  }

  it should "get all parameters of rules with multiple parameters" in {
    val rule = ScalastyleResources.allDefinedRules.find(_.clazz == "org.scalastyle.scalariform.MethodNamesChecker")
    rule.get.params map (_.name) shouldEqual List("regex", "ignoreRegex", "ignoreOverride")
  }

  it should "get labels from configuration" in {
    ScalastyleResources.label("disallow.space.after.token") shouldEqual "Space after tokens"
    ScalastyleResources.label("no.whitespace.before.left.bracket") shouldEqual "No whitespace before left bracket ''[''"
  }

  it should "get description from configuration" in {
    ScalastyleResources.description("magic.number") shouldEqual "Replacing a magic number with a named constant can make code easier to read and understand, and can avoid some subtle bugs."

    // In case no long description found, return the short description
    ScalastyleResources.label("disallow.space.after.token") shouldEqual "Space after tokens"
  }

  it should "get parameter key from node" in {
    val xmlFromClassPath = PrivateMethod[Elem]('xmlFromClassPath)
    val nodeToParameterKey = PrivateMethod[String]('nodeToParameterKey)

    val key = "org.scalastyle.scalariform.ParameterNumberChecker"
    val definitions = ScalastyleResources invokePrivate xmlFromClassPath("/scalastyle_definition.xml")
    val ruleNodes = definitions \\ "scalastyle-definition" \ "checker"
    val ruleNode = ruleNodes find { _ \\ "@class" exists (_.text == key) }

    ruleNode match {
      case Some(node) => {
        val parameter = (node \ "parameters" \ "parameter").head
        ScalastyleResources invokePrivate nodeToParameterKey(parameter) shouldEqual "maxParameters"
      }
      case _ => fail("rule with key " + key + "could not found")
    }
  }

  it should "get property type from node" in {
    val xmlFromClassPath = PrivateMethod[Elem]('xmlFromClassPath)
    val nodeToPropertyType = PrivateMethod[PropertyType]('nodeToPropertyType)

    val key = "org.scalastyle.scalariform.ParameterNumberChecker"
    val definitions = ScalastyleResources invokePrivate xmlFromClassPath("/scalastyle_definition.xml")
    val ruleNodes = definitions \\ "scalastyle-definition" \ "checker"
    val ruleNode = ruleNodes find { _ \\ "@class" exists (_.text == key) }

    ruleNode match {
      case Some(node) => {
        val parameter = (node \ "parameters" \ "parameter").head
        ScalastyleResources invokePrivate nodeToPropertyType(parameter) shouldEqual PropertyType.INTEGER
      }
      case _ => fail("rule with key " + key + "could not found")
    }
  }

  it should "get default value from node" in {
    val xmlFromClassPath = PrivateMethod[Elem]('xmlFromClassPath)
    val nodeToDefaultValue = PrivateMethod[String]('nodeToDefaultValue)

    val key = "org.scalastyle.scalariform.ParameterNumberChecker"
    val definitions = ScalastyleResources invokePrivate xmlFromClassPath("/scalastyle_definition.xml")
    val ruleNodes = definitions \\ "scalastyle-definition" \ "checker"
    val ruleNode = ruleNodes find { _ \\ "@class" exists (_.text == key) }

    ruleNode match {
      case Some(node) => {
        val parameter = (node \ "parameters" \ "parameter").head
        ScalastyleResources invokePrivate nodeToDefaultValue(parameter) shouldEqual "8"
      }
      case _ => fail("rule with key " + key + "could not found")
    }
  }

}
