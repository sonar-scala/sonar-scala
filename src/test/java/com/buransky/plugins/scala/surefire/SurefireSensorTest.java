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
package com.buransky.plugins.scala.surefire;

import org.junit.Test;
import org.junit.Before;
import org.sonar.api.batch.CoverageExtension;
import org.sonar.api.resources.Project;
import com.buransky.plugins.scala.language.Scala;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class SurefireSensorTest {

    private SurefireSensor sensor;
    private Project project;
    
    @Before
    public void setUp() {
    	sensor = new SurefireSensor();
    	project = mock(Project.class);
    }

    @Test
    public void shouldExecuteOnReuseReports() {
        when(project.getLanguageKey()).thenReturn(Scala.INSTANCE.getKey());
        when(project.getAnalysisType()).thenReturn(Project.AnalysisType.REUSE_REPORTS);
        assertTrue(sensor.shouldExecuteOnProject(project));
    }

    @Test
    public void shouldExecuteOnDynamicAnalysis() {
        when(project.getLanguageKey()).thenReturn(Scala.INSTANCE.getKey());
        when(project.getAnalysisType()).thenReturn(Project.AnalysisType.DYNAMIC);
        assertTrue(sensor.shouldExecuteOnProject(project));
    }

    @Test
    public void shouldNotExecuteIfStaticAnalysis() {
        when(project.getLanguageKey()).thenReturn(Scala.INSTANCE.getKey());
        when(project.getAnalysisType()).thenReturn(Project.AnalysisType.STATIC);
        assertFalse(sensor.shouldExecuteOnProject(project));
    }

    @Test
    public void shouldNotExecuteOnJavaProject() {
        when(project.getLanguageKey()).thenReturn("java");
        when(project.getAnalysisType()).thenReturn(Project.AnalysisType.DYNAMIC);
        assertFalse(sensor.shouldExecuteOnProject(project));
    }

    @Test
    public void shouldDependOnCoverageSensors() {
        assertEquals(CoverageExtension.class, sensor.dependsUponCoverageSensors());
    }

    @Test
    public void testToString() {
        assertEquals("Scala SurefireSensor", sensor.toString());
    }
}