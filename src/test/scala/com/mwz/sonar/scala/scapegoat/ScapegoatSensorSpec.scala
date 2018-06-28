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
package scapegoat

import org.scalatest.{FlatSpec, LoneElement, OptionValues}
import org.sonar.api.batch.fs.InputFile
import org.sonar.api.batch.fs.internal.{
  DefaultFileSystem,
  DefaultTextPointer,
  DefaultTextRange,
  TestInputFileBuilder
}
import org.sonar.api.batch.rule.internal.ActiveRulesBuilder
import org.sonar.api.batch.sensor.internal.{DefaultSensorDescriptor, SensorContextTester}
import org.sonar.api.batch.sensor.issue.internal.DefaultIssue
import org.sonar.api.config.internal.MapSettings
import org.sonar.api.rule.RuleKey

import java.nio.file.{Path, Paths}

/** Tests the Scapegoat Sensor */
class ScapegoatSensorSpec extends FlatSpec with SensorContextMatchers with LoneElement with OptionValues {
  val scapegoatReportParser = new TestScapegoatReportParser()
  val scapegoatSensor = new ScapegoatSensor(scapegoatReportParser)

  behavior of "Scapegoat Sensor"

  it should "correctly set descriptor" in {
    val descriptor = new DefaultSensorDescriptor
    scapegoatSensor.describe(descriptor)

    descriptor should not be 'global
    descriptor.name shouldBe "Scapegoat Sensor"
    descriptor.`type` shouldBe InputFile.Type.MAIN
    descriptor.languages.loneElement shouldBe "scala"
    descriptor.ruleRepositories.loneElement shouldBe "sonar-scala-scapegoat-repository"
  }

  it should "get default scapegoat report path, when the scala version property is missing" in {
    val reportPath = scapegoatSensor.getScapegoatReportPath(new MapSettings().asConfig())

    reportPath shouldBe Paths.get("target", "scala-2.11", "scapegoat-report", "scapegoat.xml")
  }

  it should "get default scapegoat report path, when the scala version property is set" in {
    val reportPath = scapegoatSensor.getScapegoatReportPath(
      new MapSettings().setProperty("sonar.scala.version", "2.12.6").asConfig()
    )

    reportPath shouldBe Paths.get("target", "scala-2.12", "scapegoat-report", "scapegoat.xml")
  }

  it should "get scapegoat report path set in sonar properties" in {
    val reportPath = scapegoatSensor.getScapegoatReportPath(
      new MapSettings().setProperty("sonar.scala.scapegoat.reportPath", "target/report-path").asConfig()
    )

    reportPath shouldBe Paths.get("target", "report-path")
  }

  it should "get a module file using the filename extracted from the scapegoat report" in {
    val filesystem = new DefaultFileSystem(Paths.get("./"))

    val testFileA = TestInputFileBuilder
      .create("", "src/main/scala/com/mwz/sonar/scala/scapegoat/TestFileA.scala")
      .setLanguage("scala")
      .setType(InputFile.Type.MAIN)
      .build()
    filesystem.add(testFileA)

    val moduleFile =
      scapegoatSensor.getModuleFile("com/mwz/sonar/scala/scapegoat/TestFileA.scala", filesystem)

    moduleFile.value shouldBe testFileA
  }

  it should "not get a module file if its filename does not match any file" in {
    val filesystem = new DefaultFileSystem(Paths.get("./"))

    val moduleFile =
      scapegoatSensor.getModuleFile("com/mwz/sonar/scala/scapegoat/TestFileA.scala", filesystem)

    moduleFile shouldBe None
  }

  it should "not get a module file if its filename match two or more files" in {
    val filesystem = new DefaultFileSystem(Paths.get("./"))

    val testFileA = TestInputFileBuilder
      .create("", "src/main/scala/moduleA/com/mwz/sonar/scala/scapegoat/TestFile.scala")
      .setLanguage("scala")
      .setType(InputFile.Type.MAIN)
      .build()
    filesystem.add(testFileA)

    val testFileB = TestInputFileBuilder
      .create("", "src/main/scala/moduleB/com/mwz/sonar/scala/scapegoat/TestFile.scala")
      .setLanguage("scala")
      .setType(InputFile.Type.MAIN)
      .build()
    filesystem.add(testFileB)

    val moduleFile =
      scapegoatSensor.getModuleFile("com/mwz/sonar/scala/scapegoat/TestFile.scala", filesystem)

    moduleFile shouldBe None
  }

  it should "create an issue for each scapegoat report's warning" in {
    // create the sensor context
    val sensorContext = SensorContextTester.create(Paths.get("./"))

    // setup the filesystem
    val testFileA = TestInputFileBuilder
      .create("", "src/main/scala/com/mwz/sonar/scala/scapegoat/TestFileA.scala")
      .setLanguage("scala")
      .setType(InputFile.Type.MAIN)
      .setLines(2)
      .setOriginalLineOffsets(Array(0, 51)) // line 1 -> 50 chars, line 2 -> 80 chars
      .setLastValidOffset(131)
      .build()
    sensorContext.fileSystem.add(testFileA)

    val testFileB = TestInputFileBuilder
      .create("", "src/main/scala/com/mwz/sonar/scala/scapegoat/TestFileB.scala")
      .setLanguage("scala")
      .setType(InputFile.Type.MAIN)
      .setLines(3)
      .setOriginalLineOffsets(Array(0, 31, 82)) // line 1 -> 30 chars, line 2 -> 50 chars, line 3 -> 50 chars
      .setLastValidOffset(132)
      .build()
    sensorContext.fileSystem.add(testFileB)

    // setup the active rules
    val activeRulesBuilder = new ActiveRulesBuilder()

    val emptyClassRuleKey =
      RuleKey.of("sonar-scala-scapegoat-repository", "Empty case class")
    activeRulesBuilder
      .create(emptyClassRuleKey)
      .setInternalKey("com.sksamuel.scapegoat.inspections.EmptyCaseClass")
      .activate()

    val arrayPassedToStringFormatRuleKey =
      RuleKey.of("sonar-scala-scapegoat-repository", "Array passed to String.forma")
    activeRulesBuilder
      .create(arrayPassedToStringFormatRuleKey)
      .setInternalKey("com.sksamuel.scapegoat.inspections.string.ArraysInFormat")
      .activate()

    val lonelySealedTraitRuleKey =
      RuleKey.of("sonar-scala-scapegoat-repository", "Lonely sealed trait")
    activeRulesBuilder
      .create(lonelySealedTraitRuleKey)
      .setInternalKey("com.sksamuel.scapegoat.inspections.LonelySealedTrait")
      .activate()

    val redundantFinalModifierOnMethodRuleKey =
      RuleKey.of("sonar-scala-scapegoat-repository", "Redundant final modifier on method")
    activeRulesBuilder
      .create(redundantFinalModifierOnMethodRuleKey)
      .setInternalKey("com.sksamuel.scapegoat.inspections.RedundantFinalModifierOnMethod")
      .activate()

    sensorContext.setActiveRules(activeRulesBuilder.build())

    // set the scapegoat report path property
    sensorContext.setSettings(
      new MapSettings()
        .setProperty("sonar.scala.scapegoat.reportPath", "scapegoat-report/two-files-five-warnings.xml")
    )

    // execute the sensor
    scapegoatSensor.execute(sensorContext)

    // validate the sensor behavior
    val testFileAIssueEmptyCaseClass =
      new DefaultIssue().forRule(emptyClassRuleKey)
    testFileAIssueEmptyCaseClass.at(
      testFileAIssueEmptyCaseClass
        .newLocation()
        .on(testFileA)
        .at(new DefaultTextRange(new DefaultTextPointer(1, 0), new DefaultTextPointer(1, 50)))
        .message(
          "Empty case class\nEmpty case class can be rewritten as a case object"
        )
    )

    val testFileAIssueArrayPassedToStringFormat =
      new DefaultIssue().forRule(arrayPassedToStringFormatRuleKey)
    testFileAIssueArrayPassedToStringFormat.at(
      testFileAIssueArrayPassedToStringFormat
        .newLocation()
        .on(testFileA)
        .at(new DefaultTextRange(new DefaultTextPointer(2, 0), new DefaultTextPointer(2, 80)))
        .message(
          "Array passed to String.format\nscala.Predef.augmentString(\"data is: %s\").format(scala.Array.apply(1, 2, 3))"
        )
    )

    val testFileBIssueLonelySealedTrait =
      new DefaultIssue().forRule(lonelySealedTraitRuleKey)
    testFileBIssueLonelySealedTrait.at(
      testFileBIssueLonelySealedTrait
        .newLocation()
        .on(testFileB)
        .at(new DefaultTextRange(new DefaultTextPointer(1, 0), new DefaultTextPointer(1, 30)))
        .message(
          "Lonely sealed trait\nSealed trait NotUsed has no implementing classes"
        )
    )

    val testFileBIssueRedundantFinalModifierOnMethod =
      new DefaultIssue().forRule(redundantFinalModifierOnMethodRuleKey)
    testFileBIssueRedundantFinalModifierOnMethod.at(
      testFileBIssueRedundantFinalModifierOnMethod
        .newLocation()
        .on(testFileB)
        .at(new DefaultTextRange(new DefaultTextPointer(2, 0), new DefaultTextPointer(2, 50)))
        .message(
          "Redundant final modifier on method\ncom.mwz.sonar.scala.scapegoat.TestFileB.testMethod cannot be overridden, final modifier is redundant"
        )
    )

    val testFileBIssueEmptyCaseClass =
      new DefaultIssue().forRule(emptyClassRuleKey)
    testFileBIssueEmptyCaseClass.at(
      testFileBIssueEmptyCaseClass
        .newLocation()
        .on(testFileB)
        .at(new DefaultTextRange(new DefaultTextPointer(3, 0), new DefaultTextPointer(3, 50)))
        .message(
          "Empty case class\nEmpty case class can be rewritten as a case object"
        )
    )

    sensorContext.allIssues should contain theSameElementsAs Seq(
      testFileAIssueEmptyCaseClass,
      testFileAIssueArrayPassedToStringFormat,
      testFileBIssueLonelySealedTrait,
      testFileBIssueRedundantFinalModifierOnMethod,
      testFileBIssueEmptyCaseClass
    )
  }

  it should "not report any issue if the report is empty" in {
    // create the sensor context
    val sensorContext = SensorContextTester.create(Paths.get("./"))

    // setup the filesystem
    val testFileA = TestInputFileBuilder
      .create("", "src/main/scala/com/mwz/sonar/scala/scapegoat/TestFileA.scala")
      .setLanguage("scala")
      .setType(InputFile.Type.MAIN)
      .setLines(2)
      .setOriginalLineOffsets(Array(0, 51)) // line 1 -> 50 chars, line 2 -> 80 chars
      .setLastValidOffset(131)
      .build()
    sensorContext.fileSystem.add(testFileA)

    // setup the active rules
    val activeRulesBuilder = new ActiveRulesBuilder()

    val emptyClassRuleKey =
      RuleKey.of("sonar-scala-scapegoat-repository", "Empty case class")
    activeRulesBuilder
      .create(emptyClassRuleKey)
      .setInternalKey("com.sksamuel.scapegoat.inspections.EmptyCaseClass")
      .activate()

    sensorContext.setActiveRules(activeRulesBuilder.build())

    // set the scapegoat report path property
    sensorContext.setSettings(
      new MapSettings()
        .setProperty("sonar.scala.scapegoat.reportPath", "scapegoat-report/no-warnings.xml")
    )

    // execute the sensor
    scapegoatSensor.execute(sensorContext)

    // validate the sensor behavior
    sensorContext.allIssues shouldBe empty
  }

  it should "not report an issue if its rule is not active" in {
    // create the sensor context
    val sensorContext = SensorContextTester.create(Paths.get("./"))

    // setup the filesystem
    val testFileA = TestInputFileBuilder
      .create("", "src/main/scala/com/mwz/sonar/scala/scapegoat/TestFileA.scala")
      .setLanguage("scala")
      .setType(InputFile.Type.MAIN)
      .setLines(2)
      .setOriginalLineOffsets(Array(0, 51)) // line 1 -> 50 chars, line 2 -> 80 chars
      .setLastValidOffset(131)
      .build()
    sensorContext.fileSystem.add(testFileA)

    // set the scapegoat report path property
    sensorContext.setSettings(
      new MapSettings()
        .setProperty("sonar.scala.scapegoat.reportPath", "scapegoat-report/one-file-one-warning.xml")
    )

    // execute the sensor
    scapegoatSensor.execute(sensorContext)

    // validate the sensor behavior
    sensorContext.allIssues shouldBe empty
  }
}

/** Mock of the ScapegoatReportParser */
final class TestScapegoatReportParser extends ScapegoatReportParserAPI {
  override def parse(reportPath: Path): Map[String, Seq[ScapegoatIssue]] = reportPath.toString match {
    case "scapegoat-report/no-warnings.xml" =>
      Map()
    case "scapegoat-report/one-file-one-warning.xml" =>
      Map(
        "com/mwz/sonar/scala/scapegoat/TestFileA.scala" -> Seq(
          ScapegoatIssue(
            line = 1,
            text = "Empty case class",
            snippet = "Empty case class can be rewritten as a case object",
            file = "com/mwz/sonar/scala/scapegoat/TestFileA.scala",
            inspectionId = "com.sksamuel.scapegoat.inspections.EmptyCaseClass"
          )
        )
      )
    case "scapegoat-report/two-files-five-warnings.xml" =>
      Map(
        "com/mwz/sonar/scala/scapegoat/TestFileA.scala" -> Seq(
          ScapegoatIssue(
            line = 1,
            text = "Empty case class",
            snippet = "Empty case class can be rewritten as a case object",
            file = "com/mwz/sonar/scala/scapegoat/TestFileA.scala",
            inspectionId = "com.sksamuel.scapegoat.inspections.EmptyCaseClass"
          ),
          ScapegoatIssue(
            line = 2,
            text = "Array passed to String.format",
            snippet = "scala.Predef.augmentString(\"data is: %s\").format(scala.Array.apply(1, 2, 3))",
            file = "com/mwz/sonar/scala/scapegoat/TestFileA.scala",
            inspectionId = "com.sksamuel.scapegoat.inspections.string.ArraysInFormat"
          )
        ),
        "com/mwz/sonar/scala/scapegoat/TestFileB.scala" -> Seq(
          ScapegoatIssue(
            line = 1,
            text = "Lonely sealed trait",
            snippet = "Sealed trait NotUsed has no implementing classes",
            file = "com/mwz/sonar/scala/scapegoat/TestFileB.scala",
            inspectionId = "com.sksamuel.scapegoat.inspections.LonelySealedTrait"
          ),
          ScapegoatIssue(
            line = 2,
            text = "Redundant final modifier on method",
            snippet =
              "com.mwz.sonar.scala.scapegoat.TestFileB.testMethod cannot be overridden, final modifier is redundant",
            file = "com/mwz/sonar/scala/scapegoat/TestFileB.scala",
            inspectionId = "com.sksamuel.scapegoat.inspections.RedundantFinalModifierOnMethod"
          ),
          ScapegoatIssue(
            line = 3,
            text = "Empty case class",
            snippet = "Empty case class can be rewritten as a case object",
            file = "com/mwz/sonar/scala/scapegoat/TestFileB.scala",
            inspectionId = "com.sksamuel.scapegoat.inspections.EmptyCaseClass"
          )
        )
      )
  }
}
