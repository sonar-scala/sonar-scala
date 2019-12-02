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
package util

import org.sonar.api.utils.log.{Logger => SonarLogger, Loggers => SonarLoggers}

trait Log {
  def debug(s: String): Unit
  def info(s: String): Unit
  def warn(s: String): Unit
  def error(s: String): Unit
}

object Log {
  def apply[T](clazz: Class[T], module: String): Log = Log(clazz, Some(module))
  def apply[T](clazz: Class[T], module: Option[String] = None): Log = {
    val log: SonarLogger = SonarLoggers.get(clazz)
    val prefix: String = "sonar-scala" + module.fold("")("-" + _)
    new Log {
      override def debug(s: String): Unit = log.debug(s"[$prefix] $s")
      override def info(s: String): Unit = log.info(s"[$prefix] $s")
      override def warn(s: String): Unit = log.warn(s"[$prefix] $s")
      override def error(s: String): Unit = log.error(s"[$prefix] $s")
    }
  }
}
