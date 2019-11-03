/*
 * Copyright (C) 2018-2019  All sonar-scala contributors
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
package scapegoat

import java.nio.file.{Path, Paths}

import scala.collection.JavaConverters._

import com.mwz.sonar.scala.pr.GlobalIssues
import com.mwz.sonar.scala.util.PathUtils.cwd
import org.mockito.ArgumentMatchers._
import org.mockito.Mockito._
import org.scalatest.{FlatSpec, LoneElement, OptionValues}
import org.scalatestplus.mockito.MockitoSugar
import org.sonar.api.batch.fs.InputFile
import org.sonar.api.batch.fs.internal.{DefaultFileSystem, TestInputFileBuilder}
import org.sonar.api.batch.rule.internal.{ActiveRulesBuilder, NewActiveRule}
import org.sonar.api.batch.sensor.internal.{DefaultSensorDescriptor, SensorContextTester}
import org.sonar.api.config.internal.MapSettings
import org.sonar.api.rule.RuleKey
import scalariform.ScalaVersion

/** Tests the Scapegoat Sensor. */
class ScapegoatSensorSpec
    extends FlatSpec
    with MockitoSugar
    with SensorContextMatchers
    with LoneElement
    with OptionValues {
  val globalConfig = new GlobalConfig(new MapSettings().asConfig)
  val globalIssues = new GlobalIssues()
  val scapegoatReportParser = new TestScapegoatReportParser()
  val scapegoatSensor = new ScapegoatSensor(globalConfig, globalIssues, scapegoatReportParser)

  it should "read the 'disable' config property" in {
    val context = SensorContextTester.create(cwd)
    ScapegoatSensor.shouldEnableSensor(context.config) shouldBe true

    val context2 = SensorContextTester.create(cwd)
    context2.setSettings(new MapSettings().setProperty("sonar.scala.scapegoat.disable", "maybe"))
    ScapegoatSensor.shouldEnableSensor(context2.config) shouldBe true

    val context3 = SensorContextTester.create(cwd)
    context3.setSettings(new MapSettings().setProperty("sonar.scala.scapegoat.disable", "true"))
    ScapegoatSensor.shouldEnableSensor(context3.config) shouldBe false
  }

  it should "execute the sensor if the 'disable' flag wasn't set" in {
    val context = SensorContextTester.create(cwd)
    val scapegoatReportParser = mock[ScapegoatReportParserAPI]
    val scapegoatSensor = new ScapegoatSensor(globalConfig, globalIssues, scapegoatReportParser)

    val descriptor = new DefaultSensorDescriptor
    scapegoatSensor.describe(descriptor)
    descriptor.configurationPredicate.test(context.config) shouldBe true

    when(scapegoatReportParser.parse(any()))
      .thenReturn(Map.empty[String, Seq[ScapegoatIssue]])

    scapegoatSensor.execute(context)
    verify(scapegoatReportParser).parse(any())
  }

  it should "respect the 'disable' config property and skip scapegoat analysis if set to true" in {
    val context = SensorContextTester.create(cwd)
    context.setSettings(new MapSettings().setProperty("sonar.scala.scapegoat.disable", "true"))

    val scapegoatReportParser = mock[ScapegoatReportParserAPI]
    val scapegoatSensor = new ScapegoatSensor(globalConfig, globalIssues, scapegoatReportParser)

    val descriptor = new DefaultSensorDescriptor
    scapegoatSensor.describe(descriptor)
    descriptor.configurationPredicate.test(context.config) shouldBe false
  }

  it should "construct the default report path" in {
    val scalaVersion = ScalaVersion(2, 12, "6")
    ScapegoatSensor.getDefaultScapegoatReportPath(scalaVersion) shouldBe Paths.get(
      "target",
      "scala-2.12",
      "scapegoat-report",
      "scapegoat.xml"
    )
  }

  it should "correctly set descriptor" in {
    val descriptor = new DefaultSensorDescriptor
    scapegoatSensor.describe(descriptor)

    descriptor should not be 'global
    descriptor.name shouldBe "Scapegoat Sensor"
    descriptor.`type` shouldBe InputFile.Type.MAIN
    descriptor.languages.loneElement shouldBe "scala"
    descriptor.ruleRepositories.loneElement shouldBe "sonar-scala-scapegoat"
  }

  it should "get default scapegoat report path, when the scala version property is missing" in {
    val reportPath = scapegoatSensor.getScapegoatReportPath(new MapSettings().asConfig())

    reportPath shouldBe Paths.get("target", "scala-2.12", "scapegoat-report", "scapegoat.xml")
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

  it should "get a root module file using the filename extracted from the scapegoat report" in {
    val filesystem = new DefaultFileSystem(Paths.get("./"))

    val testFileA = TestInputFileBuilder
      .create("", "src/main/scala/com/mwz/sonar/scala/scapegoat/TestFileA.scala")
      .setLanguage("scala")
      .setType(InputFile.Type.MAIN)
      .build()
    filesystem.add(testFileA)

    val moduleFile =
      scapegoatSensor.getProjectFile("com/mwz/sonar/scala/scapegoat/TestFileA.scala", filesystem)

    moduleFile.value shouldBe testFileA
  }

  it should "get a module file using the filename extracted from the scapegoat report" in {
    val filesystem = new DefaultFileSystem(Paths.get("./module1"))

    val testFileA = TestInputFileBuilder
      .create("module1", "src/main/scala/com/mwz/sonar/scala/scapegoat/TestFileA.scala")
      .setLanguage("scala")
      .setType(InputFile.Type.MAIN)
      .build()
    filesystem.add(testFileA)

    val moduleFile =
      scapegoatSensor.getProjectFile("com/mwz/sonar/scala/scapegoat/TestFileA.scala", filesystem)

    moduleFile.value shouldBe testFileA
  }

  it should "not get a module file if its filename does not match any file" in {
    val filesystem = new DefaultFileSystem(Paths.get("./"))

    val moduleFile =
      scapegoatSensor.getProjectFile("com/mwz/sonar/scala/scapegoat/TestFileA.scala", filesystem)

    moduleFile shouldBe None
  }

  it should "create an issue for each scapegoat report's warning" in {
    // Create the sensor context.
    val sensorContext = SensorContextTester.create(Paths.get("./"))

    // Setup the filesystem.
    val testFileA =
      new TestInputFileBuilder("test-project", "src/main/scala/com/mwz/sonar/scala/scapegoat/TestFileA.scala")
        .setLanguage("scala")
        .setType(InputFile.Type.MAIN)
        .setLines(2)
        .setOriginalLineStartOffsets(Array(0, 51)) // line 1 -> 50 chars, line 2 -> 80 chars
        .setOriginalLineEndOffsets(Array(50, 131))
        .setLastValidOffset(131)
        .build()
    sensorContext.fileSystem.add(testFileA)

    val testFileB =
      new TestInputFileBuilder("test-project", "src/main/scala/com/mwz/sonar/scala/scapegoat/TestFileB.scala")
        .setLanguage("scala")
        .setType(InputFile.Type.MAIN)
        .setLines(3)
        .setOriginalLineStartOffsets(Array(0, 31, 82)) // line 1 -> 30 chars, line 2 -> 50 chars, line 3 -> 50 chars
        .setOriginalLineEndOffsets(Array(30, 81, 132))
        .setLastValidOffset(132)
        .build()
    sensorContext.fileSystem.add(testFileB)

    // Setup the active rules.
    val emptyClassRuleKey = RuleKey.of("sonar-scala-scapegoat", "Empty case class")
    val arrayPassedToStringFormatRuleKey =
      RuleKey.of("sonar-scala-scapegoat", "Array passed to String.format")
    val lonelySealedTraitRuleKey = RuleKey.of("sonar-scala-scapegoat", "Lonely sealed trait")
    val redundantFinalModifierRuleKey =
      RuleKey.of("sonar-scala-scapegoat", "Redundant final modifier on method")

    val activeRules = (new ActiveRulesBuilder)
      .addRule(
        (new NewActiveRule.Builder)
          .setRuleKey(emptyClassRuleKey)
          .setInternalKey("com.sksamuel.scapegoat.inspections.EmptyCaseClass")
          .setLanguage("scala")
          .build()
      )
      .addRule(
        (new NewActiveRule.Builder)
          .setRuleKey(arrayPassedToStringFormatRuleKey)
          .setInternalKey("com.sksamuel.scapegoat.inspections.string.ArraysInFormat")
          .setLanguage("scala")
          .build()
      )
      .addRule(
        (new NewActiveRule.Builder)
          .setRuleKey(lonelySealedTraitRuleKey)
          .setInternalKey("com.sksamuel.scapegoat.inspections.LonelySealedTrait")
          .setLanguage("scala")
          .build()
      )
      .addRule(
        (new NewActiveRule.Builder)
          .setRuleKey(redundantFinalModifierRuleKey)
          .setInternalKey("com.sksamuel.scapegoat.inspections.RedundantFinalModifierOnMethod")
          .setLanguage("scala")
          .build()
      )
      .build()

    sensorContext.setActiveRules(activeRules)

    // Set the scapegoat report path property.
    sensorContext.setSettings(
      new MapSettings()
        .setProperty("sonar.scala.scapegoat.reportPath", "scapegoat-report/two-files-five-warnings.xml")
    )

    // Execute the sensor.
    scapegoatSensor.execute(sensorContext)

    // Validate the sensor behavior.
    sensorContext.allIssues.asScala.map { issue =>
      (issue.primaryLocation.inputComponent, issue.ruleKey) -> (
        issue.primaryLocation.textRange,
        issue.primaryLocation.message
      )
    }.toMap shouldBe Map(
      (testFileA, emptyClassRuleKey) -> (
        testFileA.newRange(1, 0, 1, 50),
        """Empty case class
          |Empty case class can be rewritten as a case object""".stripMargin
      ),
      (testFileA, arrayPassedToStringFormatRuleKey) -> (
        testFileA.newRange(2, 0, 2, 80),
        """Array passed to String.format
          |scala.Predef.augmentString("data is: %s").format(scala.Array.apply(1, 2, 3))""".stripMargin
      ),
      (testFileB, lonelySealedTraitRuleKey) -> (
        testFileB.newRange(1, 0, 1, 30),
        """Lonely sealed trait
          |Sealed trait NotUsed has no implementing classes""".stripMargin
      ),
      (testFileB, redundantFinalModifierRuleKey) -> (
        testFileB.newRange(2, 0, 2, 50),
        """Redundant final modifier on method
          |com.mwz.sonar.scala.scapegoat.TestFileB.testMethod cannot be overridden, final modifier is redundant""".stripMargin
      ),
      (testFileB, emptyClassRuleKey) -> (
        testFileB.newRange(3, 0, 3, 50),
        """Empty case class
          |Empty case class can be rewritten as a case object""".stripMargin
      )
    )
  }

  it should "not report any issue if the report is empty" in {
    // Create the sensor context.
    val sensorContext = SensorContextTester.create(Paths.get("./"))

    // Setup the filesystem.
    val testFileA =
      new TestInputFileBuilder("test-project", "src/main/scala/com/mwz/sonar/scala/scapegoat/TestFileA.scala")
        .setLanguage("scala")
        .setType(InputFile.Type.MAIN)
        .setLines(2)
        .setOriginalLineStartOffsets(Array(0, 51)) // line 1 -> 50 chars, line 2 -> 80 chars
        .setOriginalLineEndOffsets(Array(50, 131))
        .setLastValidOffset(131)
        .build()
    sensorContext.fileSystem.add(testFileA)

    // Setup the active rules.
    val activeRules = (new ActiveRulesBuilder())
      .addRule(
        (new NewActiveRule.Builder)
          .setRuleKey(RuleKey.of("sonar-scala-scapegoat", "Empty case class"))
          .setLanguage("scala")
          .build()
      )
      .build()
    sensorContext.setActiveRules(activeRules)

    // Set the scapegoat report path property.
    sensorContext.setSettings(
      new MapSettings()
        .setProperty("sonar.scala.scapegoat.reportPath", "scapegoat-report/no-warnings.xml")
    )

    // Execute the sensor.
    scapegoatSensor.execute(sensorContext)

    // Validate the sensor behavior.
    sensorContext.allIssues shouldBe empty
  }

  it should "not report an issue if its rule is not active" in {
    // Create the sensor context.
    val sensorContext = SensorContextTester.create(Paths.get("./"))

    // Setup the filesystem.
    val testFileA =
      new TestInputFileBuilder("test-project", "src/main/scala/com/mwz/sonar/scala/scapegoat/TestFileA.scala")
        .setLanguage("scala")
        .setType(InputFile.Type.MAIN)
        .setLines(2)
        .setOriginalLineStartOffsets(Array(0, 51)) // line 1 -> 50 chars, line 2 -> 80 chars
        .setOriginalLineEndOffsets(Array(50, 131))
        .setLastValidOffset(131)
        .build()
    sensorContext.fileSystem.add(testFileA)

    // Set the scapegoat report path property.
    sensorContext.setSettings(
      new MapSettings()
        .setProperty("sonar.scala.scapegoat.reportPath", "scapegoat-report/one-file-one-warning.xml")
    )

    // Execute the sensor.
    scapegoatSensor.execute(sensorContext)

    // Validate the sensor behavior.
    sensorContext.allIssues shouldBe empty
  }

  it should "report issues for absolute files" in {
    // Create the sensor context.
    val sensorContext = SensorContextTester.create(cwd)

    // Setup the filesystem.
    val testFile =
      new TestInputFileBuilder("test-project", "app/TestFile.scala")
        .setLanguage("scala")
        .setType(InputFile.Type.MAIN)
        .setLines(2)
        .setOriginalLineStartOffsets(Array(0, 51)) // line 1 -> 50 chars, line 2 -> 80 chars
        .setOriginalLineEndOffsets(Array(50, 131))
        .setLastValidOffset(131)
        .build()

    // Setup the active rules.
    val emptyClassRuleKey = RuleKey.of("sonar-scala-scapegoat", "Empty case class")

    val activeRules = (new ActiveRulesBuilder)
      .addRule(
        (new NewActiveRule.Builder)
          .setRuleKey(emptyClassRuleKey)
          .setInternalKey("com.sksamuel.scapegoat.inspections.EmptyCaseClass")
          .setLanguage("scala")
          .build()
      )
      .build()

    // Set up the sensor.
    sensorContext.setSettings(
      new MapSettings().setProperty(
        "sonar.scala.scapegoat.reportPath",
        "scapegoat-report/absolute-file-path.xml"
      )
    )
    sensorContext.fileSystem.add(testFile)
    sensorContext.setActiveRules(activeRules)

    // Execute the sensor.
    scapegoatSensor.execute(sensorContext)

    // Validate the sensor behavior.
    val result = sensorContext.allIssues.loneElement
    result.ruleKey shouldBe emptyClassRuleKey
    result.primaryLocation.inputComponent shouldBe testFile
    result.primaryLocation.textRange shouldBe testFile.newRange(1, 0, 1, 50)
    result.primaryLocation.message shouldBe "Empty case class\nEmpty case class can be rewritten as a case object"
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
    case "scapegoat-report/absolute-file-path.xml" =>
      val file = cwd.resolve("app").resolve("TestFile.scala").toString
      Map(
        file -> Seq(
          ScapegoatIssue(
            line = 1,
            text = "Empty case class",
            snippet = "Empty case class can be rewritten as a case object",
            file = file,
            inspectionId = "com.sksamuel.scapegoat.inspections.EmptyCaseClass"
          )
        )
      )
  }
}
