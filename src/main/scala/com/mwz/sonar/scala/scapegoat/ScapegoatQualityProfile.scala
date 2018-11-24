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

import com.mwz.sonar.scala.qualityprofiles.Overrides
import org.sonar.api.server.profile.BuiltInQualityProfilesDefinition
import org.sonar.api.server.profile.BuiltInQualityProfilesDefinition.{
  NewBuiltInActiveRule,
  NewBuiltInQualityProfile
}

/** Defines a quality profile that activates all Scapegoat rules/inspections */
final class ScapegoatQualityProfile extends BuiltInQualityProfilesDefinition {
  override def define(context: BuiltInQualityProfilesDefinition.Context): Unit = {
    // Create an empty profile.
    val profile =
      context
        .createBuiltInQualityProfile(ScapegoatQualityProfile.ProfileName, Scala.LanguageKey)

    // Ensure this is not the default profile.
    profile.setDefault(false)

    // Activate all rules in the Scapegoat rules repository.
    ScapegoatQualityProfile.activateAllRules(profile)

    // Save the profile.
    profile.done()
  }
}

object ScapegoatQualityProfile {
  private[scapegoat] final val ProfileName: String = "Scapegoat"

  /**
   * Activates all rules in the Scapegoat rules repository for the given quality profile.
   */
  def activateAllRules(profile: NewBuiltInQualityProfile): Unit = {
    ScapegoatInspections.AllInspections.foreach { inspection =>
      profile.activateRule(ScapegoatRulesRepository.RepositoryKey, inspection.id)
    }
  }

  /**
   * Enables Scapegoat rules excluding blacklisted rules.
   * Overrides the default severity.
   */
  def activateWithOverrides(profile: NewBuiltInQualityProfile, overrides: Overrides): Unit = {
    ScapegoatInspections.AllInspections
      .filterNot(inspection => overrides.blacklist.contains(inspection.id))
      .foreach { inspection =>
        val rule: NewBuiltInActiveRule =
          profile.activateRule(ScapegoatRulesRepository.RepositoryKey, inspection.id)

        // Override the severity.
        overrides.severities
          .get(inspection.id)
          .foreach(severity => rule.overrideSeverity(severity.name))
      }
  }
}
