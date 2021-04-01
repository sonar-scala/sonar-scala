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

import com.mwz.sonar.scala.pr.Generators._
import org.scalacheck.ScalacheckShapeless._
import org.scalatest.Inspectors
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks
import org.sonar.api.batch.fs.internal.TestInputFileBuilder
import org.sonar.api.batch.rule.Severity
import org.sonar.api.rule.RuleKey

class GlobalIssuesSpec extends AnyFlatSpec with Matchers with ScalaCheckDrivenPropertyChecks {
  it should "add a new issue" in {
    val issues = new GlobalIssues
    val file = TestInputFileBuilder
      .create("", "test.scala")
      .build
    val issue = Issue(
      key = RuleKey.of("repo", "rule"),
      file = file,
      line = 10,
      severity = Severity.MAJOR,
      message = "test"
    )

    issues.add(issue)
    issues.allIssues shouldBe Map(file -> List(issue))
  }

  it should "return all issues" in {
    forAll { (issues: List[Issue]) =>
      whenever(issues.nonEmpty) {
        val globalIssues = new GlobalIssues
        val expected = issues.groupBy(_.file)

        issues.foreach(globalIssues.add)

        Inspectors.forAll(globalIssues.allIssues) { case (file, issues) =>
          issues should contain theSameElementsAs expected(file)
        }
      }
    }
  }
}
