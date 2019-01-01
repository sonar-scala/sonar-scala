/*
 * Copyright (C) 2018-2019  All sonar-scala contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU General Lesser Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.mwz.sonar.scala

import java.io.File
import java.nio.file.{Files, Path}

trait WithFiles {
  def withFiles(paths: String*)(test: Seq[File] => Any): Unit = {
    val tmpDir: Path = Files.createTempDirectory("")
    val files: Seq[File] = paths.map { path =>
      Files.createFile(tmpDir.resolve(path)).toFile
    }
    try test(files)
    finally {
      files.foreach(f => Files.deleteIfExists(f.toPath))
      Files.deleteIfExists(tmpDir)
    }
  }
}
