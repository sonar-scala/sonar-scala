/*
 * Sonar Scala Plugin
 * Copyright (C) 2018 All contributors
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
package com.mwz.sonar.scala
package scalastyle

import com.mwz.sonar.scala.scalastyle.ScalastyleRulesRepository.SkipTemplateInstances
import org.scalatest.{FlatSpec, Inspectors, LoneElement, Matchers}
import org.sonar.api.server.profile.BuiltInQualityProfilesDefinition.{
  BuiltInActiveRule,
  BuiltInQualityProfile,
  Context
}

import scala.collection.JavaConverters._

class ScalastyleQualityProfileSpec extends FlatSpec with Matchers with LoneElement with Inspectors {
  trait Ctx {
    val context = new Context()
    new ScalastyleQualityProfile().define(context)
    val qualityProfile: BuiltInQualityProfile =
      context.profilesByLanguageAndName.loneElement.value.loneElement.value
    val rules: Seq[BuiltInActiveRule] = qualityProfile.rules.asScala
  }

  "ScalastyleQualityProfile" should "define a quality profile" in new Ctx {
    qualityProfile.language shouldBe "scala"
    qualityProfile.name shouldBe "Scalastyle"
  }

  it should "not be the default profile" in new Ctx {
    qualityProfile.isDefault shouldBe false
  }

  it should "activate all default (non-template) rules" in new Ctx {
    rules.map(_.ruleKey) should contain allElementsOf
    ScalastyleInspections.AllInspections.filter(_.params.isEmpty).map(_.clazz)
  }

  it should "have 67 rules" in new Ctx {
    rules should have size 67
  }

  it should "not activate templates" in new Ctx {
    val templates = ScalastyleInspections.AllInspections
      .filter(_.params.nonEmpty)
      .map(i => s"${i.clazz}-template")

    rules.map(_.ruleKey) should contain noElementsOf templates
  }

  it should "activate not excluded template rules" in new Ctx {
    val templateInstances = ScalastyleInspections.AllInspections
      .filter(i => i.params.nonEmpty && !SkipTemplateInstances.contains(i.id))
      .map(_.clazz)
    rules.map(_.ruleKey) should contain allElementsOf templateInstances

    val excluded = ScalastyleInspections.AllInspections
      .filter(i => SkipTemplateInstances.contains(i.id))
      .map(_.clazz)

    rules.map(_.ruleKey) should contain noElementsOf excluded
  }

  it should "have all rules come from the Scalastyle rules repository" in new Ctx {
    forEvery(rules) { rule =>
      rule.repoKey shouldBe "sonar-scala-scalastyle"
    }
  }
}
