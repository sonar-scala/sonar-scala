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
package syntax

import java.io.File
import java.nio.file.Path

import cats.syntax.flatMap._
import cats.{Monad, MonoidK}
import org.sonar.api.batch.fs.FileSystem

import scala.language.{higherKinds, implicitConversions}
import scala.util.{Failure, Success, Try}

object FileSystemSyntax {
  implicit final def fileSystemOps(fs: FileSystem): FileSystemOps =
    new FileSystemOps(fs)
}

final class FileSystemOps(val fs: FileSystem) extends AnyVal {

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
