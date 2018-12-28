/*
 * Sonar Scala Plugin
 * Copyright (C) 2018 All contributors
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
package com.mwz.sonar.scala
package util

import java.nio.file.{Path, Paths}

import org.sonar.api.batch.fs.FileSystem

import scala.language.implicitConversions

/**
 * Various Path utilities.
 */
object PathUtils {

  /** Current working directory. */
  def cwd: Path = Paths.get(".").toAbsolutePath.normalize

  /**
   * Construct a relative path between the base path and the suffix if the suffix is absolute,
   * otherwise resolve the relative suffix path against the 'next' path.
   */
  def relativize(base: Path, next: Path, fullOrSuffix: Path): Path =
    if (fullOrSuffix.isAbsolute) base.relativize(fullOrSuffix)
    else next.resolve(fullOrSuffix)

  /**
   * Strips out prefix from the path if it starts with the prefix.
   */
  def stripOutPrefix(prefix: Path, path: Path): Path =
    if (path.startsWith(prefix)) prefix.relativize(path)
    else path

  /**
   * Returns the module base path relative to the current working directory
   */
  def getModuleBaseDirectory(fs: FileSystem): Path = {
    val moduleAbsolutePath = Paths.get(fs.baseDir().getAbsolutePath).normalize
    val currentWorkdirAbsolutePath = PathUtils.cwd
    currentWorkdirAbsolutePath.relativize(moduleAbsolutePath)
  }
}
