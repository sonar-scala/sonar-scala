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
package sensor

import java.nio.file.Paths

import org.scalatest.LoneElement
import org.sonar.api.batch.fs.internal.TestInputFileBuilder
import org.sonar.api.batch.sensor.internal.{DefaultSensorDescriptor, SensorContextTester}
import org.sonar.api.config.internal.MapSettings
import org.sonar.api.measures.{CoreMetrics => CM}
import org.scalatest.flatspec.AnyFlatSpec

/** Tests the Scala Sensor */
class ScalaSensorSpec extends AnyFlatSpec with SensorContextMatchers with LoneElement {
  val globalConfig = new GlobalConfig(new MapSettings().asConfig)
  val sensor = new ScalaSensor(globalConfig)
  behavior of "A ScalaSensor"

  it should "correctly set descriptor" in {
    val descriptor = new DefaultSensorDescriptor
    sensor.describe(descriptor)

    descriptor.name() shouldBe "Scala Sensor"
    descriptor.languages().loneElement shouldBe "scala"
  }

  it should "correctly measure ScalaFile1" in {
    val context = SensorContextTester.create(Paths.get("./src/test/resources"))
    val inputFile =
      TestInputFileBuilder.create("", "src/test/resources/ScalaFile1.scala").setLanguage("scala").build()
    context.fileSystem().add(inputFile)
    sensor.execute(context)

    val componentKey = inputFile.key()

    context should have(metric[java.lang.Integer](componentKey, CM.COMMENT_LINES_KEY, 0))
    context should have(metric[java.lang.Integer](componentKey, CM.CLASSES_KEY, 1))
    context should have(metric[java.lang.Integer](componentKey, CM.FUNCTIONS_KEY, 1))
    context should have(metric[java.lang.Integer](componentKey, CM.NCLOC_KEY, 6))
  }

  it should "correctly measure ScalaFile2" in {
    val context = SensorContextTester.create(Paths.get("./src/test/resources"))
    val inputFile =
      TestInputFileBuilder.create("", "src/test/resources/ScalaFile2.scala").setLanguage("scala").build()
    context.fileSystem().add(inputFile)
    sensor.execute(context)

    val componentKey = inputFile.key()

    context should have(metric[java.lang.Integer](componentKey, CM.COMMENT_LINES_KEY, 1))
    context should have(metric[java.lang.Integer](componentKey, CM.CLASSES_KEY, 2))
    context should have(metric[java.lang.Integer](componentKey, CM.FUNCTIONS_KEY, 2))
  }
}
