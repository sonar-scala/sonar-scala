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

import cats.effect.Sync
import org.sonar.api.utils.log.{Logger => SonarLogger}
import org.sonar.api.utils.log.{Loggers => SonarLoggers}

trait Logger[F[_]] {
  def debug(s: String): F[Unit]
  def info(s: String): F[Unit]
  def warn(s: String): F[Unit]
  def error(s: String): F[Unit]
  def error(s: String, e: Throwable): F[Unit]
}

object Logger {
  def apply[F[_]](implicit ev: Logger[F]): Logger[F] = ev
  def create[F[_]: Sync, T](clazz: Class[T], module: String): F[Logger[F]] =
    create(clazz, Some(module))
  def create[F[_]: Sync, T](clazz: Class[T], module: Option[String] = None): F[Logger[F]] =
    Sync[F].delay {
      val log: SonarLogger = SonarLoggers.get(clazz)
      val prefix: String = "sonar-scala" + module.fold("")("-" + _)

      new Logger[F] {
        override def debug(s: String): F[Unit] =
          Sync[F].delay(log.debug(s"[$prefix] $s"))
        override def info(s: String): F[Unit] =
          Sync[F].delay(log.info(s"[$prefix] $s"))
        override def warn(s: String): F[Unit] =
          Sync[F].delay(log.warn(s"[$prefix] $s"))
        override def error(s: String): F[Unit] =
          Sync[F].delay(log.error(s"[$prefix] $s"))
        override def error(s: String, e: Throwable): F[Unit] =
          Sync[F].delay(log.error(s"[$prefix] $s", e))
      }
    }
}
