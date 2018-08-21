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

import java.io.InputStream

import com.typesafe.config.{Config, ConfigFactory}
import org.scalastyle.{Level, _}
import sbt.Keys._
import sbt._

import scala.meta._
import scala.xml.{Node, NodeSeq, XML}

/**
 * An SBT task that generates a managed source file with all Scalastyle inspections.
 * */
object ScalastyleInspectionsGenerator {

  val generatorTask = Def.task {
    val log = streams.value.log
    log.info("Generating scalastyle inspections file.")

    val c = classOf[MainConfig]
    val definitionStream: InputStream = c.getResourceAsStream("/scalastyle_definition.xml")
    val documentationStream: InputStream = c.getResourceAsStream("/scalastyle_documentation.xml")
    val config: Config = ConfigFactory.load(c.getClassLoader)

    val inspections: NodeSeq = XML.load(definitionStream) \\ "checker"
    val docs: NodeSeq = XML.load(documentationStream) \\ "scalastyle-documentation" \ "check"
    val docsMap: Map[String, Node] = docs.map(node => node \@ "id" -> node).toMap

    // Collect Scalastyle inspections from config files.
    val generatedInspections: Seq[ScalastyleInspection] = for {
      inspection <- inspections
      clazz = (inspection \ "@class").text
      id = inspection \@ "id"
      cfg = config.getConfig(id)
      label = cfg.getString("label")
      description = cfg.getString("description")
      doc = docsMap.get(id)
      extraDescription = doc.flatMap(node => (node \ "extra-description").map(_.text.trim).headOption)
      justification = doc.map(node => (node \ "justification").text.trim)
      defaultLevel = Level(inspection \@ "defaultLevel")
      // TODO: Add parameters
    } yield ScalastyleInspection(clazz, id, label, description, extraDescription, justification, defaultLevel)

    // Load the template file from ./project/ScalastyleInspections.scala.
    val projectDir = new File(baseDirectory.value, "project")
    val templateFile = new File(projectDir, "ScalastyleInspections.scala")
    val source: Source = templateFile.parse[Source].get

    // TODO: Substitute the AllInspections list with generatedInspections.
    val stringified: Seq[String] = generatedInspections.collect {
      case inspection =>
        // TODO: Do we want to format the text with code examples?
        val extraDescription = inspection.extraDescription.map(s => "\"\"\"" + s + "\"\"\"")
        val justification = inspection.justification.map(s => "\"\"\"" + s + "\"\"\"")

        s"""
           |ScalastyleInspection(
           |  clazz = "${inspection.clazz}",
           |  id = "${inspection.id}",
           |  label = "${inspection.label}",
           |  description = "${inspection.description}",
           |  extraDescription = $extraDescription,
           |  justification = $justification,
           |  defaultLevel = ${inspection.defaultLevel}
           |)
         """.stripMargin
    }

    // Transform the template file.
    val term: Term = stringified.toString.parse[Term].get
    val transformed: Tree = source.transform {
      case q"val AllInspections: $tpe = $expr" =>
        q"val AllInspections: $tpe = $term"
    }

    // Save the new file to the managed sources dir.
    val scalastyleInspectionsFile = (sourceManaged in Compile).value / "scalastyle" / "inspections.scala"
    IO.write(scalastyleInspectionsFile, transformed.syntax)

    Seq(scalastyleInspectionsFile)
  }
}
