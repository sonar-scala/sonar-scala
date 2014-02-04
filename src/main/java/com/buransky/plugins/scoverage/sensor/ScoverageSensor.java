package com.buransky.plugins.scoverage.sensor;

import com.buransky.plugins.scoverage.language.Scala;
import com.buransky.plugins.scoverage.language.ScalaFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.CoverageExtension;
import org.sonar.api.batch.Sensor;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.measures.CoreMetrics;
import org.sonar.api.measures.CoverageMeasuresBuilder;
import org.sonar.api.measures.Measure;
import org.sonar.api.resources.*;

import java.util.HashMap;
import java.util.Map;

public class ScoverageSensor implements Sensor, CoverageExtension {

    private static final Logger LOG = LoggerFactory.getLogger(ScoverageSensor.class);

    public boolean shouldExecuteOnProject(Project project) {
        return project.getAnalysisType().isDynamic(true) && Scala.INSTANCE.getKey().equals(project.getLanguageKey());
    }

    public void analyse(Project project, SensorContext context) {
        parseFakeReport(project, context);
    }

    @Override
    public String toString() {
        return "Scala ScoverageSensor";
    }

    private void parseFakeReport(Project project, final SensorContext context) {
        ProjectFileSystem fileSystem = project.getFileSystem();

        HashMap<String, Directory> dirs = new HashMap<String, Directory>();
        for (InputFile sourceFile : fileSystem.mainFiles("scala")) {
            LOG.info("[ScoverageSensor] Set coverage for [" + sourceFile.getRelativePath() + "]");
            File scalaSourcefile = ScalaFile.fromIOFile(sourceFile.getFile(), project);

            CoverageMeasuresBuilder coverage = CoverageMeasuresBuilder.create();
            coverage.setHits(1, 1);
            coverage.setHits(2, 2);
            coverage.setHits(3, 3);
            coverage.setHits(4, 0);
            coverage.setHits(5, 0);
            coverage.setHits(6, 0);
            coverage.setHits(7, 0);
            coverage.setHits(8, 1);
            coverage.setHits(9, 0);
            coverage.setHits(10, 2);
            coverage.setHits(11, 0);
            coverage.setHits(12, 3);
            coverage.setHits(13, 0);

            for (Measure measure : coverage.createMeasures()) {
                context.saveMeasure(scalaSourcefile, measure);
            }

            context.saveMeasure(scalaSourcefile, new Measure(CoreMetrics.COVERAGE, 51.4));
            dirs.put(scalaSourcefile.getParent().getKey(), scalaSourcefile.getParent());
        }

        for (Map.Entry<String, Directory> e: dirs.entrySet()) {
            LOG.info("[ScoverageSensor] Set dir coverage for [" + e.getKey() + "]");
            context.saveMeasure(e.getValue(), new Measure(CoreMetrics.COVERAGE, 23.4));
        }

        context.saveMeasure(project, new Measure(CoreMetrics.COVERAGE, 12.3));
    }

}
