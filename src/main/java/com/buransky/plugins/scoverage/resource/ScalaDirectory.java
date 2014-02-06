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
import org.sonar.api.resources.Directory;
import org.sonar.api.resources.Language;
import org.sonar.api.resources.Resource;

public class ScalaDirectory extends Directory {
    private final String name;
    private final ScalaDirectory parent;

    public ScalaDirectory(String key) {
        super(key);

        int i = getKey().lastIndexOf(SEPARATOR);
        if (i > 0) {
            parent = new ScalaDirectory(key.substring(0, i));
            name = key.substring(i + 1);
        }
        else {
            name = key;
            parent = null;
        }
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Language getLanguage() {
        return Scala.INSTANCE;
    }

    @Override
    public Resource getParent() {
        return parent;
    }
}
