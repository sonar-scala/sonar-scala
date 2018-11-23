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

import com.mwz.sonar.scala.scalastyle.{ScalastyleInspections, ScalastyleRulesRepository}
import com.mwz.sonar.scala.scapegoat.{ScapegoatInspections, ScapegoatRulesRepository}
import org.sonar.api.batch.rule.Severity
import org.sonar.api.server.profile.BuiltInQualityProfilesDefinition
import org.sonar.api.server.profile.BuiltInQualityProfilesDefinition.{
  NewBuiltInActiveRule,
  NewBuiltInQualityProfile
}

/**
 * Defines a quality profile recommended by sonar-scala (including Scalastyle and Scapegoat).
 */
final class RecommendedQualityProfile extends BuiltInQualityProfilesDefinition {
  override def define(context: BuiltInQualityProfilesDefinition.Context): Unit = {
    // Create an empty profile.
    val profile: NewBuiltInQualityProfile =
      context.createBuiltInQualityProfile(ScalastyleScapegoatQualityProfile.ProfileName, Scala.LanguageKey)

    // Enable Scalastyle rules - excluding those which have been blacklisted and templates.
    ScalastyleInspections.AllInspections
      .filterNot { i =>
        RecommendedQualityProfile.Blacklist.contains(i.id) ||
        i.params.nonEmpty
      }
      .foreach { inspection =>
        RecommendedQualityProfile.processRule(
          profile,
          ScalastyleRulesRepository.RepositoryKey,
          inspection.clazz,
          inspection.id
        )
      }

    // TODO: Scalastyle template instances.

    // Enable Scapegoat rules - excluding those which have been blacklisted.
    ScapegoatInspections.AllInspections
      .filterNot(inspection => RecommendedQualityProfile.Blacklist.contains(inspection.id))
      .foreach { inspection =>
        RecommendedQualityProfile.processRule(
          profile,
          ScapegoatRulesRepository.RepositoryKey,
          inspection.id,
          inspection.id
        )
      }

    // Ensure this is not the default profile.
    profile.setDefault(false)

    // Save the profile.
    profile.done()
  }
}

private[qualityprofiles] object RecommendedQualityProfile {
  final val ProfileName: String = "Recommended by sonar-scala"
  final val Blacklist: Set[String] = Set(
    // Scalastyle
    "block.import", // avoid block imports
    "lowercase.pattern.match", // lowercase pattern match
    "no.newline.at.eof", // no newline at EOF
    "pattern.match.align", // pattern match align
    "underscore.import", // avoid wildcard imports
    // Scapegoat
    "com.sksamuel.scapegoat.inspections.naming.ClassNames", // exists in Scalastyle (class.name)
    "com.sksamuel.scapegoat.inspections.string.EmptyInterpolatedString", // exists in Scalastyle (empty.interpolated.strings)
    "com.sksamuel.scapegoat.inspections.unneccesary.UnnecessaryReturnUse" // exists in Scalastyle (return)
  )
  final val Severities: Map[String, Severity] = Map(
    "covariant.equals" -> Severity.MAJOR,
    "equals.hash.code" -> Severity.MAJOR,
    "illegal.imports" -> Severity.MAJOR,
    "null" -> Severity.MAJOR,
    "var.field" -> Severity.MAJOR,
    "var.local" -> Severity.MAJOR,
  )
  final val Params: Map[String, Map[String, String]] = Map(
    // Scalastyle
    "if.brace" -> Map("doubleLineAllowed" -> "true") // "if" without braces allowed if everything is on one or two lines
  )

  def processRule(
    profile: NewBuiltInQualityProfile,
    repoKey: String,
    ruleKey: String,
    inspectionId: String
  ) = {
    val rule: NewBuiltInActiveRule =
      profile.activateRule(repoKey, ruleKey)

    // Override the severity.
    RecommendedQualityProfile.Severities
      .get(inspectionId)
      .foreach(severity => rule.overrideSeverity(severity.name))

    // Override rule params.
    RecommendedQualityProfile.Params
      .get(inspectionId)
      .foreach {
        _.foreach {
          case (k, v) => rule.overrideParam(k, v)
        }
      }
  }
}
