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
package com.mwz.sonar.scala.sensor

import com.mwz.sonar.scala.Scala
import org.sonar.api.batch.sensor.{Sensor, SensorContext, SensorDescriptor}
import org.sonar.api.measures.{CoreMetrics => CM}
import scala.collection.JavaConverters._
import scala.io.Source

/**
 * SonarQube Sensor for the Scala programming language.
 *
 * @author mwz
 * @author ElfoLiNk
 */
class ScalaSensor(scala: Scala) extends Sensor {
  override def execute(context: SensorContext): Unit = {
    val charset = context.fileSystem().encoding.toString

    val inputFiles =
      context.fileSystem().inputFiles(context.fileSystem().predicates().hasLanguage(scala.getKey))

    inputFiles.asScala.foreach { inputFile =>
      context.newMeasure().on(inputFile).forMetric(CM.FILES).withValue(1).save()

      val sourceCode = Source.fromFile(inputFile.uri, charset).mkString
      val tokens = Scala.tokenize(sourceCode, Scala.getScalaVersion(context.config()))

      context
        .newMeasure()
        .on(inputFile)
        .forMetric(CM.COMMENT_LINES)
        .withValue(int2Integer(Measures.countCommentLines(tokens)))
        .save()
      context
        .newMeasure()
        .on(inputFile)
        .forMetric(CM.NCLOC)
        .withValue(int2Integer(Measures.countNonCommentLines(tokens)))
        .save()
      context
        .newMeasure()
        .on(inputFile)
        .forMetric(CM.CLASSES)
        .withValue(int2Integer(Measures.countClasses(tokens)))
        .save()
      context
        .newMeasure()
        .on(inputFile)
        .forMetric(CM.FUNCTIONS)
        .withValue(int2Integer(Measures.countMethods(tokens)))
        .save()
    }
  }

  override def describe(descriptor: SensorDescriptor): Unit = {
    descriptor
      .onlyOnLanguage(Scala.Key)
      .name("Scala Sensor")
  }
}
