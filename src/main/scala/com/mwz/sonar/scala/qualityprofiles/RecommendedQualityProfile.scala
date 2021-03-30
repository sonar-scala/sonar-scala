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
package qualityprofiles

import com.mwz.sonar.scala.scalastyle.ScalastyleQualityProfile
import com.mwz.sonar.scala.scapegoat.ScapegoatQualityProfile
import org.sonar.api.batch.rule.Severity
import org.sonar.api.server.profile.BuiltInQualityProfilesDefinition
import org.sonar.api.server.profile.BuiltInQualityProfilesDefinition.NewBuiltInQualityProfile

/**
 * Defines a quality profile recommended by sonar-scala (including Scalastyle and Scapegoat).
 */
final class RecommendedQualityProfile extends BuiltInQualityProfilesDefinition {
  override def define(context: BuiltInQualityProfilesDefinition.Context): Unit = {
    // Create an empty profile.
    val profile: NewBuiltInQualityProfile =
      context.createBuiltInQualityProfile(RecommendedQualityProfile.ProfileName, Scala.LanguageKey)

    // Ensure this is the default profile.
    profile.setDefault(true)

    // Activate Scalastyle rules excluding blacklisted rules.
    ScalastyleQualityProfile.activateRules(profile, Some(RecommendedQualityProfile.ScalastyleOverrides))

    // Activate Scapegoat rules excluding blacklisted rules.
    ScapegoatQualityProfile.activateRules(profile, Some(RecommendedQualityProfile.ScapegoatOverrides))

    // Save the profile.
    profile.done()
  }
}

private[qualityprofiles] object RecommendedQualityProfile {
  final val ProfileName: String = "Recommended by sonar-scala"
  final val ScalastyleOverrides: Overrides = Overrides(
    blacklist = Set(
      "org.scalastyle.scalariform.BlockImportChecker", // avoid block imports
      "org.scalastyle.scalariform.CurliesImportChecker", // avoid curlies imports
      "org.scalastyle.scalariform.LowercasePatternMatchChecker", // lowercase pattern match
      "org.scalastyle.scalariform.PatternMatchAlignChecker", // pattern match align
      "org.scalastyle.scalariform.UnderscoreImportChecker" // avoid wildcard imports
    ),
    severities = Map(
      "org.scalastyle.scalariform.CovariantEqualsChecker" -> Severity.MAJOR,
      "org.scalastyle.scalariform.EqualsHashCodeChecker" -> Severity.MAJOR,
      "org.scalastyle.scalariform.IllegalImportsChecker" -> Severity.MAJOR,
      "org.scalastyle.scalariform.NullChecker" -> Severity.MAJOR,
      "org.scalastyle.scalariform.VarFieldChecker" -> Severity.MAJOR,
      "org.scalastyle.scalariform.VarLocalChecker" -> Severity.MAJOR
    ),
    params = Map(
      // Scalastyle
      // "if" without braces allowed if everything is on one or two lines
      "org.scalastyle.scalariform.IfBraceChecker" -> Map("doubleLineAllowed" -> "true"),
      // the classParamIndentSize should be the same as methodParamIndentSize
      "org.scalastyle.file.IndentationChecker" -> Map("classParamIndentSize" -> "2"),
      // tabSize should be 2 according to the Scala Style Guide (https://docs.scala-lang.org/style/indentation.html)
      // The default maxLineLength of 160 is an abomination!
      "org.scalastyle.file.FileLineLengthChecker" -> Map("tabSize" -> "2", "maxLineLength" -> "110")
    )
  )
  final val ScapegoatOverrides: Overrides = Overrides(
    blacklist = Set(
      // exists in Scalastyle (org.scalastyle.scalariform.ClassNamesChecker)
      "com.sksamuel.scapegoat.inspections.naming.ClassNames",
      // exists in Scalastyle (org.scalastyle.scalariform.EmptyInterpolatedStringChecker)
      "com.sksamuel.scapegoat.inspections.string.EmptyInterpolatedString",
      // exists in Scalastyle (org.scalastyle.scalariform.ReturnChecker)
      "com.sksamuel.scapegoat.inspections.unneccesary.UnnecessaryReturnUse"
    ),
    severities = Map.empty,
    params = Map.empty
  )
}
