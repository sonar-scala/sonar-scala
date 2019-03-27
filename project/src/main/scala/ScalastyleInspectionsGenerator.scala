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

import java.nio.file.Paths

import com.typesafe.config.{Config, ConfigFactory}
import org.scalastyle.{MainConfig, ParameterType, Level}
import sbt.Keys._
import sbt._

import scala.meta._
import scala.xml.{Node, NodeSeq, XML}

/**
 * An SBT task that generates a managed source file with all Scalastyle inspections.
 */
object ScalastyleInspectionsGenerator {
  /** Project relative path to the Scalastyle Inspections template file. */
  private final val ScalastyleInspectionsTemplateFilePath = List(
    "project",
    "src",
    "main",
    "resources",
    "ScalastyleInspections.scala"
  )

  val generatorTask = Def.task {
    val log = streams.value.log
    val cachedFun =
      FileFunction
        .cached(
          cacheBaseDirectory = streams.value.cacheDirectory / "scalastyle",
          inStyle = FilesInfo.hash,
          outStyle = FilesInfo.exists
        ) { (in: Set[File]) =>
          log.info("Generating Scalastyle inspections file.")

          val c = classOf[MainConfig]
          val definitionStream = c.getResourceAsStream("/scalastyle_definition.xml")
          val documentationStream = c.getResourceAsStream("/scalastyle_documentation.xml")
          val inspections = XML.load(definitionStream) \\ "checker"
          val docs = XML.load(documentationStream) \\ "scalastyle-documentation" \ "check"
          val config = ConfigFactory.load(c.getClassLoader)

          // Collect Scalastyle inspections from config files.
          val generatedInspections = extractInspections(inspections, docs, config)

          // Load the template file.
          val templateFile = Paths.get(baseDirectory.value.toString, ScalastyleInspectionsTemplateFilePath: _*)

          // Substitute AllInspections with generated inspections.
          val source = templateFile.parse[Source].get
          val transformed = transform(source, generatedInspections)

          // Save the new file to the managed sources dir.
          val scalastyleInspectionsFile = (sourceManaged in Compile).value / "scalastyle" / "inspections.scala"
          IO.write(scalastyleInspectionsFile, transformed.syntax)
          Set(scalastyleInspectionsFile)
        }

    cachedFun(Set(file(ScalastyleInspectionsTemplateFilePath.mkString("/")))).toSeq
  }

  /**
   * Extract Scalastyle inspections from config files.
   */
  def extractInspections(
    inspections: NodeSeq,
    docs: NodeSeq,
    config: Config
  ): Seq[String] = {
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
        s"""Param(
           |  name = "$name",
           |  typ = $typ,
           |  label = "$label",
           |  description = "$description",
           |  default = \"\"\"$default\"\"\"
           |)""".stripMargin
      }
    } yield s"""ScalastyleInspection(
               |  clazz = "$clazz",
               |  id = "$id",
               |  label = "$label",
               |  description = "$description",
               |  extraDescription = ${extraDescription.map(text => "\"\"\"" + text + "\"\"\"")},
               |  justification = ${justification.map(text => "\"\"\"" + text + "\"\"\"")},
               |  defaultLevel = $defaultLevel,
               |  params = ${params.mkString("List(", ",", ")")}
               |)""".stripMargin
  }

  /**
   * Fill the template with generated inspections.
   */
  def transform(source: Tree, stringified: Seq[String]): Tree = {
    val term: Term = stringified.toString.parse[Term].get
    source.transform {
      case q"val AllInspections: $tpe = $expr" =>
        q"val AllInspections: $tpe = $term"
    }
  }
}
