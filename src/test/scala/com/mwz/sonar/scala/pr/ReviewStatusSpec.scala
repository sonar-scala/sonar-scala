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

package com.mwz.sonar.scala.pr

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class ReviewStatusSpec extends AnyFlatSpec with Matchers {
  it should "infer the description from a review status" in {
    ReviewStatus.description(ReviewStatus(blocker = 0, critical = 0)) shouldBe "no critical or blocker issues"
    ReviewStatus.description(ReviewStatus(blocker = 1, critical = 0)) shouldBe "1 blocker"
    ReviewStatus.description(ReviewStatus(blocker = 2, critical = 0)) shouldBe "2 blockers"
    ReviewStatus.description(ReviewStatus(blocker = 0, critical = 1)) shouldBe "1 critical issue"
    ReviewStatus.description(ReviewStatus(blocker = 0, critical = 2)) shouldBe "2 critical issues"
    ReviewStatus.description(
      ReviewStatus(blocker = 1, critical = 1)
    ) shouldBe "1 blocker and 1 critical issue"
    ReviewStatus.description(
      ReviewStatus(blocker = 2, critical = 2)
    ) shouldBe "2 blockers and 2 critical issues"
  }
}
