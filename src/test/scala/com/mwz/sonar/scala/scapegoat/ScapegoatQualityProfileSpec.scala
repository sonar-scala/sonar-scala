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
package com.mwz.sonar.scala.scapegoat

import inspections.ScapegoatInspection

import org.scalatest.{FlatSpec, Inspectors, LoneElement, Matchers}
import org.sonar.api.server.profile.BuiltInQualityProfilesDefinition.Context

/** Tests the correct behavior of the Scapegoat Quality Profile */
class ScapegoatQualityProfileSpec extends FlatSpec with Inspectors with LoneElement with Matchers {
  // tests about properties of the scapegoat quality profile
  val context = new Context()
  new ScapegoatQualityProfile().define(context)
  behavior of "the Scapegoat Quality Profile"

  it should "define only one quality profile" in {
    context.profilesByLanguageAndName should have size 1 // by language
    context.profilesByLanguageAndName.loneElement.value should have size 1 // by language and name
  }

  it should "properly define the properties of the quality profile" in {
    val scapegoatQualityProfile = context.profilesByLanguageAndName.loneElement.value.loneElement.value

    scapegoatQualityProfile.name shouldBe "Scapegoat"
    scapegoatQualityProfile.language shouldBe "scala"
  }

  it should "not be the default quality profile" in {
    val scapegoatQualityProfile = context.profilesByLanguageAndName.loneElement.value.loneElement.value

    scapegoatQualityProfile should not be 'default
  }

  it should "activate one rule for each scapegoat inspection" in {
    val scapegoatQualityProfile = context.profilesByLanguageAndName.loneElement.value.loneElement.value
    val totalInspections = ScapegoatInspection.AllScapegoatInspections.length

    scapegoatQualityProfile.rules should have length totalInspections
  }

  // tests about properties of the scapegoat quality profile
  val scapegoatQualityProfile = context.profile("scala", "Scapegoat")
  val rules = scapegoatQualityProfile.rules
  behavior of "all Scapegoat active Rules"

  it should "be from the Scapegaot Rules Repository" in {
    forEvery(rules) { rule =>
      rule.repoKey shouldBe "sonar-scala-scapegoat"
    }
  }
}
