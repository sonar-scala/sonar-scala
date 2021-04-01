/*
 * Copyright (C) 2018-2021  All sonar-scala contributors
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
package scalastyle

import com.mwz.sonar.scala.metadata.scalastyle.ScalastyleRules
import com.mwz.sonar.scala.metadata.scalastyle.ScalastyleRulesRepository
import com.mwz.sonar.scala.qualityprofiles.Overrides
import org.sonar.api.server.profile.BuiltInQualityProfilesDefinition
import org.sonar.api.server.profile.BuiltInQualityProfilesDefinition.NewBuiltInActiveRule
import org.sonar.api.server.profile.BuiltInQualityProfilesDefinition.NewBuiltInQualityProfile

/**
 * Defines a Scalastyle quality profile.
 */
final class ScalastyleQualityProfile extends BuiltInQualityProfilesDefinition {
  override def define(context: BuiltInQualityProfilesDefinition.Context): Unit = {
    // Create an empty profile.
    val profile = context.createBuiltInQualityProfile(ScalastyleQualityProfile.ProfileName, Scala.LanguageKey)

    // Ensure this is not the default profile.
    profile.setDefault(false)

    // Activate all rules in the Scalastyle rules repository.
    // (except for those which were not included in the repository)
    ScalastyleQualityProfile.activateRules(profile)

    // Save the profile.
    profile.done()
  }
}

object ScalastyleQualityProfile {
  private[scalastyle] final val ProfileName: String = "Scalastyle"

  // Blacklist the following inspections.
  private[scalastyle] final val BlacklistRules: Set[String] = Set(
    // it is the opposite to "org.scalastyle.file.NewLineAtEofChecker"
    "org.scalastyle.file.NoNewLineAtEofChecker"
  )

  /**
   * Activates Scalastyle rules for the given profile excluding blacklisted rules.
   * Overrides the default severity and parameter values if provided in overrides.
   */
  def activateRules(
    profile: NewBuiltInQualityProfile,
    overrides: Option[Overrides] = None
  ): Unit = {
    ScalastyleRules.rules
      .filterNot { rule =>
        ScalastyleRulesRepository.SkipTemplateInstances.contains(rule.key) ||
        BlacklistRules.contains(rule.key) ||
        overrides.exists(_.blacklist.contains(rule.key))
      }
      .iterator
      .foreach { rule =>
        val activeRule: NewBuiltInActiveRule =
          profile.activateRule(ScalastyleRulesRepository.RepositoryKey, rule.key)

        overrides.foreach { overrides =>
          // Override the severity.
          overrides.severities
            .get(rule.key)
            .foreach(severity => activeRule.overrideSeverity(severity.name))

          // Override rule params.
          overrides.params
            .get(rule.key)
            .foreach {
              _.foreach { case (k, v) =>
                activeRule.overrideParam(k, v)
              }
            }
        }
      }
  }
}
