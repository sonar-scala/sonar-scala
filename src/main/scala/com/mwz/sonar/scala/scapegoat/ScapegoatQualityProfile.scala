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
package scapegoat

import inspections.ScapegoatInspection

import org.sonar.api.server.profile.BuiltInQualityProfilesDefinition

/** Defines a quality profile that activates all Scapegoat rules/inspections */
final class ScapegoatQualityProfile extends BuiltInQualityProfilesDefinition {
  override def define(context: BuiltInQualityProfilesDefinition.Context): Unit = {
    // create an empty profile
    val profile =
      context
        .createBuiltInQualityProfile(ScapegoatQualityProfile.ProfileName, Scala.LanguageKey)

    // ensure this is not the default profile
    profile.setDefault(false)

    // activate each rule in the Scapegoat Rules Repository
    ScapegoatInspection.AllScapegoatInspections foreach { inspection =>
      profile.activateRule(ScapegoatRulesRepository.RepositoryKey, inspection.id)
    }

    // save the profile
    profile.done()
  }
}

object ScapegoatQualityProfile {
  private[scapegoat] val ProfileName = "Scapegoat"
}
