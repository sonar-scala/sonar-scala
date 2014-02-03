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
package com.buransky.plugins.scala.language;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;

import com.buransky.plugins.scala.util.FileTestUtils;
import org.junit.Test;
import org.sonar.api.resources.InputFile;
import org.sonar.api.resources.Qualifiers;

public class ScalaFileTest {

  @Test
  public void shouldHaveFileQualifierForSourceFile() {
    assertThat(new ScalaFile("package", "Class", false).getQualifier(),
        equalTo(Qualifiers.FILE));
  }

  @Test
  public void shouldHaveTestFileQualifierForTestFile() {
    assertThat(new ScalaFile("package", "Class", true).getQualifier(),
        equalTo(Qualifiers.UNIT_TEST_FILE));
  }

  @Test
  public void shouldCreateScalaFileWithCorrectAttributes() {
    InputFile inputFile = FileTestUtils.getInputFiles("/scalaFile/", "ScalaFile", 1).get(0);
    ScalaFile scalaFile = ScalaFile.fromInputFile(inputFile);

    assertThat(scalaFile.getLanguage().getKey(), is(Scala.INSTANCE.getKey()));
    assertThat(scalaFile.getName(), is("ScalaFile1"));
    assertThat(scalaFile.getLongName(), is("scalaFile.ScalaFile1"));
    assertThat(scalaFile.getParent().getName(), is("scalaFile"));
    assertThat(scalaFile.isUnitTest(), is(false));
  }

  @Test
  public void shouldCreateScalaTestFileWithCorrectAttributes() {
    InputFile inputFile = FileTestUtils.getInputFiles("/scalaFile/", "ScalaTestFile", 1).get(0);
    ScalaFile scalaFile = ScalaFile.fromInputFile(inputFile, true);

    assertThat(scalaFile.getLanguage().getKey(), is(Scala.INSTANCE.getKey()));
    assertThat(scalaFile.getName(), is("ScalaTestFile1"));
    assertThat(scalaFile.getLongName(), is("scalaFile.ScalaTestFile1"));
    assertThat(scalaFile.getParent().getName(), is("scalaFile"));
    assertThat(scalaFile.isUnitTest(), is(true));
  }

  @Test
  public void shouldNotCreateScalaFileIfInputFileIsNull() {
    assertNull(ScalaFile.fromInputFile(null));
  }

  @Test
  public void shouldNotCreateScalaFileIfFileIsNull() {
    InputFile inputFile = mock(InputFile.class);
    when(inputFile.getFile()).thenReturn(null);
    assertNull(ScalaFile.fromInputFile(inputFile));
  }

  @Test
  public void shouldNotCreateScalaFileIfRelativePathIsNull() {
    InputFile inputFile = mock(InputFile.class);
    when(inputFile.getFile()).thenReturn(new File(""));
    when(inputFile.getRelativePath()).thenReturn(null);
    assertNull(ScalaFile.fromInputFile(inputFile));
  }
}