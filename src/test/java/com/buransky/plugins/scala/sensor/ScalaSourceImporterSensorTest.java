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

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.resources.InputFile;
import org.sonar.api.resources.Java;
import org.sonar.api.resources.Project;
import org.sonar.api.resources.ProjectFileSystem;
import com.buransky.plugins.scala.language.Scala;
import com.buransky.plugins.scala.util.FileTestUtils;

public class ScalaSourceImporterSensorTest {

  private ScalaSourceImporterSensor scalaSourceImporter;

  private ProjectFileSystem fileSystem;
  private Project project;
  private SensorContext sensorContext;

  @Before
  public void setUp() {
    scalaSourceImporter = new ScalaSourceImporterSensor(Scala.INSTANCE);

    fileSystem = mock(ProjectFileSystem.class);
    when(fileSystem.getSourceCharset()).thenReturn(Charset.defaultCharset());

    project = mock(Project.class);
    when(project.getFileSystem()).thenReturn(fileSystem);

    sensorContext = mock(SensorContext.class);
  }

  @Test
  public void shouldImportOnlyOneScalaFile() {
    when(fileSystem.mainFiles(scalaSourceImporter.getScala().getKey())).thenReturn(getInputFiles(1));
    when(fileSystem.testFiles(scalaSourceImporter.getScala().getKey())).thenReturn(new ArrayList<InputFile>());

    scalaSourceImporter.analyse(project, sensorContext);

    InOrder inOrder = inOrder(sensorContext);
    inOrder.verify(sensorContext, times(1)).index(eq(FileTestUtils.SCALA_SOURCE_FILE));
    inOrder.verify(sensorContext, times(1)).saveSource(eq(FileTestUtils.SCALA_SOURCE_FILE), any(String.class));
    inOrder.verifyNoMoreInteractions();
  }

  @Test
  public void shouldImportOnlyOneScalaFileWithTheCorrectFileContent() throws IOException {
    when(fileSystem.mainFiles(scalaSourceImporter.getScala().getKey())).thenReturn(getInputFiles(1));
    when(fileSystem.testFiles(scalaSourceImporter.getScala().getKey())).thenReturn(new ArrayList<InputFile>());

    scalaSourceImporter.analyse(project, sensorContext);

    InOrder inOrder = inOrder(sensorContext);
    inOrder.verify(sensorContext, times(1)).index(eq(FileTestUtils.SCALA_SOURCE_FILE));
    inOrder.verify(sensorContext, times(1)).saveSource(eq(FileTestUtils.SCALA_SOURCE_FILE),
        eq(getContentOfFiles(1).get(0)));
    inOrder.verifyNoMoreInteractions();
  }

  @Test
  public void shouldImportAllScalaFiles() {
    when(fileSystem.mainFiles(scalaSourceImporter.getScala().getKey())).thenReturn(getInputFiles(3));
    when(fileSystem.testFiles(scalaSourceImporter.getScala().getKey())).thenReturn(new ArrayList<InputFile>());

    scalaSourceImporter.analyse(project, sensorContext);

    verify(sensorContext, times(3)).index(eq(FileTestUtils.SCALA_SOURCE_FILE));
    verify(sensorContext, times(3)).saveSource(eq(FileTestUtils.SCALA_SOURCE_FILE), any(String.class));
    verifyNoMoreInteractions(sensorContext);
  }

  @Test
  public void shouldImportAllScalaFilesAndNotFilesOfOtherLanguages() {
    when(fileSystem.mainFiles(scalaSourceImporter.getScala().getKey())).thenReturn(getInputFiles(3));
    when(fileSystem.mainFiles(Java.INSTANCE.getKey()))
        .thenReturn(FileTestUtils.getInputFiles("/scalaSourceImporter/", "JavaMainFile", "java", 1));
    when(fileSystem.testFiles(scalaSourceImporter.getScala().getKey())).thenReturn(new ArrayList<InputFile>());

    scalaSourceImporter.analyse(project, sensorContext);

    verify(sensorContext, times(3)).index(eq(FileTestUtils.SCALA_SOURCE_FILE));
    verify(sensorContext, times(3)).saveSource(eq(FileTestUtils.SCALA_SOURCE_FILE), any(String.class));
    verifyNoMoreInteractions(sensorContext);
  }

  @Test
  public void shouldImportAllScalaFilesWithTheCorrectFileContent() throws IOException {
    when(fileSystem.mainFiles(scalaSourceImporter.getScala().getKey())).thenReturn(getInputFiles(3));
    when(fileSystem.testFiles(scalaSourceImporter.getScala().getKey())).thenReturn(new ArrayList<InputFile>());

    scalaSourceImporter.analyse(project, sensorContext);

    List<String> contentOfFiles = getContentOfFiles(3);
    verify(sensorContext, times(3)).index(eq(FileTestUtils.SCALA_SOURCE_FILE));
    verify(sensorContext, times(1)).saveSource(eq(FileTestUtils.SCALA_SOURCE_FILE), eq(contentOfFiles.get(0)));
    verify(sensorContext, times(1)).saveSource(eq(FileTestUtils.SCALA_SOURCE_FILE), eq(contentOfFiles.get(1)));
    verify(sensorContext, times(1)).saveSource(eq(FileTestUtils.SCALA_SOURCE_FILE), eq(contentOfFiles.get(2)));
    verifyNoMoreInteractions(sensorContext);
  }

  @Test
  public void shouldImportOnlyOneScalaTestFile() {
    when(fileSystem.mainFiles(scalaSourceImporter.getScala().getKey())).thenReturn(new ArrayList<InputFile>());
    when(fileSystem.testFiles(scalaSourceImporter.getScala().getKey())).thenReturn(getTestInputFiles(1));

    scalaSourceImporter.analyse(project, sensorContext);

    InOrder inOrder = inOrder(sensorContext);
    inOrder.verify(sensorContext, times(1)).index(eq(FileTestUtils.SCALA_TEST_FILE));
    inOrder.verify(sensorContext, times(1)).saveSource(eq(FileTestUtils.SCALA_TEST_FILE), any(String.class));
    inOrder.verifyNoMoreInteractions();
  }

  @Test
  public void shouldImportOnlyOneScalaTestFileWithTheCorrectFileContent() throws IOException {
    when(fileSystem.mainFiles(scalaSourceImporter.getScala().getKey())).thenReturn(new ArrayList<InputFile>());
    when(fileSystem.testFiles(scalaSourceImporter.getScala().getKey())).thenReturn(getTestInputFiles(1));

    scalaSourceImporter.analyse(project, sensorContext);

    InOrder inOrder = inOrder(sensorContext);
    inOrder.verify(sensorContext, times(1)).index(eq(FileTestUtils.SCALA_TEST_FILE));
    inOrder.verify(sensorContext, times(1)).saveSource(eq(FileTestUtils.SCALA_TEST_FILE),
        eq(getContentOfTestFiles(1).get(0)));
    inOrder.verifyNoMoreInteractions();
  }

  @Test
  public void shouldImportAllScalaTestFiles() {
    when(fileSystem.mainFiles(scalaSourceImporter.getScala().getKey())).thenReturn(new ArrayList<InputFile>());
    when(fileSystem.testFiles(scalaSourceImporter.getScala().getKey())).thenReturn(getTestInputFiles(3));

    scalaSourceImporter.analyse(project, sensorContext);

    verify(sensorContext, times(3)).index(eq(FileTestUtils.SCALA_TEST_FILE));
    verify(sensorContext, times(3)).saveSource(eq(FileTestUtils.SCALA_TEST_FILE), any(String.class));
    verifyNoMoreInteractions(sensorContext);
  }

  @Test
  public void shouldImportAllScalaTestFilesAndNotTestFilesOfOtherLanguages() {
    when(fileSystem.mainFiles(scalaSourceImporter.getScala().getKey())).thenReturn(new ArrayList<InputFile>());
    when(fileSystem.testFiles(Java.INSTANCE.getKey()))
        .thenReturn(FileTestUtils.getInputFiles("/scalaSourceImporter/", "JavaTestFile", "java", 1));
    when(fileSystem.testFiles(scalaSourceImporter.getScala().getKey())).thenReturn(getTestInputFiles(3));

    scalaSourceImporter.analyse(project, sensorContext);

    verify(sensorContext, times(3)).index(eq(FileTestUtils.SCALA_TEST_FILE));
    verify(sensorContext, times(3)).saveSource(eq(FileTestUtils.SCALA_TEST_FILE), any(String.class));
    verifyNoMoreInteractions(sensorContext);
  }

  @Test
  public void shouldImportAllScalaTestFilesWithTheCorrectFileContent() throws IOException {
    when(fileSystem.mainFiles(scalaSourceImporter.getScala().getKey())).thenReturn(new ArrayList<InputFile>());
    when(fileSystem.testFiles(scalaSourceImporter.getScala().getKey())).thenReturn(getTestInputFiles(3));

    scalaSourceImporter.analyse(project, sensorContext);

    List<String> contentOfFiles = getContentOfTestFiles(3);
    verify(sensorContext, times(3)).index(eq(FileTestUtils.SCALA_TEST_FILE));
    verify(sensorContext, times(1)).saveSource(eq(FileTestUtils.SCALA_TEST_FILE), eq(contentOfFiles.get(0)));
    verify(sensorContext, times(1)).saveSource(eq(FileTestUtils.SCALA_TEST_FILE), eq(contentOfFiles.get(1)));
    verify(sensorContext, times(1)).saveSource(eq(FileTestUtils.SCALA_TEST_FILE), eq(contentOfFiles.get(2)));
    verifyNoMoreInteractions(sensorContext);
  }

  public List<InputFile> getInputFiles(int numberOfFiles) {
    return FileTestUtils.getInputFiles("/scalaSourceImporter/", "MainFile", numberOfFiles);
  }

  public List<InputFile> getTestInputFiles(int numberOfFiles) {
    return FileTestUtils.getInputFiles("/scalaSourceImporter/", "TestFile", numberOfFiles);
  }

  public List<String> getContentOfFiles(int numberOfFiles) throws IOException {
    return FileTestUtils.getContentOfFiles("/scalaSourceImporter/", "MainFile", numberOfFiles);
  }

  public List<String> getContentOfTestFiles(int numberOfFiles) throws IOException {
    return FileTestUtils.getContentOfFiles("/scalaSourceImporter/", "TestFile", numberOfFiles);
  }
}