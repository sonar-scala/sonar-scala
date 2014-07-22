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

import org.scalatest._
import org.scalatest.mock.MockitoSugar
import org.sonar.api.profiles.RulesProfile
import org.sonar.api.utils.ValidationMessages

import scala.collection.JavaConversions._

class ScalastyleQualityProfileSpec extends FlatSpec with Matchers with MockitoSugar {
  trait Fixture {
    val scalastyleRepository = new ScalastyleRepository
    val validationMessages = ValidationMessages.create
    val testee = new ScalastyleQualityProfile(scalastyleRepository)
  }

  "a scalastyle quality profile" should "create a default profile" in new Fixture {
    val rulesProfile = testee.createProfile(validationMessages)

    rulesProfile.getClass shouldEqual classOf[RulesProfile]
    rulesProfile.getName shouldEqual Constants.ProfileName
    rulesProfile.getLanguage shouldEqual Constants.ScalaKey
  }

  "the default quality profile" should "have all the rules in default config" in new Fixture {
    val rulesCount = 36

    val rulesProfile = testee.createProfile(validationMessages)

    rulesProfile.getActiveRules.size shouldBe rulesCount
  }

  it should "have all the parameters in default config" in new Fixture {
    val parametersCount = 18

    val rulesProfile = testee.createProfile(validationMessages)

    rulesProfile.getActiveRules.flatMap(_.getActiveRuleParams).size shouldBe parametersCount
  }

  it should "have correct values for parameters" in new Fixture {
    val ruleKey = "org.scalastyle.scalariform.NumberOfMethodsInTypeChecker"

    val rulesProfile = testee.createProfile(validationMessages)
    val rule = rulesProfile.getActiveRule(Constants.RepositoryKey, ruleKey)
    val param = rule.getActiveRuleParams.head

    param.getKey shouldBe "maxMethods"
    param.getValue shouldBe "30"
  }
}