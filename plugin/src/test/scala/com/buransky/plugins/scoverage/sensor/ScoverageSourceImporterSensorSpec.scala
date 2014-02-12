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

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{Matchers, FlatSpec}
import org.sonar.api.scan.filesystem.ModuleFileSystem
import org.scalatest.mock.MockitoSugar
import com.buransky.plugins.scoverage.language.Scala
import org.sonar.api.resources.{AbstractLanguage, Project}
import org.mockito.Mockito._

@RunWith(classOf[JUnitRunner])
class ScoverageSourceImporterSensorSpec extends FlatSpec with Matchers with MockitoSugar {
  behavior of "shouldExecuteOnProject"

  it should "succeed for Scala project" in new ScoverageSourceImporterSensorScope {
    // Setup
    val project = mock[Project]
    when(project.getLanguage).thenReturn(scala)

    // Execute & asser
    shouldExecuteOnProject(project) should equal(true)

    verify(project, times(2)).getLanguage
  }

  it should "fail for Java project" in new ScoverageSourceImporterSensorScope {
    // Setup
    val java = new AbstractLanguage("java", "Java") {
      val getFileSuffixes = Array("java")
    }
    val project = mock[Project]
    when(project.getLanguage).thenReturn(java)

    // Execute & asser
    shouldExecuteOnProject(project) should equal(false)

    verify(project, times(2)).getLanguage
  }

  class ScoverageSourceImporterSensorScope extends {
    val scala = new Scala
    val moduleFileSystem = mock[ModuleFileSystem]
  } with ScoverageSourceImporterSensor(moduleFileSystem, scala)
}
