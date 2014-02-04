/*
 * Sonar Scala Plugin
 * Copyright (C) 2011 - 2013 All contributors
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
package com.buransky.plugins.scala.sensor;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.buransky.plugins.scala.language.*;
import com.buransky.plugins.scala.metrics.CommentsAnalyzer;
import com.buransky.plugins.scala.metrics.LinesAnalyzer;
import com.buransky.plugins.scala.util.StringUtils;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.measures.CoreMetrics;
import org.sonar.api.resources.Directory;
import org.sonar.api.resources.InputFile;
import org.sonar.api.resources.Project;
import org.sonar.api.resources.ProjectFileSystem;
import org.sonar.plugins.scala.compiler.Lexer;
import org.sonar.plugins.scala.metrics.ComplexityCalculator;
import org.sonar.plugins.scala.metrics.FunctionCounter;
import org.sonar.plugins.scala.metrics.PublicApiCounter;
import org.sonar.plugins.scala.metrics.StatementCounter;
import org.sonar.plugins.scala.metrics.TypeCounter;
import org.sonar.plugins.scala.util.MetricDistribution;

/**
 * This is the main sensor of the Scala plugin. It gathers all results
 * of the computation of base metrics for all Scala resources.
 *
 * @author Felix Müller
 * @since 0.1
 */
public class BaseMetricsSensor extends AbstractScalaSensor {

    private static final Logger LOGGER = LoggerFactory.getLogger(BaseMetricsSensor.class);

    public BaseMetricsSensor(Scala scala) {
        super(scala);
    }

    public void analyse(Project project, SensorContext sensorContext) {
        final ProjectFileSystem fileSystem = project.getFileSystem();
        final String charset = fileSystem.getSourceCharset().toString();
        final Set<Directory> directories = new HashSet<Directory>();

        MetricDistribution complexityOfClasses = null;
        MetricDistribution complexityOfFunctions = null;

        for (InputFile inputFile : fileSystem.mainFiles(getScala().getKey())) {
            final ScalaRealFile scalaFile = ScalaRealFile.fromInputFile(inputFile);
            directories.add(scalaFile.getParent());
            sensorContext.saveMeasure(scalaFile, CoreMetrics.FILES, 1.0);

            try {
                final String source = FileUtils.readFileToString(inputFile.getFile(), charset);
                final List<String> lines = StringUtils.convertStringToListOfLines(source);
                final List<Comment> comments = new Lexer().getComments(source);

                final CommentsAnalyzer commentsAnalyzer = new CommentsAnalyzer(comments);
                final LinesAnalyzer linesAnalyzer = new LinesAnalyzer(lines, commentsAnalyzer);

                addLineMetrics(sensorContext, scalaFile, linesAnalyzer);
                addCommentMetrics(sensorContext, scalaFile, commentsAnalyzer);
                addCodeMetrics(sensorContext, scalaFile, source);
                addPublicApiMetrics(sensorContext, scalaFile, source);

                complexityOfClasses = sumUpMetricDistributions(complexityOfClasses,
                        ComplexityCalculator.measureComplexityOfClasses(source));

                complexityOfFunctions = sumUpMetricDistributions(complexityOfFunctions,
                        ComplexityCalculator.measureComplexityOfFunctions(source));

            } catch (IOException ioe) {
                LOGGER.error("Could not read the file: " + inputFile.getFile().getAbsolutePath(), ioe);
            }
        }

        if (complexityOfClasses != null)
            sensorContext.saveMeasure(complexityOfClasses.getMeasure());

        if (complexityOfFunctions != null)
            sensorContext.saveMeasure(complexityOfFunctions.getMeasure());

        computePackagesMetric(sensorContext, directories);
    }

    private void addLineMetrics(SensorContext sensorContext, ScalaRealFile scalaFile, LinesAnalyzer linesAnalyzer) {
        sensorContext.saveMeasure(scalaFile, CoreMetrics.LINES, (double) linesAnalyzer.countLines());
        sensorContext.saveMeasure(scalaFile, CoreMetrics.NCLOC, (double) linesAnalyzer.countLinesOfCode());
    }

    private void addCommentMetrics(SensorContext sensorContext, ScalaRealFile scalaFile,
                                   CommentsAnalyzer commentsAnalyzer) {
        sensorContext.saveMeasure(scalaFile, CoreMetrics.COMMENT_LINES,
                (double) commentsAnalyzer.countCommentLines());
        sensorContext.saveMeasure(scalaFile, CoreMetrics.COMMENTED_OUT_CODE_LINES,
                (double) commentsAnalyzer.countCommentedOutLinesOfCode());
    }

    private void addCodeMetrics(SensorContext sensorContext, ScalaRealFile scalaFile, String source) {
        sensorContext.saveMeasure(scalaFile, CoreMetrics.CLASSES,
                (double) TypeCounter.countTypes(source));
        sensorContext.saveMeasure(scalaFile, CoreMetrics.STATEMENTS,
                (double) StatementCounter.countStatements(source));
        sensorContext.saveMeasure(scalaFile, CoreMetrics.FUNCTIONS,
                (double) FunctionCounter.countFunctions(source));
        sensorContext.saveMeasure(scalaFile, CoreMetrics.COMPLEXITY,
                (double) ComplexityCalculator.measureComplexity(source));
    }

    private void addPublicApiMetrics(SensorContext sensorContext, ScalaRealFile scalaFile, String source) {
        sensorContext.saveMeasure(scalaFile, CoreMetrics.PUBLIC_API,
                (double) PublicApiCounter.countPublicApi(source));
        sensorContext.saveMeasure(scalaFile, CoreMetrics.PUBLIC_UNDOCUMENTED_API,
                (double) PublicApiCounter.countUndocumentedPublicApi(source));
    }

    private MetricDistribution sumUpMetricDistributions(MetricDistribution oldDistribution,
                                                        MetricDistribution newDistribution) {
        if (oldDistribution == null) {
            return newDistribution;
        }

        oldDistribution.add(newDistribution);
        return oldDistribution;
    }

    private void computePackagesMetric(SensorContext sensorContext, Set<Directory> directories) {
        for (Directory directory : directories) {
            sensorContext.saveMeasure(directory, CoreMetrics.PACKAGES, 1.0);
        }
    }

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }
}