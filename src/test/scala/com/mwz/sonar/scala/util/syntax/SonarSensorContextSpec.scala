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
package util
package syntax

import java.nio.file.Paths

import com.mwz.sonar.scala.util.syntax.SonarSensorContext._
import org.scalatest.{FlatSpec, Matchers}
import org.sonar.api.batch.fs.internal.TestInputFileBuilder
import org.sonar.api.batch.sensor.internal.SensorContextTester
import org.sonar.api.measures.CoreMetrics

class SonarSensorContextSpec extends FlatSpec with Matchers with SensorContextMatchers {
  it should "save a measure for a given input file" in {
    val ctx = SensorContextTester.create(Paths.get("./"))
    val testFile = TestInputFileBuilder
      .create("", "TestFile.scala")
      .build()

    ctx.saveMeasure[Integer](testFile, CoreMetrics.TESTS, 5)
    ctx.saveMeasure[java.lang.Long](testFile, CoreMetrics.TEST_EXECUTION_TIME, 124l)

    ctx should have(metric[Integer](testFile.key, "tests", 5))
    ctx should have(metric[java.lang.Long](testFile.key, "test_execution_time", 124l))
  }
}
