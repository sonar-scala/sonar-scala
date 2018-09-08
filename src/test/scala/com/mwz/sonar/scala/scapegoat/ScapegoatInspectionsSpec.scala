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
package com.mwz.sonar.scala.scapegoat

import org.scalatest.{FlatSpec, Inspectors, Matchers}
import org.sonar.api.batch.rule.Severity

/** Tests the generated scapegoat inspections file */
class ScapegoatInspectionsSpec extends FlatSpec with Inspectors with Matchers {
  "The Scapegoat Inspections object" should "define all scapegoat inspections" in {
    ScapegoatInspections.AllInspections.length shouldBe 126
    ScapegoatInspections.AllInspections.distinct.length shouldBe 126
  }

  it should "not define the blacklisted scapegoat inspections" in {
    ScapegoatInspections.AllInspections.map(inspection => inspection.id) should contain noneOf (
      "com.sksamuel.scapegoat.inspections.collections.FilterDotSizeComparison",
      "com.sksamuel.scapegoat.inspections.collections.ListTail"
    )
  }

  it should "have all inspections with non-empty properties" in {
    forEvery(ScapegoatInspections.AllInspections) { inspection =>
      inspection.id should not be empty
      inspection.name should not be empty
      inspection.description should not be empty
    }
  }

  it should "have all inspections' ids start with com.sksamuel.scapegoat.inspections" in {
    forEvery(ScapegoatInspections.AllInspections) { inspection =>
      inspection.id should startWith("com.sksamuel.scapegoat.inspections.")
    }
  }

  it should "correctly define the AnyUse inspection" in {
    val anyUseInspection = ScapegoatInspection(
      id = "com.sksamuel.scapegoat.inspections.AnyUse",
      name = "AnyUse",
      description = "No Explanation",
      defaultLevel = Level.Info
    )

    ScapegoatInspections.AllInspections should contain(anyUseInspection)
  }

  "The Scapegoat Inspection Levels" should "correctly map to SonarQube severities" in {
    Level.Info.toRuleSeverity shouldBe Severity.INFO
    Level.Warning.toRuleSeverity shouldBe Severity.MINOR
    Level.Error.toRuleSeverity shouldBe Severity.MAJOR
  }
}
