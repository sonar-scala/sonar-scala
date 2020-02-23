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

package com.mwz.sonar.scala.metadata.scapegoat

import com.sksamuel.scapegoat.inspections.{AnyUse, EmptyCaseClass}
import com.sksamuel.scapegoat.inspections.string.ArraysInFormat
import org.scalatest.{FlatSpec, LoneElement, Matchers}

import scala.meta._

/** Tests the correct behavior of the Scapegoat Inspections Generator SBT Task */
class ScapegoatInspectionsGeneratorSpec extends FlatSpec with LoneElement with Matchers {
  "stringifyInspections" should "correctly format a one scapegoat inspection" in {
    val expected =
      """ScapegoatInspection(
        |  id = "com.sksamuel.scapegoat.inspections.AnyUse",
        |  name = "AnyUse",
        |  description = None,
        |  defaultLevel = Level.Info
        |)""".stripMargin

    val result =
      ScapegoatInspectionsGenerator
        .stringifyInspections(
          List("com.sksamuel.scapegoat.inspections.AnyUse" -> new AnyUse())
        )
        .loneElement

    result shouldBe expected
  }

  "stringifyInspections" should "correctly format a list of scapegoat inspections" in {
    val expected =
      List(
        """ScapegoatInspection(
          |  id = "com.sksamuel.scapegoat.inspections.AnyUse",
          |  name = "AnyUse",
          |  description = None,
          |  defaultLevel = Level.Info
          |)""".stripMargin,
        """ScapegoatInspection(
          |  id = "com.sksamuel.scapegoat.inspections.EmptyCaseClass",
          |  name = "Empty case class",
          |  description = Some("Empty case class can be rewritten as a case object"),
          |  defaultLevel = Level.Info
          |)""".stripMargin,
        """ScapegoatInspection(
          |  id = "com.sksamuel.scapegoat.inspections.string.ArraysInFormat",
          |  name = "Array passed to String.format",
          |  description = None,
          |  defaultLevel = Level.Error
          |)""".stripMargin
      )

    val result =
      ScapegoatInspectionsGenerator
        .stringifyInspections(
          List(
            "com.sksamuel.scapegoat.inspections.AnyUse" -> new AnyUse(),
            "com.sksamuel.scapegoat.inspections.EmptyCaseClass" -> new EmptyCaseClass(),
            "com.sksamuel.scapegoat.inspections.string.ArraysInFormat" -> new ArraysInFormat()
          )
        )

    result shouldBe expected
  }

  "fillTemplate" should "succesfuly fill the code template with an stringyfied list of inspections" in {
    val expected =
      """private[metadata] object ScapegoatInspections {
        |  val AllInspections: List[ScapegoatInspection] = List(
        |    ScapegoatInspection(
        |      id = "com.sksamuel.scapegoat.inspections.AnyUse",
        |      name = "AnyUse",
        |      description = None,
        |      defaultLevel = Level.Info
        |    ),
        |    ScapegoatInspection(
        |      id = "com.sksamuel.scapegoat.inspections.EmptyCaseClass",
        |      name = "Empty case class",
        |      description = Some("Empty case class can be rewritten as a case object"),
        |      defaultLevel = Level.Info
        |    ),
        |    ScapegoatInspection(
        |      id = "com.sksamuel.scapegoat.inspections.string.ArraysInFormat",
        |      name = "Array passed to String.format",
        |      description = None,
        |      defaultLevel = Level.Error
        |    )
        |  )
        |}""".stripMargin

    val stringifiedScapegoatInspections =
      List(
        """ScapegoatInspection(
          |  id = "com.sksamuel.scapegoat.inspections.AnyUse",
          |  name = "AnyUse",
          |  description = None,
          |  defaultLevel = Level.Info
          |)""".stripMargin,
        """ScapegoatInspection(
          |  id = "com.sksamuel.scapegoat.inspections.EmptyCaseClass",
          |  name = "Empty case class",
          |  description = Some("Empty case class can be rewritten as a case object"),
          |  defaultLevel = Level.Info
          |)""".stripMargin,
        """ScapegoatInspection(
          |  id = "com.sksamuel.scapegoat.inspections.string.ArraysInFormat",
          |  name = "Array passed to String.format",
          |  description = None,
          |  defaultLevel = Level.Error
          |)""".stripMargin
      )

    val template =
      """private[metadata] object ScapegoatInspections {
        |  val AllInspections: List[ScapegoatInspection] = ???
        |}""".stripMargin

    val result =
      ScapegoatInspectionsGenerator.fillTemplate(template.parse[Source].get, stringifiedScapegoatInspections)

    result.structure shouldBe expected.parse[Source].get.structure
  }
}
