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

import com.buransky.plugins.scoverage.measure.ScalaMetrics
import com.buransky.plugins.scoverage.pathcleaner.{BruteForceSequenceMatcher, PathSanitizer}
import com.buransky.plugins.scoverage.util.LogUtil
import com.buransky.plugins.scoverage.xml.XmlScoverageReportParser
import com.buransky.plugins.scoverage.{CoveredStatement, DirectoryStatementCoverage, FileStatementCoverage, _}
import org.sonar.api.batch.fs.{FileSystem, InputFile, InputPath}
import org.sonar.api.batch.{CoverageExtension, Sensor, SensorContext}
import org.sonar.api.config.Settings
import org.sonar.api.measures.{CoverageMeasuresBuilder, Measure}
import org.sonar.api.resources.{Project, Resource}
import org.sonar.api.scan.filesystem.PathResolver
import org.sonar.api.utils.log.Loggers
import org.sonar.plugins.scala.Scala

import scala.collection.JavaConversions._

/**
 *  Main sensor for importing Scoverage report to Sonar.
 *
 * @author Rado Buransky
 */
class ScoverageSensor(settings: Settings, pathResolver: PathResolver, fileSystem: FileSystem)
  extends Sensor with CoverageExtension {
  private val log = Loggers.get(classOf[ScoverageSensor])
  protected val SCOVERAGE_REPORT_PATH_PROPERTY = "sonar.scoverage.reportPath"
  protected lazy val scoverageReportParser: ScoverageReportParser = XmlScoverageReportParser()

  override def shouldExecuteOnProject(project: Project): Boolean = fileSystem.languages().contains(Scala.KEY)

  override def analyse(project: Project, context: SensorContext) {
    scoverageReportPath match {
      case Some(reportPath) =>
        // Single-module project
        val srcOption = Option(settings.getString("sonar.sources"))
        val sonarSources = srcOption match {
          case Some(src) => src
          case None => {
            log.warn(s"could not find settings key sonar.sources assuming src/main/scala.")
            "src/main/scala"
          }
        }
        val pathSanitizer = createPathSanitizer(sonarSources)
        processProject(scoverageReportParser.parse(reportPath, pathSanitizer), project, context, sonarSources)

      case None =>
        // Multi-module project has report path set for each module individually
        analyseMultiModuleProject(project, context)
    }
  }

  override val toString = getClass.getSimpleName

  protected def createPathSanitizer(sonarSources: String): PathSanitizer
    = new BruteForceSequenceMatcher(fileSystem.baseDir(), sonarSources)
  
  private lazy val scoverageReportPath: Option[String] = {
    settings.getString(SCOVERAGE_REPORT_PATH_PROPERTY) match {
      case null => None
      case path: String =>
        pathResolver.relativeFile(fileSystem.baseDir, path) match {
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
      case moduleCoveredStatementCount: Measure[_] =>
        log.debug(LogUtil.f("Covered statement count for " + module.name + " module. [" +
          moduleCoveredStatementCount.getValue + "]"))

        moduleCoveredStatementCount.getValue.toLong
    }
  }

  private def analyseStatementCountForModule(module: Project, context: SensorContext): Long = {
    // Aggregate modules
    context.getMeasure(module, ScalaMetrics.totalStatements) match {
      case null =>
        log.debug(LogUtil.f("Module has no number of statements. [" + module.name + "]"))
        0

      case moduleStatementCount: Measure[_] =>
        log.debug(LogUtil.f("Statement count for " + module.name + " module. [" +
          moduleStatementCount.getValue + "]"))

        moduleStatementCount.getValue.toLong
    }
  }

  private def processProject(projectCoverage: ProjectStatementCoverage, project: Project, context: SensorContext, sonarSources: String) {
    // Save measures
    saveMeasures(context, project, projectCoverage)

    log.info(LogUtil.f("Statement coverage for " + project.getKey + " is " + ("%1.2f" format projectCoverage.rate)))

    // Process children
    sonarSources.split(",").foreach(subdir => processChildren(projectCoverage.children, context, subdir))
  }

  private def processDirectory(directoryCoverage: DirectoryStatementCoverage, context: SensorContext, parentDirectory: String) {
    // save measures if any
    if (directoryCoverage.statementCount > 0) {
      val path = appendFilePath(parentDirectory, directoryCoverage.name)

      getResource(path, context, false) match {
        case Some(srcDir) => {
          // Save directory measures
          saveMeasures(context, srcDir, directoryCoverage)
        }
        case None =>
      }
    }
    // Process children
    processChildren(directoryCoverage.children, context, appendFilePath(parentDirectory, directoryCoverage.name))
  }

  private def processFile(fileCoverage: FileStatementCoverage, context: SensorContext, directory: String) {
    val path = appendFilePath(directory, fileCoverage.name)

    getResource(path, context, true) match {
      case Some(scalaSourceFile) => {
        // Save measures
        saveMeasures(context, scalaSourceFile, fileCoverage)
        // Save line coverage. This is needed just for source code highlighting.
        saveLineCoverage(fileCoverage.statements, scalaSourceFile, context)
      }
      case None =>
    }
  }

  private def getResource(path: String, context: SensorContext, isFile: Boolean): Option[Resource] = {
    
    val inputOption: Option[InputPath] = if (isFile) {
      val p = fileSystem.predicates()
      Option(fileSystem.inputFile(p.and(
        p.or(p.hasPath(path),p.hasRelativePath(path)),
        p.hasLanguage(Scala.KEY),
        p.hasType(InputFile.Type.MAIN))))
    } else {
      Option(fileSystem.inputDir(pathResolver.relativeFile(fileSystem.baseDir(), path)))
    }
  
    inputOption match {
      case Some(path: InputPath) =>
        Some(context.getResource(path))
      case None => {
        log.warn(s"File or directory not found in file system! ${path}")
        None
      }
    }
  }

  private def saveMeasures(context: SensorContext, resource: Resource, statementCoverage: StatementCoverage) {
    context.saveMeasure(resource, createStatementCoverage(statementCoverage.rate))
    context.saveMeasure(resource, createStatementCount(statementCoverage.statementCount))
    context.saveMeasure(resource, createCoveredStatementCount(statementCoverage.coveredStatementsCount))

    log.debug(LogUtil.f("Save measures [" + statementCoverage.rate + ", " + statementCoverage.statementCount +
      ", " + statementCoverage.coveredStatementsCount + ", " + resource.getKey + "]"))
  }

  private def saveLineCoverage(coveredStatements: Iterable[CoveredStatement], resource: Resource,
                               context: SensorContext) {
    // Convert statements to lines
    val coveredLines = StatementCoverage.statementCoverageToLineCoverage(coveredStatements)

    // Set line hits
    val coverage = CoverageMeasuresBuilder.create()
      coveredLines.foreach { coveredLine =>
      coverage.setHits(coveredLine.line, coveredLine.hitCount)
    }

    // Save measures
    coverage.createMeasures().toList.foreach(context.saveMeasure(resource, _))
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

  private def createStatementCoverage[T <: Serializable](rate: Double): Measure[T] =
    new Measure[T](ScalaMetrics.statementCoverage, rate)

  private def createStatementCount[T <: Serializable](statements: Int): Measure[T] =
    new Measure(ScalaMetrics.totalStatements, statements.toDouble, 0)

  private def createCoveredStatementCount[T <: Serializable](coveredStatements: Int): Measure[T] =
    new Measure(ScalaMetrics.coveredStatements, coveredStatements.toDouble, 0)

  private def appendFilePath(src: String, name: String) = {
    val result = src match {
      case java.io.File.separator => java.io.File.separator
      case empty if empty.isEmpty => ""
      case other => other + java.io.File.separator
    }

    result + name
  }
}
