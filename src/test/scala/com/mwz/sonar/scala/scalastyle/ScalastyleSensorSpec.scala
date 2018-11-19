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

import java.nio.charset.Charset
import java.nio.file.Paths
import java.util

import com.mwz.sonar.scala.util.PathUtils.cwd
import org.scalactic._
import org.scalastyle.scalariform.EmptyClassChecker
import org.scalastyle.{
  ConfigurationChecker,
  ErrorLevel,
  FileSpec,
  InfoLevel,
  Message,
  ScalastyleConfiguration,
  StyleError,
  WarningLevel,
  ScalastyleChecker => Checker
}
import org.scalatest._
import org.scalatest.mockito.MockitoSugar
import org.sonar.api.batch.fs.InputFile
import org.sonar.api.batch.fs.internal.{
  DefaultFileSystem,
  DefaultTextPointer,
  DefaultTextRange,
  TestInputFileBuilder
}
import org.sonar.api.batch.rule.internal.ActiveRulesBuilder
import org.sonar.api.batch.rule.{ActiveRule, ActiveRules, Severity}
import org.sonar.api.batch.sensor.internal.{DefaultSensorDescriptor, SensorContextTester}
import org.sonar.api.batch.sensor.issue.internal.DefaultIssue
import org.sonar.api.config.internal.MapSettings
import org.sonar.api.profiles.RulesProfile
import org.sonar.api.rule.RuleKey

import scala.collection.JavaConverters._

class ScalastyleSensorSpec
    extends FlatSpec
    with Matchers
    with SensorContextMatchers
    with LoneElement
    with OptionValues
    with MockitoSugar {

  trait Ctx {
    val context = SensorContextTester.create(Paths.get("./"))
    val rulesProfile = mock[RulesProfile]
    val scalastyleChecker = new ScalastyleCheckerAPI {
      private[scalastyle] def checkFiles(
        checker: Checker[FileSpec],
        configuration: ScalastyleConfiguration,
        files: scala.Seq[FileSpec]
      ): List[Message[FileSpec]] = List.empty
    }

    val scalastyleSensor = new ScalastyleSensor(rulesProfile, scalastyleChecker)
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

  it should "convert rule severity to inspection level" in {
    ScalastyleSensor.severityToLevel(Severity.INFO) shouldBe InfoLevel
    ScalastyleSensor.severityToLevel(Severity.MINOR) shouldBe WarningLevel
    ScalastyleSensor.severityToLevel(Severity.MAJOR) shouldBe ErrorLevel
    ScalastyleSensor.severityToLevel(Severity.CRITICAL) shouldBe ErrorLevel
    ScalastyleSensor.severityToLevel(Severity.BLOCKER) shouldBe ErrorLevel
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

  it should "not open any issues if there are no active rules" in new Ctx {
    scalastyleSensor.execute(context)
    context.allIssues shouldBe empty
  }

  it should "not open any issues if there are no style errors reported" in new Ctx {
    val activeRules: ActiveRules =
      new ActiveRulesBuilder()
        .create(RuleKey.of("sonar-scala-scalastyle", "rule1"))
        .setInternalKey("rule1")
        .setSeverity(Severity.MAJOR.toString)
        .setParam("param1", "value1")
        .activate()
        .build()

    context.setActiveRules(activeRules)
    scalastyleSensor.execute(context)

    context.allIssues shouldBe empty
  }

  it should "open an issue for each style error" in new Ctx {
    val fileSpec: FileSpec = new FileSpec {
      def name: String = Paths.get("./").resolve("TestFile.scala").toString
    }

    val styleError = StyleError(
      fileSpec,
      (new EmptyClassChecker).getClass,
      "org.scalastyle.scalariform.EmptyClassChecker",
      ErrorLevel,
      List.empty,
      lineNumber = Some(1),
      column = Some(5),
      customMessage = None
    )

    val checker = new ScalastyleCheckerAPI {
      private[scalastyle] def checkFiles(
        checker: Checker[FileSpec],
        configuration: ScalastyleConfiguration,
        files: scala.Seq[FileSpec]
      ): List[Message[FileSpec]] = List(styleError)
    }

    val testFile = TestInputFileBuilder
      .create("", "TestFile.scala")
      .setLanguage("scala")
      .setType(InputFile.Type.MAIN)
      .setLines(2)
      .setOriginalLineStartOffsets(Array(0, 16))
      .setOriginalLineEndOffsets(Array(15, 30))
      .build()

    val ruleKey = RuleKey.of("sonar-scala-scalastyle", "org.scalastyle.scalariform.EmptyClassChecker")

    val activeRules: ActiveRules =
      new ActiveRulesBuilder()
        .create(ruleKey)
        .setInternalKey("org.scalastyle.scalariform.EmptyClassChecker")
        .setSeverity(Severity.MAJOR.toString)
        .setParam("ruleClass", "org.scalastyle.scalariform.EmptyClassChecker")
        .activate()
        .build()

    val issue = new DefaultIssue().forRule(ruleKey)
    val expected = Seq(
      issue.at(
        issue
          .newLocation()
          .on(testFile)
          .at(
            new DefaultTextRange(
              new DefaultTextPointer(1, 0),
              new DefaultTextPointer(1, 15)
            )
          )
          .message("Redundant braces in class definition")
      )
    )

    context.fileSystem().add(testFile)
    context.setActiveRules(activeRules)
    new ScalastyleSensor(rulesProfile, checker).execute(context)

    context.allIssues should contain theSameElementsAs expected
  }

  it should "open an issue for each style error in a module" in new Ctx {
    val fileSpec: FileSpec = new FileSpec {
      def name: String = Paths.get("./module1").resolve("TestFile.scala").toString
    }

    val styleError = StyleError(
      fileSpec,
      (new EmptyClassChecker).getClass,
      "org.scalastyle.scalariform.EmptyClassChecker",
      ErrorLevel,
      List.empty,
      lineNumber = Some(1),
      column = Some(5),
      customMessage = None
    )

    val checker = new ScalastyleCheckerAPI {
      private[scalastyle] def checkFiles(
        checker: Checker[FileSpec],
        configuration: ScalastyleConfiguration,
        files: scala.Seq[FileSpec]
      ): List[Message[FileSpec]] = List(styleError)
    }

    val testFile = TestInputFileBuilder
      .create("module1", "TestFile.scala")
      .setLanguage("scala")
      .setType(InputFile.Type.MAIN)
      .setLines(2)
      .setOriginalLineStartOffsets(Array(0, 16))
      .setOriginalLineEndOffsets(Array(15, 30))
      .build()

    val ruleKey = RuleKey.of("sonar-scala-scalastyle", "org.scalastyle.scalariform.EmptyClassChecker")

    val activeRules: ActiveRules =
      new ActiveRulesBuilder()
        .create(ruleKey)
        .setInternalKey("org.scalastyle.scalariform.EmptyClassChecker")
        .setSeverity(Severity.MAJOR.toString)
        .setParam("ruleClass", "org.scalastyle.scalariform.EmptyClassChecker")
        .activate()
        .build()

    val issue = new DefaultIssue().forRule(ruleKey)
    val expected = Seq(
      issue.at(
        issue
          .newLocation()
          .on(testFile)
          .at(
            new DefaultTextRange(
              new DefaultTextPointer(1, 0),
              new DefaultTextPointer(1, 15)
            )
          )
          .message("Redundant braces in class definition")
      )
    )

    val fs = new DefaultFileSystem(Paths.get("./module1")).setEncoding(Charset.defaultCharset)
    context.setFileSystem(fs)
    context.fileSystem().add(testFile)
    context.setActiveRules(activeRules)
    new ScalastyleSensor(rulesProfile, checker).execute(context)

    context.allIssues should contain theSameElementsAs expected
  }
}
