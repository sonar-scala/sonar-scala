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
import org.scalastyle.{ConfigurationChecker, ErrorLevel}
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{FlatSpec, LoneElement, Matchers, OptionValues}
import org.sonar.api.batch.fs.InputFile
import org.sonar.api.batch.rule.{ActiveRule, Severity}
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

  it should "convert a rule to configuration checker" in {
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
}
