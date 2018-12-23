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
package util
package syntax

import org.sonar.api.batch.fs.InputFile
import org.sonar.api.batch.measure.Metric
import org.sonar.api.batch.sensor.SensorContext

object SensorContext {
  implicit final class SensorContextOps(val context: SensorContext) extends AnyVal {

    /**
     * Save a new measure for the given metric.
     */
    def saveMeasure[T <: java.io.Serializable](
      file: InputFile,
      metric: Metric[T],
      value: T
    ): Unit =
      context
        .newMeasure[T]
        .on(file)
        .forMetric(metric)
        .withValue(value)
        .save()
  }
}
