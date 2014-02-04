package com.buransky.plugins.scoverage;

import com.buransky.plugins.scoverage.sensor.ScoverageSensor;
import com.buransky.plugins.scoverage.language.Scala;
import com.buransky.plugins.scoverage.sensor.ScalaSourceImporterSensor;
import org.sonar.api.Extension;
import org.sonar.api.SonarPlugin;

import java.util.ArrayList;
import java.util.List;

public class ScoveragePlugin extends SonarPlugin {

    public List<Class<? extends Extension>> getExtensions() {
        final List<Class<? extends Extension>> extensions = new ArrayList<Class<? extends Extension>>();
        extensions.add(Scala.class);
        extensions.add(ScalaSourceImporterSensor.class);
        extensions.add(ScoverageSensor.class);

        return extensions;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }
}
