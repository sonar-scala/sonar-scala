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
package qualityprofiles

import com.mwz.sonar.scala.scalastyle.{ScalastyleQualityProfile, ScalastyleRulesRepository}
import com.mwz.sonar.scala.scapegoat.ScapegoatQualityProfile
import org.sonar.api.batch.rule.Severity
import org.sonar.api.server.profile.BuiltInQualityProfilesDefinition
import org.sonar.api.server.profile.BuiltInQualityProfilesDefinition.NewBuiltInQualityProfile

/**
 * Defines a quality profile recommended by sonar-scala (including Scalastyle and Scapegoat).
 */
final class RecommendedQualityProfile(
  scalastyleRulesRepository: ScalastyleRulesRepository
) extends BuiltInQualityProfilesDefinition {
  override def define(context: BuiltInQualityProfilesDefinition.Context): Unit = {
    // Create an empty profile.
    val profile: NewBuiltInQualityProfile =
      context.createBuiltInQualityProfile(ScalastyleScapegoatQualityProfile.ProfileName, Scala.LanguageKey)

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
      "block.import", // avoid block imports
      "lowercase.pattern.match", // lowercase pattern match
      "no.newline.at.eof", // no newline at EOF
      "pattern.match.align", // pattern match align
      "underscore.import" // avoid wildcard imports
    ),
    severities = Map(
      "covariant.equals" -> Severity.MAJOR,
      "equals.hash.code" -> Severity.MAJOR,
      "illegal.imports" -> Severity.MAJOR,
      "null" -> Severity.MAJOR,
      "var.field" -> Severity.MAJOR,
      "var.local" -> Severity.MAJOR,
    ),
    params = Map(
      // Scalastyle
      // "if" without braces allowed if everything is on one or two lines
      "if.brace" -> Map("doubleLineAllowed" -> "true"),
      // the classParamIndentSize should be the same as methodParamIndentSize
      "indentation" -> Map("classParamIndentSize" -> "2"),
      // tabSize should be 2 according to the Scala Style Guide (https://docs.scala-lang.org/style/indentation.html)
      // The default maxLineLength of 160 is an abomination!
      "line.size.limit" -> Map("tabSize" -> "2", "maxLineLength" -> "110")
    )
  )
  final val ScapegoatOverrides: Overrides = Overrides(
    blacklist = Set(
      // exists in Scalastyle (class.name)
      "com.sksamuel.scapegoat.inspections.naming.ClassNames",
      // exists in Scalastyle (empty.interpolated.strings)
      "com.sksamuel.scapegoat.inspections.string.EmptyInterpolatedString",
      // exists in Scalastyle (return)
      "com.sksamuel.scapegoat.inspections.unneccesary.UnnecessaryReturnUse"
    ),
    severities = Map.empty,
    params = Map.empty
  )
}
