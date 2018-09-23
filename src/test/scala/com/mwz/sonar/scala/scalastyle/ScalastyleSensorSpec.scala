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
package scalastyle

import java.util

import com.mwz.sonar.scala.util.PathUtils.cwd
import org.scalactic._
import org.scalastyle.scalariform.EmptyClassChecker
import org.scalastyle.{ConfigurationChecker, ErrorLevel, FileSpec, StyleError}
import org.scalatest._
import org.scalatest.mockito.MockitoSugar
import org.sonar.api.batch.fs.InputFile
import org.sonar.api.batch.fs.internal.TestInputFileBuilder
import org.sonar.api.batch.rule.internal.ActiveRulesBuilder
import org.sonar.api.batch.rule.{ActiveRule, ActiveRules, Severity}
import org.sonar.api.batch.sensor.internal.{DefaultSensorDescriptor, SensorContextTester}
import org.sonar.api.config.internal.MapSettings
import org.sonar.api.profiles.RulesProfile
import org.sonar.api.rule.RuleKey

import scala.collection.JavaConverters._

class ScalastyleSensorSpec
    extends FlatSpec
    with Matchers
    with LoneElement
    with OptionValues
    with MockitoSugar {

  trait Ctx {
    val context = SensorContextTester.create(cwd)
    val rulesProfile = mock[RulesProfile]
    val scalastyleSensor = new ScalastyleSensor(rulesProfile)
    val descriptor = new DefaultSensorDescriptor
  }

  implicit val fileSpecEq: Equality[FileSpec] =
    (self: FileSpec, other: Any) =>
      other match {
        case fileSpec: FileSpec => fileSpec.name === self.name
        case _                  => false
    }

  it should "read the 'disable' config property" in {
    val context = SensorContextTester.create(cwd)
    ScalastyleSensor.shouldEnableSensor(context.config) shouldBe true

    val context2 = SensorContextTester.create(cwd)
    context2.setSettings(new MapSettings().setProperty("sonar.scala.scalastyle.disable", "maybe"))
    ScalastyleSensor.shouldEnableSensor(context2.config) shouldBe true

    val context3 = SensorContextTester.create(cwd)
    context3.setSettings(new MapSettings().setProperty("sonar.scala.scalastyle.disable", "true"))
    ScalastyleSensor.shouldEnableSensor(context3.config) shouldBe false
  }

  it should "execute the sensor if the 'disable' flag wasn't set" in new Ctx {
    scalastyleSensor.describe(descriptor)
    descriptor.configurationPredicate.test(context.config) shouldBe true
  }

  it should "respect the 'disable' config property and skip execution if set to true" in new Ctx {
    context.setSettings(new MapSettings().setProperty("sonar.scala.scalastyle.disable", "true"))
    scalastyleSensor.describe(descriptor)
    descriptor.configurationPredicate.test(context.config) shouldBe false
  }

  it should "correctly set descriptor" in new Ctx {
    scalastyleSensor.describe(descriptor)

    descriptor should not be 'global
    descriptor.name shouldBe "Scalastyle Sensor"
    descriptor.`type` shouldBe InputFile.Type.MAIN
    descriptor.languages.loneElement shouldBe "scala"
    descriptor.ruleRepositories.loneElement shouldBe "sonar-scala-scalastyle"
  }

  it should "convert a rule to a configuration checker" in {
    val activeRule = new ActiveRule {
      def ruleKey(): RuleKey =
        RuleKey.of("sonar-scala-scalastyle", "rule1")
      def severity(): String = Severity.MAJOR.toString
      def language(): String = ???
      def param(key: String): String = ???
      def params(): util.Map[String, String] =
        Map(
          "ruleClass" -> "hello.class.name",
          "param1" -> "value1",
          "param2" -> "value2"
        ).asJava
      def internalKey(): String = ???
      def templateRuleKey(): String = ???
    }

    val expected = ConfigurationChecker(
      "hello.class.name",
      ErrorLevel,
      enabled = true,
      Map(
        "ruleClass" -> "hello.class.name",
        "param1" -> "value1",
        "param2" -> "value2"
      ),
      customMessage = None,
      Some("rule1")
    )

    ScalastyleSensor.ruleToConfigurationChecker(activeRule).value shouldBe expected
  }

  it should "fail to convert an invalid rule to a configuration checker" in {
    val activeRule = new ActiveRule {
      def ruleKey(): RuleKey = ???
      def severity(): String = ???
      def language(): String = ???
      def param(key: String): String = ???
      def params(): util.Map[String, String] =
        Map.empty[String, String].asJava
      def internalKey(): String = ???
      def templateRuleKey(): String = ???
    }

    ScalastyleSensor.ruleToConfigurationChecker(activeRule) shouldBe empty
  }

  it should "get a list of files for analysis" in new Ctx {
    val testFileA = TestInputFileBuilder
      .create("", "TestFile.scala")
      .setLanguage("scala")
      .setType(InputFile.Type.MAIN)
      .build()
    val testFileB = TestInputFileBuilder
      .create("", "TestFileSpec.scala")
      .setLanguage("scala")
      .setType(InputFile.Type.TEST)
      .build()
    val testFileC = TestInputFileBuilder
      .create("", "TestFile.java")
      .setLanguage("java")
      .setType(InputFile.Type.MAIN)
      .build()

    context.fileSystem().add(testFileA)
    context.fileSystem().add(testFileB)
    context.fileSystem().add(testFileC)

    val expected = Seq(
      new FileSpec {
        def name: String = cwd.resolve("TestFile.scala").toString
      }
    )
    ScalastyleSensor.fileSpecs(context) should contain allElementsOf expected
  }

  it should "look up a rule from a style error" in new Ctx {
    val activeRules: ActiveRules =
      new ActiveRulesBuilder()
        .create(RuleKey.of("sonar-scala-scalastyle", "rule1"))
        .setInternalKey("rule1")
        .setSeverity(ErrorLevel.toString)
        .setParam("param1", "value1")
        .activate()
        .build()

    val fileSpec: FileSpec = new FileSpec {
      def name: String = cwd.resolve("TestFile.scala").toString
    }

    val styleError = StyleError(
      fileSpec,
      (new EmptyClassChecker).getClass,
      "rule1",
      ErrorLevel,
      List.empty,
      lineNumber = None,
      column = None,
      customMessage = None
    )

    context.setActiveRules(activeRules)

    val result: ActiveRule = ScalastyleSensor.ruleFromStyleError(context, styleError).value
    result.language === "scala"
    result.internalKey === "rule1"
    result.severity === ErrorLevel.toString
    result.params.asScala === Map("param1" -> "value1")
  }
}
