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

import org.sonar.api.batch.{SensorContext, CoverageExtension, Sensor}
import org.sonar.api.measures.{CoverageMeasuresBuilder, CoreMetrics, Measure}
import com.buransky.plugins.scoverage.measure.ScalaMetrics
import com.buransky.plugins.scoverage._
import com.buransky.plugins.scoverage.resource.{SingleDirectory, ScalaFile}
import scala.collection.JavaConversions._
import org.sonar.api.resources.{Project, Resource}
import com.buransky.plugins.scoverage.util.LogUtil
import com.buransky.plugins.scoverage.CoveredStatement
import com.buransky.plugins.scoverage.FileStatementCoverage
import com.buransky.plugins.scoverage.DirectoryStatementCoverage
import com.buransky.plugins.scoverage.language.Scala
import org.sonar.api.scan.filesystem.{PathResolver, ModuleFileSystem}
import org.sonar.api.config.Settings
import org.slf4j.LoggerFactory
import com.buransky.plugins.scoverage.xml.XmlScoverageReportParser

/**
 *  Main sensor for importing Scoverage report to Sonar.
 *
 * @author Rado Buransky
 */
class ScoverageSensor(settings: Settings, pathResolver: PathResolver, moduleFileSystem: ModuleFileSystem, scala: Scala)
  extends Sensor with CoverageExtension {
  private val log = LoggerFactory.getLogger(classOf[ScoverageSensor])
  protected val SCOVERAGE_REPORT_PATH_PROPERTY = "sonar.scoverage.reportPath"
  protected lazy val scoverageReportParser: ScoverageReportParser = XmlScoverageReportParser()

  override def shouldExecuteOnProject(project: Project): Boolean =
    project.getAnalysisType.isDynamic(true) && (scala.getKey == project.getLanguageKey)

  override def analyse(project: Project, context: SensorContext) {
    scoverageReportPath match {
      case Some(reportPath) =>
        // Single-module project
        processProject(scoverageReportParser.parse(reportPath), project, context)

      case None =>
        // Multi-module project has report path set for each module individually
        analyseMultiModuleProject(project, context)
    }
  }

  override val toString = getClass.getSimpleName

  private lazy val scoverageReportPath: Option[String] = {
    settings.getString(SCOVERAGE_REPORT_PATH_PROPERTY) match {
      case null => None
      case path: String =>
        pathResolver.relativeFile(moduleFileSystem.baseDir, path) match {
          case report: java.io.File if !report.exists || !report.isFile =>
            log.error(LogUtil.f("Report not found at {}"), report)
            None

          case report: java.io.File => Some(report.getAbsolutePath)
        }
    }
  }

  private def analyseMultiModuleProject(project: Project, context: SensorContext) {
    project.isModule match {
      case true => log.warn(LogUtil.f("Report path not set for " + project.name + " module! [" +
        project.name + "." + SCOVERAGE_REPORT_PATH_PROPERTY + "]"))
      case _ =>
        // Compute overall statement coverage from submodules
        val totalStatementCount = project.getModules.map(analyseStatementCountForModule(_, context)).sum
        val coveredStatementCount = project.getModules.map(analyseCoveredStatementCountForModule(_, context)).sum

        if (totalStatementCount > 0) {
          // Convert to percentage
          val overall = (coveredStatementCount.toDouble / totalStatementCount.toDouble) * 100.0

            // Set overall statement coverage
          context.saveMeasure(project, createStatementCoverage(overall))

          log.info(LogUtil.f("Overall statement coverage is " + ("%1.2f" format overall)))
        }
    }
  }

  private def analyseCoveredStatementCountForModule(module: Project, context: SensorContext): Long = {
    // Aggregate modules
    context.getMeasure(module, ScalaMetrics.coveredStatements) match {
      case null =>
        log.debug(LogUtil.f("Module has no statement coverage. [" + module.name + "]"))
        0
      case moduleCoveredStatementCount: Measure =>
        log.debug(LogUtil.f("Covered statement count for " + module.name + " module. [" +
          moduleCoveredStatementCount.getValue + "]"))

        moduleCoveredStatementCount.getValue.toLong
    }
  }

  private def analyseStatementCountForModule(module: Project, context: SensorContext): Long = {
    // Aggregate modules
    context.getMeasure(module, CoreMetrics.STATEMENTS) match {
      case null =>
        log.debug(LogUtil.f("Module has no number of statements. [" + module.name + "]"))
        0

      case moduleStatementCount: Measure =>
        log.debug(LogUtil.f("Statement count for " + module.name + " module. [" +
          moduleStatementCount.getValue + "]"))

        moduleStatementCount.getValue.toLong
    }
  }

  private def processProject(projectCoverage: ProjectStatementCoverage, project: Project, context: SensorContext) {
    // Save measures
    saveMeasures(context, project, projectCoverage)

    log.info(LogUtil.f("Statement coverage for " + project.getKey + " is " + ("%1.2f" format projectCoverage.rate)))

    // Process children
    processChildren(projectCoverage.children, context, "")
  }

  private def processDirectory(directoryCoverage: DirectoryStatementCoverage, context: SensorContext,
                               parentDirectory: String) {
    val currentDirectory = appendFilePath(parentDirectory, directoryCoverage.name)

    // Save measures
    saveMeasures(context, new SingleDirectory(currentDirectory, scala), directoryCoverage)

    // Process children
    processChildren(directoryCoverage.children, context, currentDirectory)
  }

  private def processFile(fileCoverage: FileStatementCoverage, context: SensorContext, directory: String) {
    val scalaSourceFile = new ScalaFile(appendFilePath(directory, fileCoverage.name), scala)

    // Save measures
    saveMeasures(context, scalaSourceFile, fileCoverage)

    // Save line coverage. This is needed just for source code highlighting.
    saveLineCoverage(fileCoverage.statements, scalaSourceFile, context)
  }

  private def saveMeasures(context: SensorContext, resource: Resource, statementCoverage: StatementCoverage) {
    context.saveMeasure(resource, createStatementCoverage(statementCoverage.rate))
    context.saveMeasure(resource, createStatementCount(statementCoverage.statementCount))
    context.saveMeasure(resource, createCoveredStatementCount(statementCoverage.coveredStatementsCount))

    log.debug(LogUtil.f("Save measures [" + statementCoverage.rate + ", " + statementCoverage.statementCount +
      ", " + statementCoverage.coveredStatementsCount + ", " + resource.getKey + "]"))
  }

  private def saveLineCoverage(coveredStatements: Iterable[CoveredStatement], scalaSourceFile: ScalaFile,
                               context: SensorContext) {
    // Convert statements to lines
    val coveredLines = StatementCoverage.statementCoverageToLineCoverage(coveredStatements)

    // Set line hits
    val coverage = CoverageMeasuresBuilder.create()
    coveredLines.foreach { coveredLine =>
      coverage.setHits(coveredLine.line, coveredLine.hitCount)
    }

    // Save measures
    coverage.createMeasures().toList.foreach(context.saveMeasure(scalaSourceFile, _))
  }

  private def processChildren(children: Iterable[StatementCoverage], context: SensorContext, directory: String) {
    children.foreach(processChild(_, context, directory))
  }

  private def processChild(dirOrFile: StatementCoverage, context: SensorContext, directory: String) {
    dirOrFile match {
      case dir: DirectoryStatementCoverage => processDirectory(dir, context, directory)
      case file: FileStatementCoverage => processFile(file, context, directory)
      case _ => throw new IllegalStateException("Not a file or directory coverage! [" +
        dirOrFile.getClass.getName + "]")
    }
  }

  private def createStatementCoverage(rate: Double): Measure = new Measure(ScalaMetrics.statementCoverage, rate)

  private def createStatementCount(statements: Int): Measure = new Measure(CoreMetrics.STATEMENTS, statements)

  private def createCoveredStatementCount(coveredStatements: Int): Measure =
    new Measure(ScalaMetrics.coveredStatements, coveredStatements);

  private def appendFilePath(src: String, name: String) = {
    val result = if (!src.isEmpty) src + java.io.File.separator else ""
    result + name
  }
}
