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

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import org.sonar.api.resources.Resource;
import com.buransky.plugins.scala.language.ScalaFile;
import com.buransky.plugins.scala.language.ScalaPackage;

public class ScalaCoberturaParserTest {

    private ScalaCoberturaParser underTest;

    @Before
    public void setUp() {
        underTest = new ScalaCoberturaParser();
    }

    @Test
    public void shouldCreateScalaFileResourceWhenDeepPackage() {
        Resource resource = underTest.getResource("com.mock.scalapackage.MockScalaClass");
        assertTrue(resource instanceof  ScalaFile);

        ScalaFile file = (ScalaFile)resource;
        assertEquals("MockScalaClass", file.getName());

        ScalaPackage scalaPackage = file.getParent();
        assertNotNull(scalaPackage);
        assertEquals("com.mock.scalapackage", scalaPackage.getName());
    }

    @Test
    public void shouldCreateScalaFileResourceWhenRootPackage() {
        Resource resource = underTest.getResource("MockScalaClass");
        assertTrue(resource instanceof  ScalaFile);

        ScalaFile file = (ScalaFile)resource;
        assertEquals("MockScalaClass", file.getName());

        ScalaPackage scalaPackage = file.getParent();
        assertNotNull(scalaPackage);
        assertEquals("[default]", scalaPackage.getName());
    }

    // TODO remove this test once the sbt scct plugin is patched to produce the correct class name.
    @Test
    public void shouldCreateScalaFileResourceWhenScctBug() {
        Resource resource = underTest.getResource("src.main.scala.com.mock.scalapackage.MockScalaClass");
        assertTrue(resource instanceof  ScalaFile);

        ScalaFile file = (ScalaFile)resource;
        assertEquals("MockScalaClass", file.getName());

        ScalaPackage scalaPackage = file.getParent();
        assertNotNull(scalaPackage);
        assertEquals("com.mock.scalapackage", scalaPackage.getName());
    }

    @Test
    public void shouldCreateScalaFileResourceWhenScctBugForPlayApp() {
        Resource resource = underTest.getResource("app.com.mock.scalapackage.MockScalaClass");
        assertTrue(resource instanceof  ScalaFile);

        ScalaFile file = (ScalaFile)resource;
        assertEquals("MockScalaClass", file.getName());

        ScalaPackage scalaPackage = file.getParent();
        assertNotNull(scalaPackage);
        assertEquals("com.mock.scalapackage", scalaPackage.getName());
    }

}
