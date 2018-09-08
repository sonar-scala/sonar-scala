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
import java.nio.file.{Path, Paths}
import com.sksamuel.scapegoat.Inspection
import io.github.classgraph.ClassGraph
import sbt.Keys._
import sbt._

import scala.meta._
import scala.collection.JavaConverters._

/** SBT Task that generates a managed file with all scapegoat inspections */
object ScapegoatInspectionsGenerator {

  /** Scapegoat inspections that won't be included in the generated file */
  val BlacklistedInspections = Set(
    "com.sksamuel.scapegoat.inspections.collections.FilterDotSizeComparison", // Not implemented yet
    "com.sksamuel.scapegoat.inspections.collections.ListTail" // Not implemented yet
  )

  val generatorTask = Def.task {
    val log = streams.value.log
    log.info("Generating the scapegoat inspections file.")

    // Load the template file.
    val templateFile = Paths
      .get(
        baseDirectory.value.toString,
        "project",
        "src",
        "main",
        "resources",
        "ScapegoatInspections.scala"
      )

    val allScapegoatInspections = extractInspections()
    val stringifiedScapegoatIsnpections = stringifyInspections(allScapegoatInspections)
    val transformed = fillTemplate(templateFile, stringifiedScapegoatIsnpections)

    val scapegoatInspectionsFile = (sourceManaged in Compile).value / "scapegoat" / "inspections.scala"
    IO.write(scapegoatInspectionsFile, transformed)

    Seq(scapegoatInspectionsFile)
  }

  /** Returns all scapegoat inspections, except the ones that should be ignored */
  def extractInspections(): List[(String, Inspection)] =
    // We need to override the scanner class loader,
    // so it can cast the loaded classes
    // from class[_] to class[Inspection]
    new ClassGraph()
      .overrideClassLoaders(classOf[Inspection].getClassLoader)
      .whitelistPackages("com.sksamuel.scapegoat.inspections")
      .scan()
      .getSubclasses("com.sksamuel.scapegoat.Inspection")
      .loadClasses(classOf[Inspection])
      .asScala
      .toList
      .collect {
        case clazz if !BlacklistedInspections.contains(clazz.getName) =>
          (clazz.getName, clazz.newInstance())
      }

  /** Stringifies a list of scapegoat inspections */
  def stringifyInspections(scapegoatInspections: List[(String, Inspection)]): List[String] =
    scapegoatInspections map {
      case (inspectionClassName, inspection) =>
        s"""ScapegoatInspection(
           |  id = "$inspectionClassName",
           |  name = "${inspection.text}",
           |  description = "${inspection.explanation.getOrElse("No Explanation")}",
           |  defaultLevel = Level.${inspection.defaultLevel}
           |)""".stripMargin
    }

  /** Fill the template file */
  def fillTemplate(templateFile: Path, stringified: List[String]): String = {
    val term: Term = stringified.toString.parse[Term].get
    val source: Source = templateFile.parse[Source].get
    val transformed: Tree = source.transform {
      case q"val AllInspections: $tpe = $expr" =>
        q"val AllInspections: $tpe = $term"
    }
    transformed.syntax
  }
}
