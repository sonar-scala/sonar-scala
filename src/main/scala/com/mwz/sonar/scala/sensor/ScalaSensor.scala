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

package com.mwz.sonar.scala
package sensor

import scala.io.Source
import scala.jdk.CollectionConverters._

import org.sonar.api.batch.sensor.Sensor
import org.sonar.api.batch.sensor.SensorContext
import org.sonar.api.batch.sensor.SensorDescriptor
import org.sonar.api.measures.{CoreMetrics => CM}
import scalariform.ScalaVersion

/** SonarQube Sensor for the Scala programming language */
final class ScalaSensor(globalConfig: GlobalConfig) extends Sensor {
  override def execute(context: SensorContext): Unit = {
    val charset = context.fileSystem().encoding.toString

    val inputFiles = context
      .fileSystem()
      .inputFiles(context.fileSystem().predicates().hasLanguage(Scala.LanguageKey))

    val scalaVersion: ScalaVersion =
      Scala.getScalaVersion(context.config())

    // Save measures if not in pr decoration mode.
    if (!globalConfig.prDecoration)
      inputFiles.asScala.foreach { inputFile =>
        // TODO: This source needs to be closed!
        val sourceCode = Source.fromFile(inputFile.uri, charset).mkString
        val tokens = Scala.tokenize(sourceCode, scalaVersion)

        context
          .newMeasure()
          .on(inputFile)
          .forMetric(CM.COMMENT_LINES)
          .withValue(Measures.countCommentLines(tokens))
          .save()

        context
          .newMeasure()
          .on(inputFile)
          .forMetric(CM.NCLOC)
          .withValue(Measures.countNonCommentLines(tokens))
          .save()

        context
          .newMeasure()
          .on(inputFile)
          .forMetric(CM.CLASSES)
          .withValue(Measures.countClasses(tokens))
          .save()

        context
          .newMeasure()
          .on(inputFile)
          .forMetric(CM.FUNCTIONS)
          .withValue(Measures.countMethods(tokens))
          .save()
      }
  }

  override def describe(descriptor: SensorDescriptor): Unit = {
    descriptor
      .onlyOnLanguage(Scala.LanguageKey)
      .name("Scala Sensor")
  }
}
