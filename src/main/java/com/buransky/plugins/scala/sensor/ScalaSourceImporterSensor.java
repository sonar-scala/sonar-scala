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

import com.buransky.plugins.scala.language.Scala;
import com.buransky.plugins.scala.language.ScalaRealFile;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.Phase;
import org.sonar.api.batch.Phase.Name;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.resources.InputFile;
import org.sonar.api.resources.Project;
import org.sonar.api.resources.ProjectFileSystem;

import java.io.IOException;

/**
 * This Sensor imports all Scala files into Sonar.
 *
 * @author Felix Müller
 * @since 0.1
 */
@Phase(name = Name.PRE)
public class ScalaSourceImporterSensor extends AbstractScalaSensor {

    private static final Logger LOGGER = LoggerFactory.getLogger(ScalaSourceImporterSensor.class);

    public ScalaSourceImporterSensor(Scala scala) {
        super(scala);
    }

    public void analyse(Project project, SensorContext sensorContext) {
        ProjectFileSystem fileSystem = project.getFileSystem();
        String charset = fileSystem.getSourceCharset().toString();

        for (InputFile sourceFile : fileSystem.mainFiles(getScala().getKey())) {
            addFileToSonar(sensorContext, sourceFile, false, charset);
        }

        for (InputFile testFile : fileSystem.testFiles(getScala().getKey())) {
            addFileToSonar(sensorContext, testFile, true, charset);
        }
    }

    private void addFileToSonar(SensorContext sensorContext, InputFile inputFile,
                                boolean isUnitTest, String charset) {
        try {
            String source = FileUtils.readFileToString(inputFile.getFile(), charset);

            //ScalaFile resource = ScalaFile.fromInputFile(inputFile, isUnitTest);
            ScalaRealFile resource = ScalaRealFile.fromInputFile(inputFile, isUnitTest);

            sensorContext.index(resource);
            sensorContext.saveSource(resource, source);

            if (LOGGER.isDebugEnabled()) {
                if (isUnitTest) {
                    LOGGER.debug("Added Scala test file to Sonar: " + inputFile.getFile().getAbsolutePath());
                } else {
                    LOGGER.debug("Added Scala source file to Sonar: " + inputFile.getFile().getAbsolutePath());
                }
            }
        } catch (IOException ioe) {
            LOGGER.error("Could not read the file: " + inputFile.getFile().getAbsolutePath(), ioe);
        }
    }

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }
}