/*
 * Copyright (C) 2018-2022  All sonar-scala contributors
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
package scapegoat

import com.mwz.sonar.scala.metadata.scapegoat.ScapegoatRules
import com.mwz.sonar.scala.metadata.scapegoat.ScapegoatRulesRepository
import com.mwz.sonar.scala.qualityprofiles.Overrides
import org.sonar.api.server.profile.BuiltInQualityProfilesDefinition
import org.sonar.api.server.profile.BuiltInQualityProfilesDefinition.NewBuiltInActiveRule
import org.sonar.api.server.profile.BuiltInQualityProfilesDefinition.NewBuiltInQualityProfile

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
    ScapegoatQualityProfile.activateRules(profile)

    // Save the profile.
    profile.done()
  }
}

object ScapegoatQualityProfile {
  private[scapegoat] final val ProfileName: String = "Scapegoat"

  /**
   * Activates Scapegoat rules for the given quality profile excluding blacklisted rules.
   * Overrides the default severity if provided in overrides.
   */
  def activateRules(profile: NewBuiltInQualityProfile, overrides: Option[Overrides] = None): Unit = {
    ScapegoatRules.rules
      .filterNot(rule => overrides.exists(_.blacklist.contains(rule.key)))
      .iterator
      .foreach { rule =>
        val activeRule: NewBuiltInActiveRule =
          profile.activateRule(ScapegoatRulesRepository.RepositoryKey, rule.key)

        // Override the severity.
        overrides
          .flatMap(_.severities.get(rule.key))
          .foreach(severity => activeRule.overrideSeverity(severity.name))
      }
  }
}
