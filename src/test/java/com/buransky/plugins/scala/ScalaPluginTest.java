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

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import org.junit.Test;
import com.buransky.plugins.scala.cobertura.CoberturaSensor;
import com.buransky.plugins.scala.surefire.SurefireSensor;

public class ScalaPluginTest {

  @Test
  public void shouldHaveExtensions() {
    assertThat(new ScalaPlugin().getExtensions().size(), greaterThan(0));
  }
  
  @Test
  public void shouldHaveCoberturaPlugin() {
	assertTrue(new ScalaPlugin().getExtensions().contains(CoberturaSensor.class));
  }

  @Test
  public void shouldHaveSurefirePlugin() {
    assertTrue(new ScalaPlugin().getExtensions().contains(SurefireSensor.class));
  }

  @Test
  public void shouldGetPathToDependencies() {
    assertThat(ScalaPlugin.getPathToScalaLibrary(), containsString("scala-library"));
  }
}
