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
package com.buransky.plugins.scala.util;

import com.buransky.plugins.scala.language.ScalaFile;

public class DummyScalaFile extends ScalaFile {

  public DummyScalaFile(boolean isUnitTest) {
    super("", "", isUnitTest);
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof ScalaFile)) {
      return false;
    }
    ScalaFile other = (ScalaFile) obj;
    return isUnitTest() == other.isUnitTest();
  }
}