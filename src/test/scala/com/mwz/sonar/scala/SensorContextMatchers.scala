/*
 * Copyright (C) 2018-2022  All sonar-scala contributors
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

import org.scalactic.Equality
import org.scalatest.matchers.HavePropertyMatchResult
import org.scalatest.matchers.HavePropertyMatcher
import org.scalatest.matchers.should.Matchers
import org.sonar.api.batch.sensor.internal.SensorContextTester
import org.sonar.api.batch.sensor.issue.Issue
import org.sonar.api.batch.sensor.issue.IssueLocation

/** Custom matchers to test properties of sensor contexts */
trait SensorContextMatchers extends Matchers {

  /** Checks that a sensor context have an expected value for some metric */
  def metric[T <: java.io.Serializable](
    componentKey: String,
    metricKey: String,
    expectedValue: T
  ): HavePropertyMatcher[SensorContextTester, T] =
    (sensorContext: SensorContextTester) => {
      val measure = Option(sensorContext.measure[T](componentKey, metricKey))
      HavePropertyMatchResult(
        matches = measure.fold(false)(m => m.value === expectedValue),
        propertyName = "measure",
        expectedValue = expectedValue,
        actualValue = None.orNull.asInstanceOf[T]
      )
    }

  /** Checks that a sensor context have an expected value for some line hits */
  def lineHits(
    fileKey: String,
    lineNum: Int,
    expectedValue: Int
  ): HavePropertyMatcher[SensorContextTester, Int] =
    (sensorContext: SensorContextTester) => {
      val hits = Option(sensorContext.lineHits(fileKey, lineNum))
      HavePropertyMatchResult(
        matches = hits.fold(false)(_ === expectedValue),
        propertyName = "measure",
        expectedValue = expectedValue,
        actualValue = None.orNull.asInstanceOf[Int]
      )
    }

  /** Custom equality for comparing issueLocations */
  implicit val issueLocationEq =
    new Equality[IssueLocation] {
      override def areEqual(a: IssueLocation, b: Any): Boolean =
        b match {
          case b: IssueLocation =>
            a.inputComponent === b.inputComponent &&
              a.message === b.message &&
              a.textRange === b.textRange
          case _ => false
        }
    }

  /** Custom equality for comparing issues */
  implicit val issueEq =
    new Equality[Issue] {
      override def areEqual(a: Issue, b: Any): Boolean =
        b match {
          case b: Issue =>
            a.ruleKey === b.ruleKey &&
              a.primaryLocation === b.primaryLocation &&
              a.overriddenSeverity === b.overriddenSeverity
          case _ => false
        }
    }
}
