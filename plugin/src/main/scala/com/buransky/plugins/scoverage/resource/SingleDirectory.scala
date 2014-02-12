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

import org.sonar.api.resources.Directory
import com.buransky.plugins.scoverage.language.Scala

/**
 * Single directory in file system. Unlike org.sonar.api.resources.Directory that can represent
 * a chain of directories.
 *
 * @author Rado Buransky
 */
class SingleDirectory(key: String, scala: Scala) extends Directory(key) {
  private val name: String = {
    val i = key.lastIndexOf(Directory.SEPARATOR)
    if (i >= 0) key.substring(i + 1) else key
  }

  private val parent: Option[SingleDirectory] = {
    val i = key.lastIndexOf(Directory.SEPARATOR)
    if (i > 0) Some(new SingleDirectory(key.substring(0, i), scala)) else None
  }

  override lazy val getName = name

  override lazy val getLanguage = scala

  override lazy val getParent = parent.getOrElse(null)
}