/*
 * Copyright (C) 2018-2020  All sonar-scala contributors
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

import cats.effect.IO
import cats.effect.concurrent.Ref
import com.mwz.sonar.scala.util.Logger

trait WithFiles {
  def withFiles(paths: String*)(test: Seq[File] => Any): Unit = {
    val tmpDir: Path = Files.createTempDirectory("")
    val files: Seq[File] = paths.map(path => Files.createFile(tmpDir.resolve(path)).toFile)
    try test(files)
    finally {
      files.foreach(f => Files.deleteIfExists(f.toPath))
      Files.deleteIfExists(tmpDir)
    }
  }
}

trait WithTracing {
  def withTracing(test: Ref[IO, List[String]] => Any): Unit =
    test(Ref.unsafe[IO, List[String]](List.empty))
}

trait WithLogging {
  object LogLevel {
    sealed trait Level
    case object Debug extends Level
    case object Info extends Level
    case object Warn extends Level
    case object Error extends Level
  }

  def withLogging(test: (Ref[IO, List[(LogLevel.Level, String)]], Logger[IO]) => Any): Unit = {
    val logs = Ref.unsafe[IO, List[(LogLevel.Level, String)]](List.empty)
    val logger: Logger[IO] = new Logger[IO] {
      def debug(s: String): IO[Unit] = logs.update((LogLevel.Debug, s) :: _)
      def info(s: String): IO[Unit] = logs.update((LogLevel.Info, s) :: _)
      def warn(s: String): IO[Unit] = logs.update((LogLevel.Warn, s) :: _)
      def error(s: String): IO[Unit] = logs.update((LogLevel.Error, s) :: _)
      def error(s: String, e: Throwable): IO[Unit] = logs.update((LogLevel.Error, s) :: _)
    }
    test(logs, logger)
  }
}
