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

package com.mwz.sonar.scala
package scalastyle

import org.mockito.ArgumentMatchers._
import org.mockito.Mockito._
import org.scalastyle.{FileSpec, ScalastyleConfiguration, ScalastyleChecker => Checker}
import org.scalatest.FlatSpec
import org.scalatestplus.mockito.MockitoSugar

class ScalastyleCheckerSpec extends FlatSpec with MockitoSugar {
  "ScalastyleChecker" should "checkFiles" in {
    val checker = mock[Checker[FileSpec]]
    when(checker.checkFiles(any(), any()))
      .thenReturn(List.empty)

    val config: ScalastyleConfiguration = new ScalastyleConfiguration(
      "SonarQube",
      commentFilter = true,
      List.empty
    )

    new ScalastyleChecker().checkFiles(checker, config, Seq.empty)

    verify(checker).checkFiles(any(), any())
  }
}
