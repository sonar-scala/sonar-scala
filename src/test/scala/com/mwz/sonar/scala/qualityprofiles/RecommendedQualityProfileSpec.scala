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

package com.mwz.sonar.scala
package qualityprofiles

import org.scalatest._
import org.sonar.api.server.profile.BuiltInQualityProfilesDefinition.{
  BuiltInActiveRule,
  BuiltInQualityProfile,
  Context
}

import scala.collection.JavaConverters._

class RecommendedQualityProfileSpec
    extends FlatSpec
    with Inspectors
    with LoneElement
    with OptionValues
    with Matchers {
  trait Ctx {
    val context: Context = new Context()
    new RecommendedQualityProfile().define(context)
    val qualityProfile: BuiltInQualityProfile =
      context.profilesByLanguageAndName.loneElement.value.loneElement.value
    val rules: Seq[BuiltInActiveRule] = qualityProfile.rules.asScala
  }

  "RecommendedQualityProfile" should "define a quality profile" in new Ctx {
    qualityProfile.language shouldBe "scala"
    qualityProfile.name shouldBe "Recommended by sonar-scala"
  }

  it should "be the default profile" in new Ctx {
    qualityProfile.isDefault shouldBe true
  }

  it should "have 175 rules" in new Ctx {
    rules.size shouldBe 175 // 61 from Scalastyle + 114 from Scapegoat
  }

  it should "have all rules come from either the Scalastyle or the Scapegoat rules repositories" in new Ctx {
    forEvery(rules) { rule =>
      rule.repoKey should (be("sonar-scala-scalastyle") or be("sonar-scala-scapegoat"))
    }
  }

  it should "have overridden the default params" in new Ctx {
    val rulesWithOverridenParams = rules.filterNot(_.overriddenParams.isEmpty)
    val paramOverrides = RecommendedQualityProfile.ScapegoatOverrides.params ++
      RecommendedQualityProfile.ScalastyleOverrides.params

    rulesWithOverridenParams.size shouldBe paramOverrides.size
    forEvery(rulesWithOverridenParams) { rule =>
      rule.overriddenParams.asScala.map(p => (p.key, p.overriddenValue)) should contain theSameElementsAs
      paramOverrides.get(rule.ruleKey).value.toSeq
    }
  }

  it should "have overridden the default severities" in new Ctx {
    val rulesWithOverridenSeverities = rules.filterNot(rule => Option(rule.overriddenSeverity).isEmpty)
    val severityOverrides = RecommendedQualityProfile.ScapegoatOverrides.severities ++
      RecommendedQualityProfile.ScalastyleOverrides.severities

    rulesWithOverridenSeverities.size shouldBe severityOverrides.size
    forEvery(rulesWithOverridenSeverities) { rule =>
      rule.overriddenSeverity shouldBe severityOverrides.get(rule.ruleKey).value.name
    }
  }
}
