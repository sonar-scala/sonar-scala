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
package com.buransky.plugins.scala.sensor;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.resources.Java;
import org.sonar.api.resources.Project;
import com.buransky.plugins.scala.language.Scala;

public class AbstractScalaSensorTest {

  private AbstractScalaSensor abstractScalaSensor;

  @Before
  public void setUp() {
    abstractScalaSensor = new AbstractScalaSensor(Scala.INSTANCE) {

      public void analyse(Project project, SensorContext context) {
        // dummy implementation, never called in this test
      }
    };
  }

  @Test
  public void shouldOnlyExecuteOnScalaProjects() {
    Project scalaProject = mock(Project.class);
    when(scalaProject.getLanguage()).thenReturn(Scala.INSTANCE);
    Project javaProject = mock(Project.class);
    when(javaProject.getLanguage()).thenReturn(Java.INSTANCE);

    assertTrue(abstractScalaSensor.shouldExecuteOnProject(scalaProject));
    assertFalse(abstractScalaSensor.shouldExecuteOnProject(javaProject));
  }

  @Test
  public void shouldHaveScalaAsLanguage() {
    assertThat(abstractScalaSensor.getScala(), equalTo(new Scala()));
  }
}