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
import org.sonar.api.rules.RulePriority
import org.sonar.api.server.rule.{RuleParamType, RulesDefinition}


import scala.collection.JavaConversions._

/**
 * Tests ScalastyleRepository
 */
@RunWith(classOf[JUnitRunner])
class ScalastyleRepositorySpec extends FlatSpec with Matchers with Inspectors with BeforeAndAfterAll {

  val testee = new ScalastyleRepository
  val ctx = new RulesDefinition.Context()

  def rules = ctx.repository(Constants.RepositoryKey).rules()

  override def beforeAll() {
    testee.define(ctx)
  }

  "a scalastyle repository" should "return a list of rules" in {
    rules should not be empty
  }

  it should "use the same repository for all rules" in {
    forAll(rules) {
      r: RulesDefinition.Rule =>
        r.repository().key() shouldEqual Constants.RepositoryKey
        r.repository().name() shouldEqual Constants.RepositoryName
    }
  }

  it should "consist of 63 rules" in {
    rules.size() shouldEqual 63
  }

  it should "set default severity to major" in {
    forAll(rules) {r: RulesDefinition.Rule => r.severity() shouldEqual RulePriority.MAJOR.name()}
  }

  it should "give a name to every rule" in {
    rules.filter(_.name == null) should be(empty)
  }

  it should "set the ClazzParam for every rule" in {
    rules.filter(_.param(Constants.ClazzParam) == null) should be(empty)
  }

  it should "name the rule properly" in {
    val rule = rules.find(_.key == "scalastyle_MagicNumberChecker")
    rule.get.name shouldEqual "Magic Number"
  }

  it should "describe the rule properly" in {
    val rule = rules.find(_.key == "scalastyle_MagicNumberChecker")
    rule.get.htmlDescription shouldEqual
      "<p>Replacing a magic number with a named constant can make code easier to read and understand," +
        " and can avoid some subtle bugs.</p>\n" +
        "<p>A simple assignment to a val is not considered to be a magic number, for example:</p>\n" +
        "<p><pre>    val foo = 4</pre></p>\n<p>is not a magic number, but</p>\n" +
        "<p><pre>    var foo = 4</pre></p>\n<p>is considered to be a magic number.</p>"
  }

  it should "determine the parameter of a rule with a parameter" in {
    val rule = rules.find(_.key == "scalastyle_ParameterNumberChecker")
    rule.get.params map (_.key) shouldEqual List("maxParameters", Constants.ClazzParam)
  }

  it should "determine parameters of a rule with multiple parameters" in {
    val rule = rules.find(_.key == "scalastyle_MethodNamesChecker")
    rule.get.params map (_.key) should contain theSameElementsAs List("regex", "ignoreRegex", "ignoreOverride", Constants.ClazzParam)
  }

  it should "determine correct type of integer parameters" in {
    val rule = rules.find(_.key == "scalastyle_ParameterNumberChecker")
    rule.get.param("maxParameters").`type` shouldEqual RuleParamType.INTEGER
  }

  it should "determine correct type of boolean parameters" in {
    val rule = rules.find(_.key == "scalastyle_MethodNamesChecker")
    rule.get.param("ignoreOverride").`type` shouldEqual RuleParamType.BOOLEAN
  }

  it should "determine correct type of regex parameters" in {
    val rule = rules.find(_.key == "scalastyle_ClassTypeParameterChecker")
    rule.get.param("regex").`type` shouldEqual RuleParamType.STRING
  }

  it should "describe the parameter properly" in {
    val rule = rules.find(_.key == "scalastyle_ClassTypeParameterChecker")
    rule.get.param("regex").description shouldEqual "Standard Scala regular expression syntax"
  }

  it should "provide default parameters to scalastyle preferred defaults for rules with a parameter" in {
    val rule = rules.find(_.key == "scalastyle_ParameterNumberChecker")
    rule.get.param("maxParameters").defaultValue.toInt shouldEqual 8
  }

  it should "provide default parameters to scalastyle preferred defaults for rules with multiple parameters" in {
    val rule = rules.find(_.key == "scalastyle_MethodNamesChecker")
    rule.get.param("regex").defaultValue shouldEqual "^[a-z][A-Za-z0-9]*(_=)?$"
    rule.get.param("ignoreRegex").defaultValue shouldEqual "^$"
    rule.get.param("ignoreOverride").defaultValue.toBoolean shouldEqual false
  }
}
