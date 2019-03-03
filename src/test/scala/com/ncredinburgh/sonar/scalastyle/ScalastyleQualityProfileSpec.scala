/*
 * Copyright (C) 2018-2019  All sonar-scala contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU General Lesser Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.ncredinburgh.sonar.scalastyle

import com.ncredinburgh.sonar.scalastyle.testUtils.TestRuleFinderWithTemplates
import org.scalatest._
import org.scalatest.mockito.MockitoSugar
import org.sonar.api.profiles.RulesProfile
import org.sonar.api.utils.ValidationMessages

import scala.collection.JavaConverters._

/**
 * Tests an adapted ScalastyleQualityProfile, assuming the user instantiated all templates once
 */
class ScalastyleQualityProfileSpec extends FlatSpec with Matchers with MockitoSugar {
  trait Fixture {
    val validationMessages = ValidationMessages.create
    val testee = new ScalastyleQualityProfile(TestRuleFinderWithTemplates)
  }

  val rulesCount = 42
  val parametersCount = 30

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
    val totalParameters = parametersCount + (rulesCount * 1)

    val rulesProfile = testee.createProfile(validationMessages)

    rulesProfile.getActiveRules.asScala.flatMap(_.getActiveRuleParams.asScala).size shouldBe totalParameters
  }

  it should "have correct values for parameters" in new Fixture {
    val ruleKey = "scalastyle_NumberOfMethodsInTypeChecker"

    val rulesProfile = testee.createProfile(validationMessages)
    val rule = rulesProfile.getActiveRule(Constants.RepositoryKey, ruleKey)
    val param = rule.getActiveRuleParams.asScala.head

    param.getKey shouldBe "maxMethods"
    param.getValue shouldBe "30"
  }
}
