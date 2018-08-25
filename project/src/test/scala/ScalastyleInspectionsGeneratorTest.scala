import com.typesafe.config.ConfigFactory
import org.scalastyle._
import org.scalatest._

import scala.io.Source._
import scala.meta._
import scala.xml._

class ScalastyleInspectionsGeneratorTest extends FlatSpec with Matchers {

  "extractInspections" should "successfully extract the inspections" in {
    val inspections = XML.load(fromResource("scalastyle/definition.xml").bufferedReader) \\ "checker"
    //XML.load(this.getClass.getResourceAsStream("scalastyle/definition.xml")) \\ "checker"
    val docs = XML.load(fromResource("scalastyle/documentation.xml").bufferedReader) \\ "check"
    // XML.load(this.getClass.getResourceAsStream("scalastyle/documentation.xml")) \\ "check"
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
        List(Param("maxFileLength", IntegerType, "1500"))
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
        List(Param("allowed", IntegerType, "1"), Param("ignoreRegex", StringType, "^\"\"$"))
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
          Param("ignoreRegex", StringType, "^$"),
          Param("ignoreTokenTypes", StringType, "^$"),
          Param("ignoreOverride", BooleanType, "false"),
          Param("indentStyle", StringType, "anydoc")
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
        List(Param("maxFileLength", IntegerType, "1500"))
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
        |   params = List(Param(name = "maxFileLength", typ = IntegerType, default = "1500"))
        | )
        |)
        |}
      """.stripMargin

    val result = ScalastyleInspectionsGenerator.transform(source, inspections)

    result.structure == expected.parse[Source].get.structure shouldBe true
  }
}
