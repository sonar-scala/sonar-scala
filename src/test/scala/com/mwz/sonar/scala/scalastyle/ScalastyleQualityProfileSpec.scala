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
package com.mwz.sonar.scala.scalastyle

import org.scalatest.FlatSpec
import org.scalatest.Inspectors
import org.scalatest.LoneElement
import org.scalatest.Matchers
import org.sonar.api.server.profile.BuiltInQualityProfilesDefinition.Context
import scala.collection.JavaConversions._
import scala.collection.JavaConverters._

/** Tests the correct behavior of the Scalastyle Quality Profile */
class ScalastyleQualityProfileSpec extends FlatSpec with Inspectors with LoneElement with Matchers {
  // tests about properties of the scapegoat quality profile
  val context = new Context()
  new ScalastyleQualityProfile().define(context)
  behavior of "the Scalastyle Quality Profile"

  it should "define only one quality profile" in {
    context.profilesByLanguageAndName should have size 1 // by language
    context.profilesByLanguageAndName.loneElement.value should have size 1 // by language and name
  }

  it should "properly define the properties of the quality profile" in {
    val scalastyleQualityProfile = context.profilesByLanguageAndName.loneElement.value.loneElement.value

    scalastyleQualityProfile.name shouldBe "Scalastyle"
    scalastyleQualityProfile.language shouldBe "scala"
  }

  it should "not be the default quality profile" in {
    val scalastyleQualityProfile = context.profilesByLanguageAndName.loneElement.value.loneElement.value

    scalastyleQualityProfile should not be 'default
  }

  // tests about properties of the scapegoat quality profile
  val scalastyleQualityProfile = context.profile("scala", "Scalastyle")
  val rules = scalastyleQualityProfile.rules
  behavior of "all Scalastyle active Rules"

  it should "be from the Scalastyle Rules Repository" in {
    forEvery(rules) { rule =>
      rule.repoKey shouldBe "sonar-scala-scalastyle"
    }
  }
}
