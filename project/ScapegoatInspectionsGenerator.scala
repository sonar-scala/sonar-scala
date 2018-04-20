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
package com.mwz.sonar.scala.scapegoat

import com.sksamuel.scapegoat.Inspection
import com.sksamuel.scapegoat.Levels
import io.github.lukehutch.fastclasspathscanner.FastClasspathScanner
import io.github.lukehutch.fastclasspathscanner.matchprocessor.SubclassMatchProcessor
import scala.collection.mutable
import sbt._
import Keys._

/** SBT Task that generates a managed file with all scapegoat inspections */
object ScapegoatInspectionsGenerator {
  val generatorTask = Def.task {
    val log = streams.value.log
    log.info("Generating the scapegoat inspections file")

    val inspectionClass = classOf[Inspection]
    val inspections = mutable.ListBuffer.empty[(String, Inspection)]

    // We need to override the scanner class loader so it can find the scapegoat inspections
    log.debug("[ScapegoatInspectionsGenerator] Scanning the classpath")
    val fastCPScanner = new FastClasspathScanner(inspectionClass.getPackage.getName)
    fastCPScanner
      .overrideClassLoaders(inspectionClass.getClassLoader)
      .matchSubclassesOf(
        inspectionClass,
        new SubclassMatchProcessor[Inspection] {
          override def processMatch(matchingClass: Class[_ <: Inspection]): Unit = {
            val inspectionClassName = matchingClass.getName
            log.debug(s"[ScapegoatInspectionsGenerator] Found the inspection: ${inspectionClassName}")
            inspections += (inspectionClassName -> matchingClass.newInstance())
          }
        }
      ).scan()

    val AllScapegoatInspections = inspections.toList map {
      case (inspectionClassName, inspection) =>
        s"""ScapegoatInspection(
           |  id = "${inspectionClassName}",
           |  name = "${inspection.text}",
           |  description = "${inspection.explanation.getOrElse("No Explanation")}",
           |  defaultLevel = Level.${inspection.defaultLevel}
           |),""".stripMargin
    }

    val lines = List(
      "package com.mwz.sonar.scala.scapegoat.inspections",
      "sealed trait Level",
      "object Level {",
      "case object Error extends Level",
      "case object Warning extends Level",
      "case object Info extends Level",
      "}",
      "final case class ScapegoatInspection (id: String, name: String, description: String, defaultLevel: Level)",
      "object ScapegoatInspection {",
      "val AllScapegoatInspections: List[ScapegoatInspection] = List("
    ) ++ AllScapegoatInspections ++ List(")", "}")

    log.debug("[ScapegoatInspectionsGenerator] Saving the scapegoat inspections file")
    val scapegoatInspectionsFile = (sourceManaged in Compile).value / "scapegoat" / "inspections.scala"
    IO.writeLines(scapegoatInspectionsFile, lines)
    Seq(scapegoatInspectionsFile)
  }
}
