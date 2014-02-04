package com.buransky.plugins.scoverage.sensor;

import com.buransky.plugins.scoverage.language.Scala;
import com.buransky.plugins.scoverage.language.ScalaFile;
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
            ScalaFile resource = ScalaFile.fromInputFile(inputFile, isUnitTest);

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