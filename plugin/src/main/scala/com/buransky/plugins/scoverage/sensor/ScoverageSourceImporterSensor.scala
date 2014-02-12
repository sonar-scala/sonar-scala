/*
 * Sonar Scoverage Plugin
 * Copyright (C) 2013 Rado Buransky
 * dev@sonar.codehaus.org
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package com.buransky.plugins.scoverage.sensor

import org.sonar.api.batch.{SensorContext, Phase, Sensor}
import org.sonar.api.batch.Phase.Name
import org.slf4j.LoggerFactory
import org.sonar.api.scan.filesystem.{FileType, FileQuery, ModuleFileSystem}
import org.sonar.api.resources.{File, Project}
import com.buransky.plugins.scoverage.language.Scala
import scala.collection.JavaConversions._
import java.io.IOException
import com.buransky.plugins.scoverage.resource.ScalaFile
import org.apache.commons.io.FileUtils

/**
 * Imports Scala source code files to Sonar.
 *
 * @author Rado Buransky
 */
@Phase(name = Name.PRE)
class ScoverageSourceImporterSensor(moduleFileSystem: ModuleFileSystem, scala: Scala) extends Sensor {
  private val log = LoggerFactory.getLogger(classOf[ScoverageSourceImporterSensor])

  override def shouldExecuteOnProject(project: Project) =
    (project.getLanguage != null) && (project.getLanguage.getKey == scala.getKey)

  override def analyse(project: Project, sensorContext: SensorContext) = {
    val charset = moduleFileSystem.sourceCharset().toString()
    val query = FileQuery.on(FileType.SOURCE).onLanguage(scala.getKey)
    moduleFileSystem.files(query).toList.foreach { sourceFile =>
      addFileToSonar(project, sensorContext, sourceFile, charset)
    }
  }

  override val toString = "Scoverage source importer"

  private def addFileToSonar(project: Project, sensorContext: SensorContext, sourceFile: java.io.File,
                             charset: String) = {
    try {
      val source = FileUtils.readFileToString(sourceFile, charset)
      val key = File.fromIOFile(sourceFile, project).getKey()
      val resource = new ScalaFile(key, scala)

      sensorContext.index(resource)
      sensorContext.saveSource(resource, source)
    } catch {
      case ioe: IOException =>  log.error("Could not read the file: " + sourceFile.getAbsolutePath, ioe)
    }
  }
}
