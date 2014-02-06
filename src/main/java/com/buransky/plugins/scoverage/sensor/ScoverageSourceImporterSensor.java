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

import com.buransky.plugins.scoverage.language.Scala;
import com.buransky.plugins.scoverage.resource.ScalaFile;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.Phase;
import org.sonar.api.batch.Phase.Name;
import org.sonar.api.batch.Sensor;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.resources.File;
import org.sonar.api.resources.InputFile;
import org.sonar.api.resources.Project;
import org.sonar.api.resources.ProjectFileSystem;

import java.io.IOException;

@Phase(name = Name.PRE)
public class ScoverageSourceImporterSensor implements Sensor {

    private static final Logger LOGGER = LoggerFactory.getLogger(ScoverageSourceImporterSensor.class);
    private final Scala scala;

    public ScoverageSourceImporterSensor(Scala scala) {
        this.scala = scala;
    }

    public boolean shouldExecuteOnProject(Project project) {
        return project.getLanguage().equals(scala);
    }

    public void analyse(Project project, SensorContext sensorContext) {
        ProjectFileSystem fileSystem = project.getFileSystem();
        String charset = fileSystem.getSourceCharset().toString();

        for (InputFile sourceFile : fileSystem.mainFiles(scala.getKey())) {
            addFileToSonar(project, sensorContext, sourceFile, charset);
        }
    }

    @Override
    public String toString() {
        return "Scoverage source importer";
    }

    private void addFileToSonar(Project project, SensorContext sensorContext, InputFile inputFile,
                                String charset) {
        try {
            String source = FileUtils.readFileToString(inputFile.getFile(), charset);

            String key = File.fromIOFile(inputFile.getFile(), project).getKey();
            ScalaFile resource =  new ScalaFile(key);

            sensorContext.index(resource);
            sensorContext.saveSource(resource, source);
        } catch (IOException ioe) {
            LOGGER.error("Could not read the file: " + inputFile.getFile().getAbsolutePath(), ioe);
        }
    }
}