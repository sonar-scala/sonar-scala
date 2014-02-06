package com.buransky.plugins.scoverage.sensor;

import com.buransky.plugins.scoverage.*;
import com.buransky.plugins.scoverage.language.Scala;
import com.buransky.plugins.scoverage.measure.ScalaMetrics;
import com.buransky.plugins.scoverage.resource.ScalaDirectory;
import com.buransky.plugins.scoverage.resource.ScalaFile;
import com.buransky.plugins.scoverage.xml.XmlScoverageReportParser$;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.CoverageExtension;
import org.sonar.api.batch.Sensor;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.measures.CoreMetrics;
import org.sonar.api.measures.CoverageMeasuresBuilder;
import org.sonar.api.measures.Measure;
import org.sonar.api.resources.*;
import scala.collection.JavaConversions;

import java.util.HashMap;
import java.util.Map;

public class ScoverageSensor implements Sensor, CoverageExtension {
    private static final Logger log = LoggerFactory.getLogger(ScoverageSensor.class);
    private final ScoverageReportParser scoverageReportParser;

    public ScoverageSensor() {
        this(XmlScoverageReportParser$.MODULE$.apply(""));
    }

    public ScoverageSensor(ScoverageReportParser scoverageReportParser) {
        this.scoverageReportParser = scoverageReportParser;
    }

    public boolean shouldExecuteOnProject(Project project) {
        return project.getAnalysisType().isDynamic(true) && Scala.INSTANCE.getKey().equals(project.getLanguageKey());
    }

    public void analyse(Project project, SensorContext context) {
        processProject(scoverageReportParser.parse(), project, context);
        //parseFakeReport(project, context);
    }

    @Override
    public String toString() {
        return "Scoverage sensor";
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

        ScalaDirectory directory = new ScalaDirectory(currentDirectory);
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

    private void parseFakeReport(Project project, final SensorContext context) {
        ProjectFileSystem fileSystem = project.getFileSystem();

        HashMap<String, Directory> dirs = new HashMap<String, Directory>();
        for (InputFile sourceFile : fileSystem.mainFiles("scala")) {
            ScalaFile scalaSourcefile = new ScalaFile(File.fromIOFile(sourceFile.getFile(), project).getKey());

            context.saveMeasure(scalaSourcefile, new Measure(CoreMetrics.COVERAGE, 51.4));
            log("Process fake file [" + scalaSourcefile.getKey() + "]");

//            CoverageMeasuresBuilder coverage = CoverageMeasuresBuilder.create();
//            coverage.setHits(1, 1);
//            coverage.setHits(2, 2);
//            coverage.setHits(3, 3);
//            coverage.setHits(4, 0);
//            coverage.setHits(5, 0);
//            coverage.setHits(6, 0);
//            coverage.setHits(7, 0);
//            coverage.setHits(8, 1);
//            coverage.setHits(9, 0);
//            coverage.setHits(10, 2);
//            coverage.setHits(11, 0);
//            coverage.setHits(12, 3);
//            coverage.setHits(13, 0);
//
//            for (Measure measure : coverage.createMeasures()) {
//                context.saveMeasure(scalaSourcefile, measure);
//            }

            dirs.put(scalaSourcefile.getParent().getKey(), scalaSourcefile.getParent());
        }

        for (Map.Entry<String, Directory> e: dirs.entrySet()) {
            log.info("[ScoverageSensor] Set dir coverage for [" + e.getKey() + "]");
            context.saveMeasure(e.getValue(), new Measure(CoreMetrics.COVERAGE, 23.4));
        }

        context.saveMeasure(project, new Measure(CoreMetrics.COVERAGE, 12.3));
    }

}
