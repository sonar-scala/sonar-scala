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
package com.mwz.sonar.scala.scalastyle

import java.nio.file.Path
import java.nio.file.Paths

import com.mwz.sonar.scala.SensorContextMatchers
import com.mwz.sonar.scala.checkstyle.CheckstyleIssue
import com.mwz.sonar.scala.checkstyle.CheckstyleReportParser
import com.mwz.sonar.scala.util.PathUtils.cwd
import org.scalatest.FlatSpec
import org.scalatest.LoneElement
import org.scalatest.OptionValues
import org.scalatest.mockito.MockitoSugar
import org.sonar.api.batch.fs.InputFile
import org.sonar.api.batch.fs.internal.DefaultFileSystem
import org.sonar.api.batch.fs.internal.DefaultTextPointer
import org.sonar.api.batch.fs.internal.DefaultTextRange
import org.sonar.api.batch.fs.internal.TestInputFileBuilder
import org.sonar.api.batch.rule.internal.ActiveRulesBuilder
import org.sonar.api.batch.sensor.internal.DefaultSensorDescriptor
import org.sonar.api.batch.sensor.internal.SensorContextTester
import org.sonar.api.batch.sensor.issue.internal.DefaultIssue
import org.sonar.api.config.internal.MapSettings
import org.sonar.api.rule.RuleKey

/** Tests the Scapegoat Sensor */
class ScalastyleSensorSpec
    extends FlatSpec
    with MockitoSugar
    with SensorContextMatchers
    with LoneElement
    with OptionValues {

  val checkstyleReportParser = new TestScalastyleReportParser()
  val scalastyleSensor = new ScalastyleSensor(checkstyleReportParser)

  it should "construct the default report path" in {
    val context = SensorContextTester.create(cwd)
    scalastyleSensor.defaultReportPath(context.config()) shouldBe Paths.get(
      "target",
      "scalastyle-result.xml"
    )
  }

  it should "correctly set descriptor" in {
    val descriptor = new DefaultSensorDescriptor
    scalastyleSensor.describe(descriptor)

    descriptor should not be 'global
    descriptor.name shouldBe "Scalastyle Sensor"
    descriptor.`type` shouldBe InputFile.Type.MAIN
    descriptor.languages.loneElement shouldBe "scala"
    descriptor.ruleRepositories.loneElement shouldBe "sonar-scala-scalastyle"
  }

  it should "get default scalastyle report path, when property is missing" in {
    val reportPath = scalastyleSensor.getReportPath(new MapSettings().asConfig())

    reportPath shouldBe Paths.get("target", "scalastyle-result.xml")
  }

  it should "get scalastyle report path set in sonar properties" in {
    val reportPath = scalastyleSensor.getReportPath(
      new MapSettings().setProperty("sonar.scala.scalastyle.reportPath", "target/report-path").asConfig()
    )

    reportPath shouldBe Paths.get("target", "report-path")
  }

  it should "get a root module file using the filename extracted from the scalastyle report" in {
    val filesystem = new DefaultFileSystem(Paths.get("./"))

    val testFileA = TestInputFileBuilder
      .create("", "src/main/scala/com/mwz/sonar/scala/scalastyle/TestFileA.scala")
      .setLanguage("scala")
      .setType(InputFile.Type.MAIN)
      .build()
    filesystem.add(testFileA)

    val moduleFile =
      scalastyleSensor.getModuleFile("com/mwz/sonar/scala/scalastyle/TestFileA.scala", filesystem)

    moduleFile.value shouldBe testFileA
  }

  it should "get a module file using the filename extracted from the scalastyle report" in {
    val filesystem = new DefaultFileSystem(Paths.get("./module1"))

    val testFileA = TestInputFileBuilder
      .create("module1", "src/main/scala/com/mwz/sonar/scala/scalastyle/TestFileA.scala")
      .setLanguage("scala")
      .setType(InputFile.Type.MAIN)
      .build()
    filesystem.add(testFileA)

    val moduleFile =
      scalastyleSensor.getModuleFile("com/mwz/sonar/scala/scalastyle/TestFileA.scala", filesystem)

    moduleFile.value shouldBe testFileA
  }

  it should "not get a module file if its filename does not match any file" in {
    val filesystem = new DefaultFileSystem(Paths.get("./"))

    val moduleFile =
      scalastyleSensor.getModuleFile("com/mwz/sonar/scala/scalastyle/TestFileA.scala", filesystem)

    moduleFile shouldBe None
  }

  //TODO: make test
  it should "create an issue for each scalastyle report's warning" ignore {
    // create the sensor context
    val sensorContext = SensorContextTester.create(Paths.get("./"))

    // setup the filesystem
    val testFileA = TestInputFileBuilder
      .create("", "src/main/scala/com/mwz/sonar/scala/scalastyle/TestFileA.scala")
      .setLanguage("scala")
      .setType(InputFile.Type.MAIN)
      .setLines(2)
      .setOriginalLineOffsets(Array(0, 51)) // line 1 -> 50 chars, line 2 -> 80 chars
      .setLastValidOffset(131)
      .build()
    sensorContext.fileSystem.add(testFileA)

    val testFileB = TestInputFileBuilder
      .create("", "src/main/scala/com/mwz/sonar/scala/scalastyle/TestFileB.scala")
      .setLanguage("scala")
      .setType(InputFile.Type.MAIN)
      .setLines(3)
      .setOriginalLineOffsets(Array(0, 31, 82)) // line 1 -> 30 chars, line 2 -> 50 chars, line 3 -> 50 chars
      .setLastValidOffset(132)
      .build()
    sensorContext.fileSystem.add(testFileB)

    // setup the active rules
    val activeRulesBuilder = new ActiveRulesBuilder()

    val magicNumberCheckerRuleKey =
      RuleKey.of("sonar-scala-scalastyle", "Magic Number")

    activeRulesBuilder
      .create(magicNumberCheckerRuleKey)
      .setInternalKey("org.scalastyle.scalariform.MagicNumberChecker")
      .activate()

    val fileLineLengthCheckerRuleKey =
      RuleKey.of("sonar-scala-scalastyle", "File line length exceeds 160 characters")

    activeRulesBuilder
      .create(fileLineLengthCheckerRuleKey)
      .setInternalKey("org.scalastyle.file.FileLineLengthChecker")
      .activate()

    val headerMatchesCheckerRuleKey =
      RuleKey.of("sonar-scala-scalastyle", "Header does not match expected text")

    activeRulesBuilder
      .create(headerMatchesCheckerRuleKey)
      .setInternalKey("org.scalastyle.file.HeaderMatchesChecker")
      .activate()

    val publicMethodsHaveTypeCheckerRuleKey =
      RuleKey.of("sonar-scala-scalastyle", "Public method must have explicit type")

    activeRulesBuilder
      .create(publicMethodsHaveTypeCheckerRuleKey)
      .setInternalKey("org.scalastyle.scalariform.PublicMethodsHaveTypeChecker")
      .activate()

    val fieldNamesCheckerRuleKey =
      RuleKey.of(
        "sonar-scala-scalastyle",
        "Field name does not match the regular expression '^[A-Z][A-Za-z]*$'"
      )

    activeRulesBuilder
      .create(fieldNamesCheckerRuleKey)
      .setInternalKey("org.scalastyle.scalariform.FieldNamesChecker")
      .activate()

    println(activeRulesBuilder.build())

    sensorContext.setActiveRules(activeRulesBuilder.build())

    // set the scapegoat report path property
    sensorContext.setSettings(
      new MapSettings()
        .setProperty("sonar.scala.scalastyle.reportPath", "scalastyle-report/two-files-five-warnings.xml")
    )

    // execute the sensor
    scalastyleSensor.execute(sensorContext)

    // validate the sensor behavior
    val testFileAIssueMagicNumber =
      new DefaultIssue().forRule(magicNumberCheckerRuleKey)
    testFileAIssueMagicNumber.at(
      testFileAIssueMagicNumber
        .newLocation()
        .on(testFileA)
        .at(new DefaultTextRange(new DefaultTextPointer(1, 0), new DefaultTextPointer(1, 50)))
        .message(
          "Magic Number"
        )
    )

    val testFileAIssueFileLineLength =
      new DefaultIssue().forRule(fileLineLengthCheckerRuleKey)
    testFileAIssueFileLineLength.at(
      testFileAIssueFileLineLength
        .newLocation()
        .on(testFileA)
        .at(new DefaultTextRange(new DefaultTextPointer(2, 0), new DefaultTextPointer(2, 80)))
        .message(
          "File line length exceeds 160 characters"
        )
    )

    val testFileBIssueHeaderMatches =
      new DefaultIssue().forRule(headerMatchesCheckerRuleKey)
    testFileBIssueHeaderMatches.at(
      testFileBIssueHeaderMatches
        .newLocation()
        .on(testFileB)
        .at(new DefaultTextRange(new DefaultTextPointer(1, 0), new DefaultTextPointer(1, 30)))
        .message(
          "Header does not match expected text"
        )
    )

    val testFileBIssuePublicMethodsHaveTypeChecker =
      new DefaultIssue().forRule(publicMethodsHaveTypeCheckerRuleKey)
    testFileBIssuePublicMethodsHaveTypeChecker.at(
      testFileBIssuePublicMethodsHaveTypeChecker
        .newLocation()
        .on(testFileB)
        .at(new DefaultTextRange(new DefaultTextPointer(2, 0), new DefaultTextPointer(2, 50)))
        .message(
          "Public method must have explicit type"
        )
    )

    val testFileBIssueFieldNames =
      new DefaultIssue().forRule(fieldNamesCheckerRuleKey)
    testFileBIssueFieldNames.at(
      testFileBIssueFieldNames
        .newLocation()
        .on(testFileB)
        .at(new DefaultTextRange(new DefaultTextPointer(3, 0), new DefaultTextPointer(3, 50)))
        .message(
          "Field name does not match the regular expression '^[A-Z][A-Za-z]*$'"
        )
    )

    println("****" + sensorContext.allIssues)
    println(sensorContext.allIssues)

    sensorContext.allIssues should contain theSameElementsAs Seq(
      testFileAIssueMagicNumber,
      testFileAIssueFileLineLength,
      testFileBIssueHeaderMatches,
      testFileBIssuePublicMethodsHaveTypeChecker,
      testFileBIssueFieldNames
    )
  }

  it should "not report any issue if the report is empty" in {
    // create the sensor context
    val sensorContext = SensorContextTester.create(Paths.get("./"))

    // setup the filesystem
    val testFileA = TestInputFileBuilder
      .create("", "src/main/scala/com/mwz/sonar/scala/scalastyle/TestFileA.scala")
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
      RuleKey.of("sonar-scala-scalastyle", "Empty case class")
    activeRulesBuilder
      .create(emptyClassRuleKey)
      .setInternalKey("com.sksamuel.scapegoat.inspections.EmptyCaseClass")
      .activate()

    sensorContext.setActiveRules(activeRulesBuilder.build())

    // set the scapegoat report path property
    sensorContext.setSettings(
      new MapSettings()
        .setProperty("sonar.scala.scalastyle.reportPath", "scalastyle-report/no-warnings.xml")
    )

    // execute the sensor
    scalastyleSensor.execute(sensorContext)

    // validate the sensor behavior
    sensorContext.allIssues shouldBe empty
  }

  it should "not report an issue if its rule is not active" in {
    // create the sensor context
    val sensorContext = SensorContextTester.create(Paths.get("./"))

    // setup the filesystem
    val testFileA = TestInputFileBuilder
      .create("", "src/main/scala/com/mwz/sonar/scala/scalastyle/TestFileA.scala")
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
        .setProperty("sonar.scala.scalastyle.reportPath", "scalastyle-report/one-file-one-warning.xml")
    )

    // execute the sensor
    scalastyleSensor.execute(sensorContext)

    // validate the sensor behavior
    sensorContext.allIssues shouldBe empty
  }

  //TODO: make test
  it should "report module issues" ignore {
    // create the sensor context
    val sensorContext = SensorContextTester.create(Paths.get("./"))
    val filesystem = new DefaultFileSystem(Paths.get("./module1"))
    sensorContext.setFileSystem(filesystem)

    // setup the filesystem
    val testFileA = TestInputFileBuilder
      .create("module1", "src/main/scala/com/mwz/sonar/scala/scalastyle/TestFileA.scala")
      .setLanguage("scala")
      .setType(InputFile.Type.MAIN)
      .setLines(2)
      .setOriginalLineOffsets(Array(0, 51)) // line 1 -> 50 chars, line 2 -> 80 chars
      .setLastValidOffset(131)
      .build()
    sensorContext.fileSystem.add(testFileA)

    // setup the active rules
    val activeRulesBuilder = new ActiveRulesBuilder()

    val cyclomaticComplexityRuleKey =
      RuleKey.of("sonar-scala-scalastyle", "Empty case class")
    activeRulesBuilder
      .create(cyclomaticComplexityRuleKey)
      .setInternalKey("org.scalastyle.scalariform.CyclomaticComplexityChecker")
      .activate()

    sensorContext.setActiveRules(activeRulesBuilder.build())

    // set the scapegoat report path property
    sensorContext.setSettings(
      new MapSettings()
        .setProperty("sonar.scala.scalastyle.reportPath", "scalastyle-report/one-file-one-warning.xml")
    )

    // execute the sensor
    scalastyleSensor.execute(sensorContext)

    // validate the sensor behavior
    val testFileAIssueCyclomaticComplexity =
      new DefaultIssue().forRule(cyclomaticComplexityRuleKey)

    testFileAIssueCyclomaticComplexity.at(
      testFileAIssueCyclomaticComplexity
        .newLocation()
        .on(testFileA)
        .at(new DefaultTextRange(new DefaultTextPointer(1, 0), new DefaultTextPointer(1, 50)))
        .message(
          "Cyclomatic complexity of 11 exceeds max of 10"
        )
    )

    sensorContext.allIssues should contain theSameElementsAs Seq(
      testFileAIssueCyclomaticComplexity
    )
  }
}

/** Mock of the ScapegoatReportParser */
final class TestScalastyleReportParser extends CheckstyleReportParser {
  override def parse(reportPath: Path): Map[String, Seq[CheckstyleIssue]] = reportPath.toString match {
    case "scalastyle-report/no-warnings.xml" =>
      Map()
    case "scalastyle-report/one-file-one-warning.xml" =>
      Map(
        "com.mwz.sonar.scala.scalastyle.TestFileA.scala" -> Seq(
          CheckstyleIssue(
            line = 1,
            column = None,
            inspectionClass = "org.scalastyle.scalariform.CyclomaticComplexityChecker",
            severity = "warning",
            text = "Cyclomatic complexity of 11 exceeds max of 10",
            snippet = ""
          )
        )
      )
    case "module1/scalastyle-report/one-file-one-warning.xml" =>
      Map(
        "com.mwz.sonar.scala.scalastyle.TestFileA.scala" -> Seq(
          CheckstyleIssue(
            line = 15,
            column = Some(15),
            inspectionClass = "org.scalastyle.scalariform.CyclomaticComplexityChecker",
            severity = "warning",
            text = "Cyclomatic complexity of 11 exceeds max of 10",
            snippet = ""
          )
        )
      )
    case "scalastyle-report/two-files-five-warnings.xml" =>
      Map(
        "com/mwz/sonar/scala/scalastyle/TestFileA.scala" -> Seq(
          CheckstyleIssue(
            line = 39,
            column = Some(37),
            inspectionClass = "org.scalastyle.scalariform.MagicNumberChecker",
            severity = "warning",
            text = "Magic Number",
            snippet = ""
          ),
          CheckstyleIssue(
            line = 77,
            column = None,
            inspectionClass = "org.scalastyle.file.FileLineLengthChecker",
            severity = "warning",
            text = "File line length exceeds 160 characters",
            snippet = ""
          )
        ),
        "com/mwz/sonar/scala/scalastyle/TestFileB.scala" -> Seq(
          CheckstyleIssue(
            line = 1,
            column = None,
            inspectionClass = "org.scalastyle.file.HeaderMatchesChecker",
            severity = "warning",
            text = "Header does not match expected text",
            snippet = ""
          ),
          CheckstyleIssue(
            line = 20,
            column = Some(8),
            inspectionClass = "org.scalastyle.scalariform.PublicMethodsHaveTypeChecker",
            severity = "warning",
            text = "Public method must have explicit type",
            snippet = ""
          ),
          CheckstyleIssue(
            line = 22,
            column = Some(16),
            inspectionClass = "org.scalastyle.scalariform.FieldNamesChecker",
            severity = "warning",
            text = "Field name does not match the regular expression '^[A-Z][A-Za-z]*$'",
            snippet = ""
          )
        )
      )
  }
}
