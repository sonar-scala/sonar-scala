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

import org.scalacheck._
import org.sonar.api.batch.fs.InputFile
import org.sonar.api.batch.fs.internal.TestInputFileBuilder
import org.sonar.api.rule.RuleKey

object Generators {
  implicit val arbInputFile: Arbitrary[InputFile] =
    Arbitrary(
      Gen
        .nonEmptyListOf(Gen.alphaNumChar)
        .map { s =>
          TestInputFileBuilder
            .create("", s"${s.mkString}.scala")
            .build()
        }
    )

  implicit val arbRuleKey: Arbitrary[RuleKey] =
    Arbitrary(
      for {
        repo <- Gen.nonEmptyListOf(Gen.alphaNumChar)
        rule <- Gen.nonEmptyListOf(Gen.alphaNumChar)
      } yield RuleKey.of(repo.mkString, rule.mkString)
    )
}
