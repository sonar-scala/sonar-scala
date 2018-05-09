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

import java.nio.file.{Path, Paths}

import com.mwz.sonar.scala.scoverage.{ScoverageMetrics, ScoverageSensor}
import com.mwz.sonar.scala.sensor.ScalaSensor
import com.mwz.sonar.scala.util.JavaOptionals._
import com.ncredinburgh.sonar.scalastyle.{ScalastyleQualityProfile, ScalastyleRepository, ScalastyleSensor}
import org.sonar.api.Plugin
import org.sonar.api.config.Configuration
import org.sonar.api.resources.AbstractLanguage
import org.sonar.api.utils.log.Loggers
import scalariform.{ScalaVersion, ScalaVersions}
import scalariform.lexer.{ScalaLexer, Token}
import scalariform.utils.Utils._

/** Defines Scala as a language for SonarQube */
class Scala(settings: Configuration) extends AbstractLanguage(Scala.Key, Scala.Name) {
  override def getFileSuffixes: Array[String] = {
    val suffixes = settings.getStringArray(Scala.FileSuffixesPropertyKey)
    val filtered = suffixes.filter(_.trim.nonEmpty)
    if (filtered.nonEmpty) filtered
    else Scala.DefaultFileSuffixes
  }
}

object Scala {
  val Key = "scala"
  val Name = "Scala"
  private val FileSuffixesPropertyKey = "sonar.scala.file.suffixes"
  private val DefaultFileSuffixes = Array(".scala")
  private val ScalaVersionPropertyKey = "sonar.scala.version"
  private val DefaultScalaVersion = ScalaVersions.Scala_2_11
  private val ScalaVersionPattern = """(\d+)\.(\d+)(?:\..+)?""".r
  private val SourcesPropertyKey = "sonar.sources"
  private val DefaultSourcesFolder = "src/main/scala"

  private val logger = Loggers.get(classOf[Scala])

  def getScalaVersion(settings: Configuration): ScalaVersion = {
    def parseVersion(s: String): Option[ScalaVersion] = s match {
      case ScalaVersionPattern(major, minor) =>
        for {
          major <- major.toIntOpt
          minor <- minor.toIntOpt
        } yield ScalaVersion(major, minor)
      case _ ⇒
        None
    }

    val scalaVersion = settings
      .get(ScalaVersionPropertyKey)
      .toOption
      .flatMap(parseVersion)
      .getOrElse(DefaultScalaVersion)

    // log a warning if using the default scala version
    if (scalaVersion == DefaultScalaVersion)
      logger.warn(
        s"[sonar-scala] The '$ScalaVersionPropertyKey' is not properly set or is missing, using the default value: 's$DefaultScalaVersion'"
      )

    scalaVersion
  }

  // even if the 'sonar.sources' property is mandatory,
  // we add a default value to ensure a safe access to it
  def getSourcesPaths(settings: Configuration): List[Path] =
    settings
      .get(SourcesPropertyKey)
      .toOption
      .filter(_.nonEmpty)
      .getOrElse(DefaultSourcesFolder)
      .split(',') // scalastyle:ignore LiteralArguments
      .map(p => Paths.get(p.trim))
      .toList

  def tokenize(sourceCode: String, settings: Configuration): List[Token] =
    ScalaLexer
      .createRawLexer(sourceCode, forgiveErrors = false, getScalaVersion(settings).toString)
      .toList
}

/** Plugin entry point */
class ScalaPlugin extends Plugin {
  override def define(context: Plugin.Context): Unit = {
    context.addExtensions(
      classOf[Scala],
      classOf[ScalaSensor],
      classOf[ScalastyleRepository],
      classOf[ScalastyleQualityProfile],
      classOf[ScalastyleSensor],
      classOf[ScoverageMetrics],
      classOf[ScoverageSensor]
    )
  }
}
