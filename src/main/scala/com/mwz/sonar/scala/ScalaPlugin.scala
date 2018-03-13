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

import com.buransky.plugins.scoverage.measure.ScalaMetrics
import com.buransky.plugins.scoverage.sensor.ScoverageSensor
import com.ncredinburgh.sonar.scalastyle.{ ScalastyleQualityProfile, ScalastyleRepository, ScalastyleSensor }
import org.sonar.api.Plugin
import org.sonar.api.config.Configuration
import org.sonar.api.resources.AbstractLanguage

import scalariform.lexer.{ ScalaLexer, Token }

/**
 * Defines Scala as a language for SonarQube.
 */
class Scala(settings: Configuration) extends AbstractLanguage(Scala.Key, Scala.Name) {
  override def getFileSuffixes: Array[String] = {
    val suffixes = settings.getStringArray(Scala.FileSuffixesKey).toList
    val filtered = suffixes.filter(_.trim.nonEmpty)
    Some(filtered).filter(_.nonEmpty).getOrElse(Scala.DefaultFileSuffixes).toArray
  }
}

object Scala {
  val Key = "scala"
  val Name = "Scala"
  val FileSuffixesKey = "sonar.scala.file.suffixes"
  val DefaultFileSuffixes = List(".scala")

  def tokenize(sourceCode: String, scalaVersion: String): List[Token] =
    ScalaLexer.createRawLexer(sourceCode, forgiveErrors = false, scalaVersion).toList
}

/**
 * Plugin entry point.
 */
class ScalaPlugin extends Plugin {
  override def define(context: Plugin.Context): Unit = {
    context.addExtensions(
      classOf[Scala],
      classOf[ScalaSensor],
      classOf[ScalastyleRepository],
      classOf[ScalastyleQualityProfile],
      classOf[ScalastyleSensor],
      classOf[ScalaMetrics],
      classOf[ScoverageSensor])
  }
}
