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

import com.ncredinburgh.sonar.scalastyle.testUtils.TestRuleFinder
import org.scalatest._
import org.scalatest.mockito.MockitoSugar
import org.sonar.api.profiles.RulesProfile
import org.sonar.api.utils.ValidationMessages

import scala.collection.JavaConverters._

/**
 * Tests the default ScalastyleQualityProfile, only rules without parameters, no templates
 */
class ScalastyleDefaultQualityProfileSpec extends FlatSpec with Matchers with MockitoSugar {
  trait Fixture {
    val validationMessages = ValidationMessages.create
    val testee = new ScalastyleQualityProfile(TestRuleFinder)
  }

  val rulesCount = 18 // rules without templates

  "a scalastyle quality profile" should "create a default profile" in new Fixture {
    val rulesProfile = testee.createProfile(validationMessages)

    rulesProfile.getClass shouldEqual classOf[RulesProfile]
    rulesProfile.getName shouldEqual Constants.ProfileName
    rulesProfile.getLanguage shouldEqual Constants.ScalaKey
  }

  "the default quality profile" should "have all the rules in default config" in new Fixture {
    val rulesProfile = testee.createProfile(validationMessages)

    rulesProfile.getActiveRules.size shouldBe rulesCount
  }

  it should "have all the parameters in default config" in new Fixture {
    val totalParameters = rulesCount * 1

    val rulesProfile = testee.createProfile(validationMessages)

    rulesProfile.getActiveRules.asScala.flatMap(_.getActiveRuleParams.asScala).size shouldBe totalParameters
  }
}
