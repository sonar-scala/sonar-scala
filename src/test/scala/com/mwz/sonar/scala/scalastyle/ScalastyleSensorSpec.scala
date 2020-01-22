/*
 * Copyright (C) 2018-2020  All sonar-scala contributors
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
package scalastyle

import java.nio.file.Paths

import scala.jdk.CollectionConverters._

import com.mwz.sonar.scala.pr.GlobalIssues
import com.mwz.sonar.scala.pr.Issue
import com.mwz.sonar.scala.util.PathUtils.cwd
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
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.mockito.MockitoSugar
import org.sonar.api.batch.fs.InputFile
import org.sonar.api.batch.fs.internal.TestInputFileBuilder
import org.sonar.api.batch.rule.Severity
import org.sonar.api.batch.rule.internal.{ActiveRulesBuilder, NewActiveRule}
import org.sonar.api.batch.sensor.internal.{DefaultSensorDescriptor, SensorContextTester}
import org.sonar.api.config.internal.MapSettings
import org.sonar.api.rule.RuleKey

class ScalastyleSensorSpec
    extends AnyFlatSpec
    with Matchers
    with SensorContextMatchers
    with LoneElement
    with OptionValues
    with MockitoSugar {
  trait Ctx {
    val globalConfig = new GlobalConfig(new MapSettings().asConfig)
    val globalIssues = new GlobalIssues()
    val context = SensorContextTester.create(Paths.get("./"))
    val scalastyleChecker = new ScalastyleCheckerAPI {
      private[scalastyle] def checkFiles(
        checker: Checker[FileSpec],
        configuration: ScalastyleConfiguration,
        files: scala.Seq[FileSpec]
      ): List[Message[FileSpec]] = List.empty
    }

    val scalastyleSensor = new ScalastyleSensor(globalConfig, globalIssues, scalastyleChecker)
    val descriptor = new DefaultSensorDescriptor
  }

  it should "exposecorrect constants" in {
    ScalastyleSensor.SensorName shouldBe "Scalastyle Sensor"
    ScalastyleSensor.ScalastyleDisablePropertyKey shouldBe "sonar.scala.scalastyle.disable"
  }

  it should "read the 'disable' config property" in {
    val context = SensorContextTester.create(cwd)
    ScalastyleSensor.shouldEnableSensor(context.config) shouldBe true

    val context2 = SensorContextTester.create(cwd)
    context2.setSettings(
      new MapSettings().setProperty(ScalastyleSensor.ScalastyleDisablePropertyKey, "maybe")
    )
    ScalastyleSensor.shouldEnableSensor(context2.config) shouldBe true

    val context3 = SensorContextTester.create(cwd)
    context3.setSettings(new MapSettings().setProperty(ScalastyleSensor.ScalastyleDisablePropertyKey, "true"))
    ScalastyleSensor.shouldEnableSensor(context3.config) shouldBe false
  }

  it should "execute the sensor if the 'disable' flag wasn't set" in new Ctx {
    scalastyleSensor.describe(descriptor)
    descriptor.configurationPredicate.test(context.config) shouldBe true
  }

  it should "respect the 'disable' config property and skip execution if set to true" in new Ctx {
    context.setSettings(new MapSettings().setProperty(ScalastyleSensor.ScalastyleDisablePropertyKey, "true"))
    scalastyleSensor.describe(descriptor)
    descriptor.configurationPredicate.test(context.config) shouldBe false
  }

  it should "correctly set descriptor" in new Ctx {
    scalastyleSensor.describe(descriptor)

    descriptor.isGlobal shouldBe false
    descriptor.name shouldBe ScalastyleSensor.SensorName
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

  it should "get a list of files for analysis" in new Ctx {
    val testFileA =
      new TestInputFileBuilder("test-project", "TestFile.scala")
        .setLanguage("scala")
        .setType(InputFile.Type.MAIN)
        .build()
    val testFileB =
      new TestInputFileBuilder("test-project", "TestFileSpec.scala")
        .setLanguage("scala")
        .setType(InputFile.Type.TEST)
        .build()
    val testFileC =
      new TestInputFileBuilder("test-project", "TestFile.java")
        .setLanguage("java")
        .setType(InputFile.Type.MAIN)
        .build()

    context.fileSystem.add(testFileA)
    context.fileSystem.add(testFileB)
    context.fileSystem.add(testFileC)

    val expected = cwd.resolve("test-project/TestFile.scala").toString

    ScalastyleSensor.fileSpecs(context).loneElement.name shouldBe expected
  }

  it should "convert a rule to a configuration checker" in {
    val rule1Key = RuleKey.of("sonar-scala-scalastyle", "rule1")

    val activeRules = (new ActiveRulesBuilder)
      .addRule(
        (new NewActiveRule.Builder)
          .setLanguage("scala")
          .setRuleKey(rule1Key)
          .setName("rule1")
          .setParam("ruleClass", "hello.class.name")
          .setParam("param1", "value1")
          .setParam("param2", "value2")
          .setSeverity(Severity.MAJOR.toString)
          .build()
      )
      .build()

    val activeRule = activeRules.find(rule1Key)

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
    val badRuleKey = RuleKey.of("sonar-scala-scalastyle", "badRule")

    val activeRules = (new ActiveRulesBuilder)
      .addRule(
        (new NewActiveRule.Builder)
          .setRuleKey(badRuleKey)
          .build()
      )
      .build()

    val activeRule = activeRules.find(badRuleKey)

    ScalastyleSensor.ruleToConfigurationChecker(activeRule) shouldBe empty
  }

  it should "look up a rule from a style error" in new Ctx {
    val rule1Key = RuleKey.of("sonar-scala-scalastyle", "rule1")

    val activeRules = (new ActiveRulesBuilder)
      .addRule(
        (new NewActiveRule.Builder)
          .setRuleKey(RuleKey.of("sonar-scala-scalastyle", "rule1"))
          .setLanguage("scala")
          .setName("rule1")
          .setSeverity(ErrorLevel.toString)
          .setParam("param1", "value1")
          .build()
      )
      .build()

    val fileSpec = new FileSpec {
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

    val result = ScalastyleSensor.ruleFromStyleError(context, styleError).value
    result.language shouldBe "scala"
    result.ruleKey shouldBe rule1Key
    result.severity shouldBe ErrorLevel.toString
    result.params.asScala.loneElement shouldBe ("param1" -> "value1")
  }

  it should "look up a rules by key and not internal key" in new Ctx {
    val rule1Key = RuleKey.of("sonar-scala-scalastyle", "rule1")

    val activeRules = (new ActiveRulesBuilder)
      .addRule(
        (new NewActiveRule.Builder)
          .setRuleKey(rule1Key)
          .setInternalKey("org.scalastyle.file.RegexChecker-template")
          .setTemplateRuleKey("sonar-scala-scalastyle:org.scalastyle.file.RegexChecker-template")
          .setLanguage("scala")
          .setName("rule1")
          .setParam("line", "false")
          .setParam("regex", ".")
          .setParam("ruleClass", "org.scalastyle.file.RegexChecker")
          .setSeverity(ErrorLevel.toString)
          .build()
      )
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

    val result = ScalastyleSensor.ruleFromStyleError(context, styleError).value
    result.language shouldBe "scala"
    result.ruleKey shouldBe rule1Key
    result.internalKey shouldBe "org.scalastyle.file.RegexChecker-template"
    result.templateRuleKey shouldBe "sonar-scala-scalastyle:org.scalastyle.file.RegexChecker-template"
    result.severity shouldBe ErrorLevel.toString
    result.params.asScala shouldBe Map(
      "line" -> "false",
      "regex" -> ".",
      "ruleClass" -> "org.scalastyle.file.RegexChecker"
    )
  }

  it should "not open any issues if there are no active rules" in new Ctx {
    scalastyleSensor.execute(context)
    context.allIssues shouldBe empty
  }

  it should "not open any issues if there are no style errors reported" in new Ctx {
    val activeRules = (new ActiveRulesBuilder)
      .addRule(
        (new NewActiveRule.Builder)
          .setRuleKey(RuleKey.of("sonar-scala-scalastyle", "rule1"))
          .setLanguage("scala")
          .setName("rule1")
          .setSeverity(Severity.MAJOR.toString)
          .setParam("param1", "value1")
          .build()
      )
      .build()

    context.setActiveRules(activeRules)
    scalastyleSensor.execute(context)

    context.allIssues shouldBe empty
  }

  it should "open an issue for each style error" in new Ctx {
    val ruleKey = RuleKey.of("sonar-scala-scalastyle", "org.scalastyle.scalariform.EmptyClassChecker")

    val activeRules = (new ActiveRulesBuilder)
      .addRule(
        (new NewActiveRule.Builder)
          .setRuleKey(ruleKey)
          .setName("EmptyClassChecker")
          .setInternalKey("org.scalastyle.scalariform.EmptyClassChecker")
          .setSeverity(Severity.MAJOR.toString)
          .setParam("ruleClass", "org.scalastyle.scalariform.EmptyClassChecker")
          .build()
      )
      .build()

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

    val testFile =
      new TestInputFileBuilder("test-project", "TestFile.scala")
        .setLanguage("scala")
        .setType(InputFile.Type.MAIN)
        .setLines(2)
        .setOriginalLineStartOffsets(Array(0, 16))
        .setOriginalLineEndOffsets(Array(15, 30))
        .build()

    context.fileSystem.add(testFile)
    context.setActiveRules(activeRules)
    new ScalastyleSensor(globalConfig, globalIssues, checker).execute(context)

    val result = context.allIssues.loneElement
    result.ruleKey shouldBe ruleKey
    result.primaryLocation.inputComponent shouldBe testFile
    result.primaryLocation.textRange shouldBe testFile.newRange(1, 0, 1, 15)
    result.primaryLocation.message shouldBe "Redundant braces in class definition"
  }

  it should "collect global issues if issue decoration is enabled" in new Ctx {
    val ruleKey = RuleKey.of("sonar-scala-scalastyle", "org.scalastyle.scalariform.EmptyClassChecker")

    val activeRules = (new ActiveRulesBuilder)
      .addRule(
        (new NewActiveRule.Builder)
          .setRuleKey(ruleKey)
          .setName("EmptyClassChecker")
          .setInternalKey("org.scalastyle.scalariform.EmptyClassChecker")
          .setSeverity(Severity.MAJOR.toString)
          .setParam("ruleClass", "org.scalastyle.scalariform.EmptyClassChecker")
          .build()
      )
      .build()

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

    val testFile =
      new TestInputFileBuilder("test-project", "TestFile.scala")
        .setLanguage("scala")
        .setType(InputFile.Type.MAIN)
        .setLines(2)
        .setOriginalLineStartOffsets(Array(0, 16))
        .setOriginalLineEndOffsets(Array(15, 30))
        .build()

    context.fileSystem.add(testFile)
    context.setActiveRules(activeRules)
    val prDecorationConf =
      new GlobalConfig(
        new MapSettings()
          .setProperty("sonar.scala.pullrequest.provider", "github")
          .setProperty("sonar.scala.pullrequest.number", "123")
          .setProperty("sonar.scala.pullrequest.github.repository", "owner/repo")
          .setProperty("sonar.scala.pullrequest.github.oauth", "token")
          .asConfig
      )
    new ScalastyleSensor(prDecorationConf, globalIssues, checker).execute(context)

    context.allIssues.asScala shouldBe empty
    globalIssues.allIssues shouldBe Map(
      testFile -> List(
        Issue(ruleKey, testFile, 1, Severity.MAJOR, "Redundant braces in class definition")
      )
    )
  }
}
