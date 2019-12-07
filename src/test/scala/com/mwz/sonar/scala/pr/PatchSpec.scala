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

import org.scalatest.{EitherValues, FlatSpec, Matchers}

class PatchSpec extends FlatSpec with Matchers with EitherValues {
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

  it should "parse successfully a patch with deletions only" in {
    val expected: Map[FileLine, PatchLine] =
      List(
        List(26 -> 1, 27 -> 2, 28 -> 3, 29 -> 6, 30 -> 7, 31 -> 8),
        List(43 -> 10, 44 -> 11, 45 -> 12, 46 -> 15, 47 -> 16, 48 -> 20, 49 -> 21, 50 -> 22)
      ).flatten.map {
        case (k, v) =>
          FileLine(k) -> PatchLine(v)
      }.toMap

    Patch.parse(patch("patches/deletions.patch")).right.value shouldBe expected
  }

  it should "parse successfully a patch with additions and deletions" in {}
}
