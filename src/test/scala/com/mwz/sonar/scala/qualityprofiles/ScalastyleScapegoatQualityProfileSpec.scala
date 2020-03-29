/*
 * Copyright (C) 2018-2020  All sonar-scala contributors
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

package com.mwz.sonar.scala.qualityprofiles

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.{Inspectors, LoneElement}
import org.sonar.api.server.profile.BuiltInQualityProfilesDefinition.{BuiltInQualityProfile, Context}

/** Tests the correct behavior of the Scalastyle+Scapegoat Quality Profile */
class ScalastyleScapegoatQualityProfileSpec
    extends AnyFlatSpec
    with Inspectors
    with LoneElement
    with Matchers {
  trait Ctx {
    val context = new Context()
    new ScalastyleScapegoatQualityProfile().define(context)
    val qualityProfile: BuiltInQualityProfile =
      context.profilesByLanguageAndName.loneElement.value.loneElement.value
    val rules = qualityProfile.rules
  }

  "Scalastyle+ScapegoatQualityProfile" should "define only one quality profile" in new Ctx {
    context.profilesByLanguageAndName should have size 1 // by language
    context.profilesByLanguageAndName.loneElement.value should have size 1 // by language and name
  }

  it should "properly define the properties of the quality profile" in new Ctx {
    qualityProfile.name shouldBe "Scalastyle+Scapegoat"
    qualityProfile.language shouldBe "scala"
  }

  it should "not be the default quality profile" in new Ctx {
    qualityProfile.isDefault shouldBe false
  }

  it should "define all Scalastyle + Scapegoat rules" in new Ctx {
    qualityProfile.rules should have size 187 // 69 from Scalastyle + 118 from Scapegoat
  }

  it should "have all rules come from either the Scalastyle or the Scapegaot rules repositories" in new Ctx {
    forEvery(rules) { rule =>
      rule.repoKey should (be("sonar-scala-scalastyle") or be("sonar-scala-scapegoat"))
    }
  }

  it should "not have overridden any of the default params" in new Ctx {
    forEvery(rules)(rule => rule.overriddenParams shouldBe empty)
  }
}
