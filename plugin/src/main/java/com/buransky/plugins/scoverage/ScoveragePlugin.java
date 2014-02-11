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
package com.buransky.plugins.scoverage;

import com.buransky.plugins.scoverage.language.Scala;
import com.buransky.plugins.scoverage.measure.ScalaMetrics;
import com.buransky.plugins.scoverage.sensor.ScoverageSensor;
import com.buransky.plugins.scoverage.sensor.ScoverageSourceImporterSensor;
import com.buransky.plugins.scoverage.widget.ScoverageWidget;
import org.sonar.api.Extension;
import org.sonar.api.SonarPlugin;

import java.util.ArrayList;
import java.util.List;

/**
 * Plugin entry point.
 *
 * @author Rado Buransky
 */
public class ScoveragePlugin extends SonarPlugin {
    public List<Class<? extends Extension>> getExtensions() {
        final List<Class<? extends Extension>> extensions = new ArrayList<Class<? extends Extension>>();
        extensions.add(ScalaMetrics.class);
        extensions.add(Scala.class);
        extensions.add(ScoverageSourceImporterSensor.class);
        extensions.add(ScoverageSensor.class);
        extensions.add(ScoverageWidget.class);

        return extensions;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }
}
