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
package com.buransky.plugins.scala.cobertura;

import org.sonar.plugins.cobertura.api.AbstractCoberturaParser;
import org.sonar.api.resources.Resource;
import com.buransky.plugins.scala.language.ScalaFile;

public class ScalaCoberturaParser extends AbstractCoberturaParser {
    @Override
    protected Resource<?> getResource(String fileName) {
        // TODO update the sbt scct plugin to provide the correct fully qualified class name.
        if (fileName.startsWith("src.main.scala."))
            fileName = fileName.replace("src.main.scala.", "");
        else if (fileName.startsWith("app."))
            fileName = fileName.replace("app.", "");

        int packageTerminator = fileName.lastIndexOf('.');

        if (packageTerminator < 0 ) {
            return new ScalaFile(null, fileName, false);
        }
        else {
            String packageName = fileName.substring(0, packageTerminator);
            String className = fileName.substring(packageTerminator + 1, fileName.length());

            return new ScalaFile(packageName, className, false);
        }
    }
}
