package com.sagacify.sonar.scala

import com.buransky.plugins.scoverage.measure.ScalaMetrics
import com.buransky.plugins.scoverage.sensor.ScoverageSensor
import com.buransky.plugins.scoverage.widget.ScoverageWidget
import com.ncredinburgh.sonar.scalastyle.{ScalastyleQualityProfile, ScalastyleRepository, ScalastyleSensor}
import org.sonar.api.Plugin
import org.sonar.api.config.Configuration
import org.sonar.api.resources.AbstractLanguage

import scalariform.lexer.{ScalaLexer, Token}

/**
 * Defines Scala as a language for SonarQube.
 */
class Scala(s: Configuration) extends AbstractLanguage("scala", "Scala") {

  override def getFileSuffixes: Array[String] = Array("scala")

}

object Scala {

  def tokenize(sourceCode: String, scalaVersion: String): List[Token] =
    ScalaLexer.createRawLexer(sourceCode, false, scalaVersion).toList

}

/**
 * Plugin entry point.
 */
class ScalaPlugin extends Plugin {

  override def define(context: Plugin.Context) = {
    context.addExtensions(
            classOf[Scala],
            classOf[ScalaSensor],
            classOf[ScalastyleRepository],
            classOf[ScalastyleQualityProfile],
            classOf[ScalastyleSensor],
            classOf[ScalaMetrics],
            classOf[ScoverageSensor],
            classOf[ScoverageWidget]
    )
  }
}
