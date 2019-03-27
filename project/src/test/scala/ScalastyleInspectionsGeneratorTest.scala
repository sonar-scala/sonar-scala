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

import com.typesafe.config.ConfigFactory
import org.scalastyle._
import org.scalatest._

import scala.io.Source.fromResource
import scala.meta._
import scala.xml._

class ScalastyleInspectionsGeneratorTest extends FlatSpec with Matchers {

  "extractInspections" should "successfully extract the inspections" in {
    val inspections = XML.load(fromResource("scalastyle/definition.xml").bufferedReader()) \\ "checker"
    val docs = XML.load(fromResource("scalastyle/documentation.xml").bufferedReader()) \\ "check"
    val conf = ConfigFactory.parseResources("scalastyle/config.conf")

    val expected =
      List(
        """ScalastyleInspection(
          |  clazz = "org.scalastyle.file.FileTabChecker",
          |  id = "line.contains.tab",
          |  label = "Line contains Tab",
          |  description = "Check that there are no tabs in a file",
          |  extraDescription = None,
          |  justification = Some("Some say that tabs are evil."),
          |  defaultLevel = WarningLevel,
          |  params = List()
          |)""".stripMargin,
        """ScalastyleInspection(
          |  clazz = "org.scalastyle.file.FileLengthChecker",
          |  id = "file.size.limit",
          |  label = "File length",
          |  description = "Check the number of lines in a file",
          |  extraDescription = None,
          |  justification = Some("Files which are too long can be hard to read and understand."),
          |  defaultLevel = WarningLevel,
          |  params = List(
          |    Param(
          |      name = "maxFileLength",
          |      typ = IntegerType,
          |      label = "Maximum file length",
          |      description = "Maximum number of lines in a file",
          |      default = "1500"
          |    )
          |  )
          |)""".stripMargin,
        """ScalastyleInspection(
          |  clazz = "org.scalastyle.scalariform.MultipleStringLiteralsChecker",
          |  id = "multiple.string.literals",
          |  label = "Multiple string literals",
          |  description = "Checks that a string literal does not appear multiple times",
          |  extraDescription = None,
          |  justification = Some("Code duplication makes maintenance more difficult, so it can be better to replace the multiple occurrences with a constant."),
          |  defaultLevel = WarningLevel,
          |  params = List(
          |    Param(
          |      name = "allowed",
          |      typ = IntegerType,
          |      label = "Maximum occurences allowed",
          |      description = "Maximum number of occurences allowed",
          |      default = "1"
          |    ),
          |    Param(
          |      name = "ignoreRegex",
          |      typ = StringType,
          |      label = "Ignore regular expression",
          |      description = "Regular expression to ignore",
          |      default = "^\"\"$"
          |    )
          |  )
          |)""".stripMargin,
        """ScalastyleInspection(
          |  clazz = "org.scalastyle.scalariform.ScalaDocChecker",
          |  id = "scaladoc",
          |  label = "Missing or badly formed ScalaDoc: {0}",
          |  description = "Checks that the ScalaDoc on documentable members is well-formed",
          |  extraDescription = Some(
          |       "Ignore tokens is a comma separated string that may include the following : PatDefOrDcl (variables), TmplDef (classes, traits), TypeDefOrDcl (type definitions), FunDefOrDcl (functions)\n            Supported indentation styles are \"scaladoc\" (for ScalaDoc-style comments, with two spaces before the asterisk), \"javadoc\" (for JavaDoc-style comments, with a single space before the asterisk) or \"anydoc\" to support any style (any number of spaces before the asterisk). For backwards compatibility, if left empty, \"anydoc\" will be assumed."
          |  ),
          |  justification = Some("Scaladoc is generally considered a good thing. Within reason."),
          |  defaultLevel = WarningLevel,
          |  params = List(
          |    Param(
          |      name = "ignoreRegex",
          |      typ = StringType,
          |      label = "Regular expression",
          |      description = "Class names matching this regular expression will be ignored",
          |      default = "^$"
          |    ),
          |    Param(
          |      name = "ignoreTokenTypes",
          |      typ = StringType,
          |      label = "Comma Separated String",
          |      description = "Include the following to ignore : PatDefOrDcl (variables), TmplDef (classes, traits), TypeDefOrDcl (type definitions), FunDefOrDcl (functions)",
          |      default = "^$"
          |    ),
          |    Param(
          |      name = "ignoreOverride",
          |      typ = BooleanType,
          |      label = "Ignore override",
          |      description = "If set to true, methods which have the override modifier are ignored",
          |      default = "false"
          |    ),
          |    Param(
          |      name = "indentStyle",
          |      typ = StringType,
          |      label = "Force indent style",
          |      description = "Possible values: scaladoc - 2 spaces before *, javadoc - 1 space before *",
          |      default = "anydoc"
          |    )
          |  )
          |)""".stripMargin
      )

    val result = ScalastyleInspectionsGenerator.extractInspections(inspections, docs, conf)

    result.toString.parse[Term].get.structure shouldBe expected.toString.parse[Term].get.structure
  }

  "transform" should "successfully transform the code template" in {
    val template =
      """
        |object ScalastyleInspections {
        | val AllInspections: Seq[ScalastyleInspection] = ???
        |}
    """.stripMargin

    val source = template.parse[Source].get

    val stringifiedScalastyleInspections = List(
      """ScalastyleInspection(
        |  clazz = "org.scalastyle.file.FileTabChecker",
        |  id = "line.contains.tab",
        |  label = "Line contains Tab",
        |  description = "Check that there are no tabs in a file",
        |  extraDescription =None,
        |  justification = Some("Some say that tabs are evil."),
        |  defaultLevel = WarningLevel,
        |  params = List()
        |)""".stripMargin,
      """ScalastyleInspection(
        |  clazz = "org.scalastyle.file.FileLengthChecker",
        |  id = "file.size.limit",
        |  label = "File length",
        |  description = "Check the number of lines in a file",
        |  extraDescription = None,
        |  justification = Some("Files which are too long can be hard to read and understand."),
        |  defaultLevel = WarningLevel,
        |  params = List(
        |    Param(
        |      name = "maxFileLength",
        |      typ = IntegerType,
        |      label = "Maximum file length",
        |      description = "Maximum number of lines in a file",
        |      default = "1500"
        |    )
        |  )
        |)""".stripMargin
    )

    val expected =
      """object ScalastyleInspections {
        |val AllInspections: Seq[ScalastyleInspection] = List(
        | ScalastyleInspection(
        |   clazz = "org.scalastyle.file.FileTabChecker",
        |   id = "line.contains.tab",
        |   label = "Line contains Tab",
        |   description = "Check that there are no tabs in a file",
        |   extraDescription = None,
        |   justification = Some("Some say that tabs are evil."),
        |   defaultLevel = WarningLevel,
        |   params = List()
        | ),
        | ScalastyleInspection(
        |   clazz = "org.scalastyle.file.FileLengthChecker",
        |   id = "file.size.limit",
        |   label = "File length",
        |   description = "Check the number of lines in a file",
        |   extraDescription = None,
        |   justification = Some("Files which are too long can be hard to read and understand."),
        |   defaultLevel = WarningLevel,
        |   params = List(
        |     Param(
        |       name = "maxFileLength",
        |       typ = IntegerType,
        |       label = "Maximum file length",
        |       description = "Maximum number of lines in a file",
        |       default = "1500"
        |     )
        |   )
        | )
        |)
        |}
      """.stripMargin

    val result = ScalastyleInspectionsGenerator.transform(source, stringifiedScalastyleInspections)

    result.structure shouldBe expected.parse[Source].get.structure
  }
}
