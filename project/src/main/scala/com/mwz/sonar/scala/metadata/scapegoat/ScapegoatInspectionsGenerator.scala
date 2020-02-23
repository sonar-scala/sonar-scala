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

package com.mwz.sonar.scala.metadata.scapegoat

import java.nio.file.{Path, Paths}

import com.sksamuel.scapegoat.{Inspection, ScapegoatConfig}
import sbt.Keys._
import sbt._

import scala.meta._

/** SBT Task that generates a managed file with all scapegoat inspections */
object ScapegoatInspectionsGenerator {

  val generatorTask = Def.task {
    val log = streams.value.log
    log.info("Generating Scapegoat inspections file.")

    // Load the template file.
    val templateFile: Path = Paths
      .get(
        baseDirectory.value.toString,
        "project",
        "src",
        "main",
        "resources",
        "ScapegoatInspections.scala"
      )

    val allScapegoatInspections: Seq[(String, Inspection)] = extractInspections()
    val stringifiedScapegoatInspections: Seq[String] = stringifyInspections(allScapegoatInspections)
    val transformed: Tree = fillTemplate(templateFile.parse[Source].get, stringifiedScapegoatInspections)

    val scapegoatInspectionsFile: File =
      (sourceManaged in Compile).value / "metadata" / "scapegoat" / "inspections.scala"
    IO.write(scapegoatInspectionsFile, transformed.syntax)

    Seq(scapegoatInspectionsFile)
  }

  /**
   * Returns all scapegoat inspections, except the ones that should be ignored
   */
  def extractInspections(): Seq[(String, Inspection)] =
    ScapegoatConfig.inspections.map(inspection => (inspection.getClass.getName, inspection))

  /** Stringifies a list of scapegoat inspections */
  def stringifyInspections(scapegoatInspections: Seq[(String, Inspection)]): Seq[String] =
    scapegoatInspections map {
      case (inspectionClassName, inspection) =>
        s"""ScapegoatInspection(
           |  id = "$inspectionClassName",
           |  name = "${inspection.text}",
           |  description = ${inspection.explanation.map(text => s""""$text"""")},
           |  defaultLevel = Level.${inspection.defaultLevel}
           |)""".stripMargin
    }

  /** Fill the template file */
  def fillTemplate(template: Source, stringified: Seq[String]): Tree = {
    val term: Term = stringified.toString.parse[Term].get
    template.transform {
      case q"val AllInspections: $tpe = $expr" =>
        q"val AllInspections: $tpe = $term"
    }
  }
}
