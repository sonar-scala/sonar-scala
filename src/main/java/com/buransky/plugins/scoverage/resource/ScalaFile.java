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
package com.buransky.plugins.scoverage.resource;

import com.buransky.plugins.scoverage.language.Scala;
import org.sonar.api.resources.File;
import org.sonar.api.resources.Language;
import org.sonar.api.resources.Resource;

public class ScalaFile extends Resource {
    private final File file;
    private ScalaDirectory parent;

    public ScalaFile(String key) {
        if (key == null)
            throw new IllegalArgumentException("Key cannot be null!");

        file = new File(key);
        setKey(key);
    }

    @Override
    public String getName() {
        return file.getName();
    }

    @Override
    public String getLongName() {
        return file.getLongName();
    }

    @Override
    public String getDescription() {
        return file.getDescription();
    }

    @Override
    public Language getLanguage() {
        return Scala.INSTANCE;
    }

    @Override
    public String getScope() {
        return file.getScope();
    }

    @Override
    public String getQualifier() {
        return file.getQualifier();
    }

    @Override
    public ScalaDirectory getParent() {
        if (parent == null) {
            parent = new ScalaDirectory(file.getParent().getKey());
        }
        return parent;
    }

    @Override
    public boolean matchFilePattern(String antPattern) {
        return file.matchFilePattern(antPattern);
    }

    @Override
    public String toString() {
        return file.toString();
    }
}