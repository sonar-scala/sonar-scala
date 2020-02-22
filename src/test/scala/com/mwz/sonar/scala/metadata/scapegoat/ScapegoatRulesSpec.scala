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
package scapegoat

import cats.data.Chain
import org.scalatest.Inspectors
import org.scalatest.LoneElement
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class ScapegoatRulesSpec extends AnyFlatSpec with Matchers with Inspectors with LoneElement {
  val rules = ScapegoatRules.rules.toChain.toVector

  it should "define all scapegoat rules" in {
    rules.size shouldBe 116
    rules.distinct.size shouldBe 116
  }

  it should "not define the blacklisted scapegoat rules" in {
    rules.map(_.key) should contain noneOf (
      "com.sksamuel.scapegoat.inspections.collections.FilterDotSizeComparison",
      "com.sksamuel.scapegoat.inspections.collections.ListTail"
    )
  }

  it should "have all rules with non-empty properties" in {
    forEvery(rules) { rule =>
      rule.key should not be empty
      rule.name should not be empty
    }
  }

  it should "have all rule ids start with com.sksamuel.scapegoat.inspections" in {
    forEvery(rules)(_.key should startWith("com.sksamuel.scapegoat.inspections."))
  }

  it should "convert Scapegoat inspection to a Rule" in {
    val inspection1 = ScapegoatInspection(
      id = "com.sksamuel.scapegoat.inspections.exception.CatchNpe",
      name = "Catching NPE",
      description = None,
      defaultLevel = Level.Error
    )

    val inspection2 = ScapegoatInspection(
      id = "com.sksamuel.scapegoat.inspections.equality.ComparisonWithSelf",
      name = "Comparison with self",
      description = Some("Comparison with self will always yield true"),
      defaultLevel = Level.Warning
    )

    val expected1 = Rule(
      key = "com.sksamuel.scapegoat.inspections.exception.CatchNpe",
      name = "Catching NPE",
      description = "No description",
      severity = Severity.Major,
      template = false,
      params = Chain.empty
    )

    val expected2 = Rule(
      key = "com.sksamuel.scapegoat.inspections.equality.ComparisonWithSelf",
      name = "Comparison with self",
      description = "Comparison with self will always yield true",
      severity = Severity.Minor,
      template = false,
      params = Chain.empty
    )

    ScapegoatRules.toRule(inspection1) shouldBe expected1
    ScapegoatRules.toRule(inspection2) shouldBe expected2
  }

  it should "convert Scapegoat inspection level to SonarQube Severity" in {
    ScapegoatRules.toSeverity(Level.Info) shouldBe Severity.Info
    ScapegoatRules.toSeverity(Level.Warning) shouldBe Severity.Minor
    ScapegoatRules.toSeverity(Level.Error) shouldBe Severity.Major
  }
}
