/*
 * Copyright (C) 2018-2020  All sonar-scala contributors
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

import cats.data.Chain

object ScalastyleRulesRepository {
  // Skip creating template instances for the following inspections.
  private final val SkipTemplateInstances = Set(
    // this rule wouldn't work with a default parameter value
    "org.scalastyle.file.HeaderMatchesChecker",
    // no default regex provided
    "org.scalastyle.file.RegexChecker",
    // incorrect default value of the ignoreRegex parameter
    "org.scalastyle.scalariform.ScalaDocChecker"
  )

  lazy val rulesRepository: RulesRepository = {
    RulesRepository(
      key = "sonar-scala-scalastyle",
      name = "Scalastyle",
      rules = ScalastyleRules.rules.flatMap(fromTemplate)
    )
  }

  private[metadata] def fromTemplate(rule: Rule): Chain[Rule] = {
    val newRule = rule.copy(params = rule.params :+ extraParam(rule.key))
    if (rule.params.nonEmpty) {
      val template = newRule.copy(key = rule.key + "-template")
      if (!SkipTemplateInstances.contains(rule.key))
        Chain(template, newRule)
      else Chain(template)
    } else Chain(newRule)
  }

  private[metadata] def extraParam(key: String): Param =
    Param(
      name = "ruleClass",
      typ = ParamType.String,
      description = "Scalastyle's rule (checker) class name.",
      default = key
    )
}
