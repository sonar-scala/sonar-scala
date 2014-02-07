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
import org.sonar.api.resources.Project;
import org.sonar.api.scan.filesystem.FileQuery;
import org.sonar.api.scan.filesystem.FileType;
import org.sonar.api.scan.filesystem.ModuleFileSystem;

import java.io.IOException;

/**
 * Imports Scala source code files to Sonar.
 *
 * @author Rado Buransky
 */
@Phase(name = Name.PRE)
public class ScoverageSourceImporterSensor implements Sensor {

    private static final Logger LOGGER = LoggerFactory.getLogger(ScoverageSourceImporterSensor.class);
    private final Scala scala;
    private final ModuleFileSystem moduleFileSystem;

    public ScoverageSourceImporterSensor(Scala scala, ModuleFileSystem moduleFileSystem) {
        this.scala = scala;
        this.moduleFileSystem = moduleFileSystem;
    }

    public boolean shouldExecuteOnProject(Project project) {
        return project.getLanguage().equals(scala);
    }

    public void analyse(Project project, SensorContext sensorContext) {
        String charset = moduleFileSystem.sourceCharset().toString();

        FileQuery query = FileQuery.on(FileType.SOURCE).onLanguage(scala.getKey());
        for (java.io.File sourceFile : moduleFileSystem.files(query)) {
            addFileToSonar(project, sensorContext, sourceFile, charset);
        }
    }

    @Override
    public String toString() {
        return "Scoverage source importer";
    }

    private void addFileToSonar(Project project, SensorContext sensorContext, java.io.File sourceFile,
                                String charset) {
        try {
            String source = FileUtils.readFileToString(sourceFile, charset);
            String key = File.fromIOFile(sourceFile, project).getKey();
            ScalaFile resource =  new ScalaFile(key);

            sensorContext.index(resource);
            sensorContext.saveSource(resource, source);
        } catch (IOException ioe) {
            LOGGER.error("Could not read the file: " + sourceFile.getAbsolutePath(), ioe);
        }
    }
}