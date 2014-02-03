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
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertThat;

import org.junit.Test;

public class ScalaTest {

  @Test
  public void shouldHaveScalaLanguageKey() {
    assertThat(new Scala().getKey(), equalTo("scala"));
    assertThat(Scala.INSTANCE.getKey(), equalTo("scala"));
  }

  @Test
  public void shouldHaveScalaLanguageName() {
    assertThat(new Scala().getName(), equalTo("Scala"));
    assertThat(Scala.INSTANCE.getName(), equalTo("Scala"));
  }

  @Test
  public void shouldHaveScalaFileSuffixes() {
    String[] suffixes = new String[] { "scala" };
    assertArrayEquals(new Scala().getFileSuffixes(), suffixes);
    assertArrayEquals(Scala.INSTANCE.getFileSuffixes(), suffixes);
  }
}