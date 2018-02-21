package com.sagacify.sonar.scala

import com.buransky.plugins.scoverage.measure.ScalaMetrics
import com.buransky.plugins.scoverage.sensor.ScoverageSensor
import com.ncredinburgh.sonar.scalastyle.{ScalastyleQualityProfile, ScalastyleRepository, ScalastyleSensor}
import org.sonar.api.Plugin
import org.sonar.api.config.Configuration
import org.sonar.api.resources.AbstractLanguage

import scalariform.lexer.{ScalaLexer, Token}

/**
 * Defines Scala as a language for SonarQube.
 */
class Scala(settings: Configuration) extends AbstractLanguage(Scala.KEY, Scala.Name) {
  override def getFileSuffixes: Array[String] = {
    val suffixes = settings.getStringArray(Scala.FileSuffixesKey).toList
    val filtered = suffixes.filter(_.trim.nonEmpty)
    Some(filtered).filter(_.nonEmpty).getOrElse(Scala.DefaultFileSuffixes).toArray
  }
}

object Scala {
  val KEY = "scala"
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
      classOf[ScoverageSensor]
    )
  }
}
