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
package qualityprofiles

import com.mwz.sonar.scala.scalastyle.ScalastyleQualityProfile
import com.mwz.sonar.scala.scapegoat.ScapegoatQualityProfile
import org.sonar.api.server.profile.BuiltInQualityProfilesDefinition

/**
 * Defines a Scalastyle+Scapegoat quality profile.
 */
final class ScalastyleScapegoatQualityProfile extends BuiltInQualityProfilesDefinition {
  override def define(context: BuiltInQualityProfilesDefinition.Context): Unit = {
    // Create an empty profile.
    val profile =
      context.createBuiltInQualityProfile(ScalastyleScapegoatQualityProfile.ProfileName, Scala.LanguageKey)

    // Ensure this is not the default profile.
    profile.setDefault(false)

    // Activate all rules in the Scalastyle rules repository.
    ScalastyleQualityProfile.activateRules(profile)

    // Activate all rules in the Scapegoat rules repository.
    ScapegoatQualityProfile.activateRules(profile)

    // Save the profile.
    profile.done()
  }
}

private[qualityprofiles] object ScalastyleScapegoatQualityProfile {
  final val ProfileName: String = "Scalastyle+Scapegoat"
}
