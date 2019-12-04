/*
 * Copyright (C) 2018-2019  All sonar-scala contributors
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

package com.mwz.sonar.scala.pr

import scala.io.Source

import org.scalatest.EitherValues
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class PatchSpec extends AnyFlatSpec with Matchers with EitherValues {
  def patch(path: String): String =
    Source
      .fromResource(path)
      .getLines()
      .mkString("\n")

  it should "parse successfully a patch with additions only" in {
    val expected: Map[FileLine, PatchLine] =
      (69 to 84).zipWithIndex.map {
        case (fileLine, index) =>
          (FileLine(fileLine), PatchLine(index + 1))
      }.toMap

    Patch.parse(patch("patches/additions.patch")).right.value shouldBe expected
  }

  it should "parse successfully a patch with deletions only" in {}

  it should "parse successfully a patch with additions and deletions" in {}
}
