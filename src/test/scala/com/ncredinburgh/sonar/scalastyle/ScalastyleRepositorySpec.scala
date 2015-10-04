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

import org.junit.runner.RunWith
import org.scalatest._
import org.scalatest.junit.JUnitRunner
import org.sonar.api.rules.{Rule, RulePriority}

import scala.collection.JavaConversions._

/**
 * Tests ScalastyleRepository
 */
@RunWith(classOf[JUnitRunner])
class ScalastyleRepositorySpec extends FlatSpec with Matchers with Inspectors {

  val testee = new ScalastyleRepository

  "a scalastyle repository" should "return a list of rules" in {
    assert(testee.createRules.nonEmpty)
  }

  it should "use the same repository key for all rules" in {
    forAll(testee.createRules) {r: Rule => r.getRepositoryKey shouldEqual Constants.RepositoryKey}
  }

  it should "set default severity to major" in {
    forAll(testee.createRules) {r: Rule => r.getSeverity shouldEqual RulePriority.MAJOR}
  }

  it should "name the rule after its short description" in {
    val rule = testee.createRules.find(_.getKey == "org.scalastyle.scalariform.MagicNumberChecker")
    rule.get.getName shouldEqual "Checks for use of magic numbers"
  }

  it should "use describe rule using long description" in {
    val rule = testee.createRules.find(_.getKey == "org.scalastyle.scalariform.MagicNumberChecker")
    rule.get.getDescription shouldEqual
      "Replacing a magic number with a named constant can make code easier to read and understand, and can avoid some subtle bugs."
  }

  it should "determine the parameter of a rule with a parameter" in {
    val rule = testee.createRules.find(_.getKey == "org.scalastyle.scalariform.ParameterNumberChecker")
    rule.get.getParams map (_.getKey) shouldEqual List("maxParameters")
  }

  it should "determine parameters of a rule with multiple parameters" in {
    val rule = testee.createRules.find(_.getKey == "org.scalastyle.scalariform.MethodNamesChecker")
    rule.get.getParams map (_.getKey) shouldEqual List("regex", "ignoreRegex", "ignoreOverride")
  }

  it should "determine correct type of integer parameters" in {
    val rule = testee.createRules.find(_.getKey == "org.scalastyle.scalariform.ParameterNumberChecker")
    rule.get.getParam("maxParameters").getType shouldEqual "INTEGER"
  }

  it should "determine correct type of boolean parameters" in {
    val rule = testee.createRules.find(_.getKey == "org.scalastyle.scalariform.MethodNamesChecker")
    rule.get.getParam("ignoreOverride").getType shouldEqual "BOOLEAN"
  }

  it should "determine correct type of regex parameters" in {
    val rule = testee.createRules.find(_.getKey == "org.scalastyle.scalariform.ClassTypeParameterChecker")
    rule.get.getParam("regex").getType shouldEqual "REGULAR_EXPRESSION"
  }

  it should "provide default parameters to scalastyle preferred defaults for rules with a parameter" in {
    val rule = testee.createRules.find(_.getKey == "org.scalastyle.scalariform.ParameterNumberChecker")
    rule.get.getParam("maxParameters").getDefaultValueAsInteger shouldEqual 8
  }
    it should "provide default parameters to scalastyle preferred defaults for rules with multiple parameters" in {
    val rule = testee.createRules.find(_.getKey == "org.scalastyle.scalariform.MethodNamesChecker")
    rule.get.getParam("regex").getDefaultValue shouldEqual "^[a-z][A-Za-z0-9]*(_=)?$"
    rule.get.getParam("ignoreRegex").getDefaultValue shouldEqual "^$"
    rule.get.getParam("ignoreOverride").getDefaultValueAsBoolean shouldEqual false
  }
}
