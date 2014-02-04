package com.buransky.plugins.scoverage.sensor;

import com.buransky.plugins.scoverage.language.Scala;
import com.buransky.plugins.scoverage.language.ScalaFile;
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

    private void addFileToSonar(Project project, SensorContext sensorContext, InputFile inputFile,
                                String charset) {
        try {
            String source = FileUtils.readFileToString(inputFile.getFile(), charset);
            ScalaFile resource =  new ScalaFile(File.fromIOFile(inputFile.getFile(), project).getKey());
            if (resource == null) {
                LOGGER.warn("[ScoverageSourceImporterSensor] Resource null! " + inputFile.getRelativePath());
                return;
            }

            sensorContext.index(resource);
            sensorContext.saveSource(resource, source);
        } catch (IOException ioe) {
            LOGGER.error("Could not read the file: " + inputFile.getFile().getAbsolutePath(), ioe);
        }
    }
}