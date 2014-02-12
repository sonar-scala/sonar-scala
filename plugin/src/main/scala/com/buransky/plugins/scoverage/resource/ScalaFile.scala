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
package com.buransky.plugins.scoverage.resource

import org.sonar.api.resources.{Directory, File, Resource}
import com.buransky.plugins.scoverage.language.Scala

/**
 * Scala source code file resource.
 *
 * @author Rado Buransky
 */
class ScalaFile(key: String, scala: Scala) extends Resource {
  if (key == null)
    throw new IllegalArgumentException("Key cannot be null!");

  setKey(key)

  private val file = new File(key)

  override lazy val getName = file.getName

  override lazy val getLongName = file.getLongName

  override lazy val getDescription = file.getDescription

  override lazy val getLanguage = scala

  override lazy val getScope = file.getScope

  override lazy val getQualifier = file.getQualifier

  override lazy val getParent = {
    val dir = new SingleDirectory(file.getParent.getKey, scala)

    if (Directory.ROOT == dir.getKey())
      null
    else
      dir
  }

  override def matchFilePattern(antPattern: String) = file.matchFilePattern(antPattern)

  override lazy val toString = file.toString
}
