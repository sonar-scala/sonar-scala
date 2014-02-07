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
package com.buransky.plugins.scoverage.sensor;

import com.buransky.plugins.scoverage.*;
import com.buransky.plugins.scoverage.language.Scala;
import com.buransky.plugins.scoverage.measure.ScalaMetrics;
import com.buransky.plugins.scoverage.resource.ScalaFile;
import com.buransky.plugins.scoverage.xml.XmlScoverageReportParser$;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.CoverageExtension;
import org.sonar.api.batch.Sensor;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.config.Settings;
import org.sonar.api.measures.CoverageMeasuresBuilder;
import org.sonar.api.measures.Measure;
import org.sonar.api.resources.*;
import org.sonar.api.scan.filesystem.ModuleFileSystem;
import scala.collection.JavaConversions;
import org.sonar.api.scan.filesystem.PathResolver;

/**
 *  Main sensor for importing Scoverage report to Sonar.
 *
 * @author Rado Buransky
 */
public class ScoverageSensor implements Sensor, CoverageExtension {
    private static final Logger log = LoggerFactory.getLogger(ScoverageSensor.class);
    private final ScoverageReportParser scoverageReportParser;
    private final Settings settings;
    private final PathResolver pathResolver;
    private final ModuleFileSystem moduleFileSystem;

    private static final String SCOVERAGE_REPORT_PATH_PROPERTY = "sonar.scoverage.reportPath";

    public ScoverageSensor(Settings settings, PathResolver pathResolver, ModuleFileSystem fileSystem) {
        this(XmlScoverageReportParser$.MODULE$.apply(), settings, pathResolver, fileSystem);
    }

    public ScoverageSensor(ScoverageReportParser scoverageReportParser, Settings settings,
                           PathResolver pathResolver, ModuleFileSystem moduleFileSystem) {
        this.scoverageReportParser = scoverageReportParser;
        this.settings = settings;
        this.pathResolver = pathResolver;
        this.moduleFileSystem = moduleFileSystem;
    }

    public boolean shouldExecuteOnProject(Project project) {
        return project.getAnalysisType().isDynamic(true) && Scala.INSTANCE.getKey().equals(project.getLanguageKey());
    }

    public void analyse(Project project, SensorContext context) {
        String reportPath = getScoverageReportPath();
        if (reportPath != null)
            processProject(scoverageReportParser.parse(reportPath), project, context);
    }

    @Override
    public String toString() {
        return "Scoverage sensor";
    }

    private String getScoverageReportPath() {
        String path = settings.getString(SCOVERAGE_REPORT_PATH_PROPERTY);
        if (path == null) {
            log.error("Scoverage report path not set! [" + SCOVERAGE_REPORT_PATH_PROPERTY + "]");
            return null;
        }
        java.io.File report = pathResolver.relativeFile(moduleFileSystem.baseDir(), path);
        if (!report.exists() || !report.isFile()) {
            log.error("Scoverage report not found at {}", report);
            return null;
        }

        return report.getAbsolutePath();
    }

    private void processProject(ProjectStatementCoverage projectCoverage,
                                Project project, SensorContext context) {
        // Save project measure
        context.saveMeasure(project, createStatementCoverage(projectCoverage.rate()));
        log("Project coverage = " + projectCoverage.rate());

        // Process children
        processChildren(projectCoverage.children(), context, "");
    }

    private void processDirectory(DirectoryStatementCoverage directoryCoverage, SensorContext context,
                                  String parentDirectory) {
        String currentDirectory = appendFilePath(parentDirectory, directoryCoverage.name());

        com.buransky.plugins.scoverage.resource.SingleDirectory directory = new com.buransky.plugins.scoverage.resource.SingleDirectory(currentDirectory);
        context.saveMeasure(directory, createStatementCoverage(directoryCoverage.rate()));

        log("Process directory [" + directory.getKey() + ", " + directoryCoverage.rate() + "]");

        // Process children
        processChildren(directoryCoverage.children(), context, currentDirectory);
    }

    private void processFile(FileStatementCoverage fileCoverage, SensorContext context,
                             String directory) {
        ScalaFile scalaSourcefile = new ScalaFile(appendFilePath(directory, fileCoverage.name()));
        context.saveMeasure(scalaSourcefile, createStatementCoverage(fileCoverage.rate()));

        log("Process file [" + scalaSourcefile.getKey() + ", " + fileCoverage.rate() + "]");

        // Save line coverage. This is needed just for source code highlighting.
        saveLineCoverage(fileCoverage.statements(), scalaSourcefile, context);
    }

    private void saveLineCoverage(scala.collection.Iterable<CoveredStatement> coveredStatements,
                                  ScalaFile scalaSourcefile, SensorContext context) {
        // Convert statements to lines
        scala.collection.Iterable<CoveredLine> coveredLines =
                StatementCoverage$.MODULE$.statementCoverageToLineCoverage(coveredStatements);

        // Set line hits
        CoverageMeasuresBuilder coverage = CoverageMeasuresBuilder.create();
        for (CoveredLine coveredLine: JavaConversions.asJavaIterable(coveredLines)) {
            coverage.setHits(coveredLine.line(), coveredLine.hitCount());
        }

        // Save measures
        for (Measure measure : coverage.createMeasures()) {
            context.saveMeasure(scalaSourcefile, measure);
        }
    }

    private void processChildren(scala.collection.Iterable<StatementCoverage> children, SensorContext context,
                                 String directory) {
        // Process children
        for (StatementCoverage child: JavaConversions.asJavaIterable(children)) {
            processChild(child, context, directory);
        }
    }

    private void processChild(StatementCoverage dirOrFile, SensorContext context,
                              String directory) {
        if (dirOrFile instanceof DirectoryStatementCoverage) {
            processDirectory((DirectoryStatementCoverage) dirOrFile, context, directory);
        }
        else {
            if (dirOrFile instanceof FileStatementCoverage) {
                processFile((FileStatementCoverage) dirOrFile, context, directory);
            }
            else {
                throw new IllegalStateException("Not a file or directory coverage! [" +
                        dirOrFile.getClass().getName() + "]");
            }
        }
    }

    private Measure createStatementCoverage(Double rate) {
        return new Measure(ScalaMetrics.STATEMENT_COVERAGE, rate);
    }

    private String appendFilePath(String src, String name) {
        String result;
        if (!src.isEmpty())
            result = src + java.io.File.separator;
        else
            result = "";

        return result + name;
    }

    private static void log(String message) {
        log.info("[Scoverage] " + message);
    }

}
