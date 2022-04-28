/*
 * Copyright (C) 2018-2022  All sonar-scala contributors
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
package util
package syntax

import java.io.File
import java.nio.file.Path

import scala.util.Failure
import scala.util.Success
import scala.util.Try

import cats.Monad
import cats.MonoidK
import cats.syntax.flatMap._
import org.sonar.api.batch.fs.FileSystem

object SonarFileSystem {
  implicit final class FileSystemOps(private val fs: FileSystem) extends AnyVal {

    /**
     * Resolve paths relative to the given file system.
     */
    def resolve[F[_]: Monad: MonoidK](toResolve: F[Path]): F[File] =
      toResolve.flatMap[File] { path =>
        Try(fs.resolvePath(path.toString)) match {
          case Failure(_) => MonoidK[F].empty
          case Success(f) => Monad[F].pure(f)
        }
      }
  }
}
