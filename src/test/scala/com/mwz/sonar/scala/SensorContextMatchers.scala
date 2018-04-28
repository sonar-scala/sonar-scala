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
package com.mwz.sonar.scala

import org.scalatest.matchers.{HavePropertyMatchResult, HavePropertyMatcher}
import org.sonar.api.batch.sensor.internal.SensorContextTester

/** Custom matchers to test properties of sensor contexts */
trait SensorContextMatchers {

  /** Checks that a sensor context have an expected value for some metric */
  def metric[T <: java.io.Serializable](
    componentKey: String,
    metricKey: String,
    expectedValue: T
  ): HavePropertyMatcher[SensorContextTester, T] = (sensorContext: SensorContextTester) => {
    val measure = Option(sensorContext.measure[T](componentKey, metricKey))
    HavePropertyMatchResult(
      matches = measure.fold(false)(m => m.value() == expectedValue),
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
  ): HavePropertyMatcher[SensorContextTester, Int] = (sensorContext: SensorContextTester) => {
    val hits = Option(sensorContext.lineHits(fileKey, lineNum))
    HavePropertyMatchResult(
      matches = hits.fold(false)(_ == expectedValue),
      propertyName = "measure",
      expectedValue = expectedValue,
      actualValue = None.orNull.asInstanceOf[Int]
    )
  }
}
