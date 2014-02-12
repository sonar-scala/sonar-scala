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
import org.scalatest.mock.MockitoSugar
import org.sonar.api.resources.Project
import org.mockito.Mockito._
import com.buransky.plugins.scoverage.language.Scala
import org.sonar.api.scan.filesystem.{PathResolver, ModuleFileSystem}
import org.sonar.api.config.Settings
import org.sonar.api.resources.Project.AnalysisType
import org.sonar.api.batch.SensorContext
import com.buransky.plugins.scoverage.{ProjectStatementCoverage, ScoverageReportParser}

@RunWith(classOf[JUnitRunner])
class ScoverageSensorSpec extends FlatSpec with Matchers with MockitoSugar {
  behavior of "shouldExecuteOnProject"

  it should "succeed for Scala project" in new ShouldExecuteOnProject {
    checkShouldExecuteOnProject("scala", true)
  }

  it should "fail for Java project" in new ShouldExecuteOnProject {
    checkShouldExecuteOnProject("java", false)
  }

  class ShouldExecuteOnProject extends ScoverageSensorScope {
    protected def checkShouldExecuteOnProject(language: String, expectedResult: Boolean) {
      // Setup
      val project = mock[Project]
      when(project.getAnalysisType).thenReturn(AnalysisType.DYNAMIC)
      when(project.getLanguageKey).thenReturn(language)

      // Execute & asser
      shouldExecuteOnProject(project) should equal(expectedResult)

      verify(project, times(1)).getAnalysisType
      verify(project, times(1)).getLanguageKey

    }
  }

  behavior of "analyse for single project"

  it should "set 0% coverage for a project without children" in new AnalyseScoverageSensorScope {
    // Setup
    val pathToScoverageReport = "#path-to-scoverage-report#"
    val reportAbsolutePath = "#report-absolute-path#"
    val projectStatementCoverage = ProjectStatementCoverage("project-name", Nil)
    val reportFile = mock[java.io.File]
    val moduleBaseDir = mock[java.io.File]
    when(reportFile.exists).thenReturn(true)
    when(reportFile.isFile).thenReturn(true)
    when(reportFile.getAbsolutePath).thenReturn(reportAbsolutePath)
    when(settings.getString(SCOVERAGE_REPORT_PATH_PROPERTY)).thenReturn(pathToScoverageReport)
    when(moduleFileSystem.baseDir).thenReturn(moduleBaseDir)
    when(pathResolver.relativeFile(moduleBaseDir, pathToScoverageReport)).thenReturn(reportFile)
    when(scoverageReportParser.parse(reportAbsolutePath)).thenReturn(projectStatementCoverage)

    // Execute
    analyse(project, context)
  }

  class AnalyseScoverageSensorScope extends ScoverageSensorScope {
    val project = mock[Project]
    val context = new TestSensorContext

    override protected lazy val scoverageReportParser = mock[ScoverageReportParser]
  }

  class ScoverageSensorScope extends {
    val scala = new Scala
    val settings = mock[Settings]
    val pathResolver = mock[PathResolver]
    val moduleFileSystem = mock[ModuleFileSystem]
  } with ScoverageSensor(settings, pathResolver, moduleFileSystem, scala)

}
