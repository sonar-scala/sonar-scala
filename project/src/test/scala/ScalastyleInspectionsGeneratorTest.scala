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

import com.mwz.sonar.scala.scalastyle._
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

    val expected = List(
      ScalastyleInspection(
        "org.scalastyle.file.FileTabChecker",
        "line.contains.tab",
        "Line contains Tab",
        "Check that there are no tabs in a file",
        None,
        Some("Some say that tabs are evil."),
        WarningLevel,
        List()
      ),
      ScalastyleInspection(
        "org.scalastyle.file.FileLengthChecker",
        "file.size.limit",
        "File length",
        "Check the number of lines in a file",
        None,
        Some("Files which are too long can be hard to read and understand."),
        WarningLevel,
        List(
          Param(
            "maxFileLength",
            IntegerType,
            "Maximum file length",
            "Maximum number of lines in a file",
            "1500"
          )
        )
      ),
      ScalastyleInspection(
        "org.scalastyle.scalariform.MultipleStringLiteralsChecker",
        "multiple.string.literals",
        "Multiple string literals",
        "Checks that a string literal does not appear multiple times",
        None,
        Some(
          "Code duplication makes maintenance more difficult, so it can be better to replace the multiple occurrences with a constant."
        ),
        WarningLevel,
        List(
          Param(
            "allowed",
            IntegerType,
            "Maximum occurences allowed",
            "Maximum number of occurences allowed",
            "1"
          ),
          Param(
            "ignoreRegex",
            StringType,
            "Ignore regular expression",
            "Regular expression to ignore",
            "^\"\"$"
          )
        )
      ),
      ScalastyleInspection(
        "org.scalastyle.scalariform.ScalaDocChecker",
        "scaladoc",
        "Missing or badly formed ScalaDoc: {0}",
        "Checks that the ScalaDoc on documentable members is well-formed",
        Some(
          """Ignore tokens is a comma separated string that may include the following : PatDefOrDcl (variables), TmplDef (classes, traits), TypeDefOrDcl (type definitions), FunDefOrDcl (functions)
            |            Supported indentation styles are "scaladoc" (for ScalaDoc-style comments, with two spaces before the asterisk), "javadoc" (for JavaDoc-style comments, with a single space before the asterisk) or "anydoc" to support any style (any number of spaces before the asterisk). For backwards compatibility, if left empty, "anydoc" will be assumed.""".stripMargin
        ),
        Some("Scaladoc is generally considered a good thing. Within reason."),
        WarningLevel,
        List(
          Param(
            "ignoreRegex",
            StringType,
            "Regular expression",
            "Class names matching this regular expression will be ignored",
            "^$"
          ),
          Param(
            "ignoreTokenTypes",
            StringType,
            "Comma Separated String",
            "Include the following to ignore : PatDefOrDcl (variables), TmplDef (classes, traits), TypeDefOrDcl (type definitions), FunDefOrDcl (functions)",
            "^$"
          ),
          Param(
            "ignoreOverride",
            BooleanType,
            "Ignore override",
            "If set to true, methods which have the override modifier are ignored",
            "false"
          ),
          Param(
            "indentStyle",
            StringType,
            "Force indent style",
            "Possible values: scaladoc - 2 spaces before *, javadoc - 1 space before *",
            "anydoc"
          )
        )
      )
    )

    val result = ScalastyleInspectionsGenerator.extractInspections(inspections, docs, conf)

    result shouldBe expected
  }

  "transform" should "successfully transform the code template" in {
    val template =
      """
        |object ScalastyleInspections { 
        | val AllInspections: Seq[ScalastyleInspection] = ???
        |}
    """.stripMargin

    val source = template.parse[Source].get

    val inspections = List(
      ScalastyleInspection(
        "org.scalastyle.file.FileTabChecker",
        "line.contains.tab",
        "Line contains Tab",
        "Check that there are no tabs in a file",
        None,
        Some("Some say that tabs are evil."),
        WarningLevel,
        List()
      ),
      ScalastyleInspection(
        "org.scalastyle.file.FileLengthChecker",
        "file.size.limit",
        "File length",
        "Check the number of lines in a file",
        None,
        Some("Files which are too long can be hard to read and understand."),
        WarningLevel,
        List(
          Param(
            "maxFileLength",
            IntegerType,
            "Maximum file length",
            "Maximum number of lines in a file",
            "1500"
          )
        )
      )
    )

    val expected =
      """
        |object ScalastyleInspections {
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

    val result = ScalastyleInspectionsGenerator.transform(source, inspections)

    result.structure shouldBe expected.parse[Source].get.structure
  }
}
