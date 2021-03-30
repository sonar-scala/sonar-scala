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

package com.mwz.sonar.scala.metadata
package scalastyle

import cats.data.NonEmptyChain

object ScalastyleRulesRepository {
  final val RepositoryKey: String = "sonar-scala-scalastyle"
  final val RepositoryName: String = "Scalastyle"
  final val RuleClassParam: String = "ruleClass"

  // Skip creating template instances for the following inspections.
  final val SkipTemplateInstances: Set[String] = Set(
    // this rule wouldn't work with a default parameter value
    "org.scalastyle.file.HeaderMatchesChecker",
    // no default regex provided
    "org.scalastyle.file.RegexChecker",
    // incorrect default value of the ignoreRegex parameter
    "org.scalastyle.scalariform.ScalaDocChecker"
  )

  lazy val rulesRepository: RulesRepository =
    RulesRepository(
      key = RepositoryKey,
      name = RepositoryName,
      rules = ScalastyleRules.rules.flatMap(fromTemplate)
    )

  /**
   * From each template create a new rule (an instance).
   * Also add an additional parameter to capture Scalastyle class name of each rule.
   */
  private[metadata] def fromTemplate(rule: Rule): NonEmptyChain[Rule] = {
    val newRule = rule.copy(params = rule.params :+ extraParam(rule.key), template = false)

    // For each template create a rule with default parameter values.
    // (except for the rules listed in the SkipTemplateInstances set)
    if (rule.params.nonEmpty) {
      val template = newRule.copy(key = rule.key + "-template", template = true)
      if (!SkipTemplateInstances.contains(rule.key))
        NonEmptyChain(template, newRule)
      else NonEmptyChain.one(template)
    } else NonEmptyChain.one(newRule)
  }

  /**
   * Create an extra param to capture Scalastyle class name of each rule.
   * This is used by the Scalastyle sensor.
   */
  private[metadata] def extraParam(key: String): Param =
    Param(
      name = RuleClassParam,
      typ = ParamType.String,
      description = "Scalastyle's rule (checker) class name.",
      default = key
    )
}
