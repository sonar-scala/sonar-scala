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

import java.io.InputStream
import java.nio.file.Paths

import com.mwz.sonar.scala.scalastyle._
import com.typesafe.config.{Config, ConfigFactory}
import org.scalastyle.{Level, _}
import sbt.Keys._
import sbt._

import scala.meta._
import scala.xml.{Node, NodeSeq, XML}

/**
 * An SBT task that generates a managed source file with all Scalastyle inspections.
 */
object ScalastyleInspectionsGenerator {

  val generatorTask = Def.task {
    val log = streams.value.log
    log.info("Generating Scalastyle inspections file.")

    val c = classOf[MainConfig]
    val definitionStream: InputStream = c.getResourceAsStream("/scalastyle_definition.xml")
    val documentationStream: InputStream = c.getResourceAsStream("/scalastyle_documentation.xml")
    val inspections: NodeSeq = XML.load(definitionStream) \\ "checker"
    val docs: NodeSeq = XML.load(documentationStream) \\ "scalastyle-documentation" \ "check"
    val config: Config = ConfigFactory.load(c.getClassLoader)

    // Collect Scalastyle inspections from config files.
    val generatedInspections: Seq[ScalastyleInspection] = extractInspections(inspections, docs, config)

    // Load the template file.
    val templateFile = Paths
      .get(
        baseDirectory.value.toString,
        "project",
        "src",
        "main",
        "scala",
        "ScalastyleInspections.scala"
      )

    // Substitute AllInspections with generated inspections.
    val source: Source = templateFile.parse[Source].get
    val transformed = transform(source, generatedInspections)

    // Save the new file to the managed sources dir.
    val scalastyleInspectionsFile = (sourceManaged in Compile).value / "scalastyle" / "inspections.scala"
    IO.write(scalastyleInspectionsFile, transformed.syntax)

    Seq(scalastyleInspectionsFile)
  }

  /**
   * Extract Scalastyle inspections from config files.
   */
  def extractInspections(
    inspections: NodeSeq,
    docs: NodeSeq,
    config: Config
  ): Seq[ScalastyleInspection] = {
    val docsMap: Map[String, Node] = docs.map(node => node \@ "id" -> node).toMap
    for {
      inspection <- inspections
      clazz = inspection \@ "class"
      id = inspection \@ "id"
      cfg = config.getConfig(id)
      label = cfg.getString("label")
      description = cfg.getString("description")
      doc = docsMap.get(id)
      extraDescription = doc.flatMap(node => (node \ "extra-description").map(_.text.trim).headOption)
      justification = doc.map(node => (node \ "justification").text.trim)
      defaultLevel = Level(inspection \@ "defaultLevel")
      params = (inspection \\ "parameter").map { param =>
        val name = param \@ "name"
        val typ = ParameterType(param \@ "type")
        val label = cfg.getString(s"$name.label")
        val description = cfg.getString(s"$name.description")
        val default = param \@ "default"
        Param(name, typ, label, description, default)
      }
    } yield
      ScalastyleInspection(
        clazz,
        id,
        label,
        description,
        extraDescription,
        justification,
        defaultLevel,
        params
      )
  }

  /**
   * Fill the template with generated inspections.
   */
  def transform(source: Tree, inspections: Seq[ScalastyleInspection]): Tree = {
    val stringified: Seq[String] = inspections.collect {
      case inspection =>
        // Is there a better way of embedding multi-line text?
        val extraDescription = inspection.extraDescription.map(s => "\"\"\"" + s + "\"\"\"")
        val justification = inspection.justification.map(s => "\"\"\"" + s + "\"\"\"")
        val params = inspection.params.map { p =>
          s"""
             |Param(
             |  name = "${p.name}",
             |  typ = ${p.typ},
             |  label = "${p.label}",
             |  description = "${p.description}",
             |  default = \"\"\"${p.default}\"\"\"
             |)
           """.stripMargin
        }

        // It doesn't seem to be straightforward to automatically convert a collection
        // into a tree using scalameta, so I'm turning it into a String so it can be parsed,
        // which is easier than constructing the tree manually.
        // Totally doable with shapeless though, but it would be a bit of an overkill in this case.
        s"""
           |ScalastyleInspection(
           |  clazz = "${inspection.clazz}",
           |  id = "${inspection.id}",
           |  label = "${inspection.label}",
           |  description = "${inspection.description}",
           |  extraDescription = $extraDescription,
           |  justification = $justification,
           |  defaultLevel = ${inspection.defaultLevel},
           |  params = ${params.toString.parse[Term].get.syntax}
           |)
         """.stripMargin
    }

    // Transform the template file.
    val term: Term = stringified.toString.parse[Term].get
    source.transform {
      case q"val AllInspections: $tpe = $expr" =>
        q"val AllInspections: $tpe = $term"
    }
  }
}
