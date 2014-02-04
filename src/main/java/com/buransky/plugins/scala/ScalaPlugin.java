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
package com.buransky.plugins.scala;

import com.buransky.plugins.scala.cobertura.CoberturaSensor;
import com.buransky.plugins.scala.language.Scala;
import com.buransky.plugins.scala.sensor.ScalaSourceImporterSensor;
import org.sonar.api.Extension;
import org.sonar.api.SonarPlugin;

import java.util.ArrayList;
import java.util.List;

/**
 * This class is the entry point for all extensions made by the
 * Sonar Scala Plugin.
 *
 * @author Felix Müller
 * @since 0.1
 */
public class ScalaPlugin extends SonarPlugin {

    public List<Class<? extends Extension>> getExtensions() {
        final List<Class<? extends Extension>> extensions = new ArrayList<Class<? extends Extension>>();
        extensions.add(Scala.class);
        extensions.add(ScalaSourceImporterSensor.class);
        extensions.add(CoberturaSensor.class);

        return extensions;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }

    public static String getPathToScalaLibrary() {
        return getPathByResource("scala/package.class");
    }

    /**
     * Godin: during execution of Sonar Batch all dependencies of a plugin are downloaded and
     * available locally as JAR-files, so we can use this kind of hack to locate JARs.
     */
    private static String getPathByResource(String name) {
        String path = ScalaPlugin.class.getClassLoader().getResource(name).getPath();
        return path.substring("file:".length(), path.lastIndexOf('!'));
    }
}
