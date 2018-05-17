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

import org.sonar.api.utils.log.Loggers

trait Log {
  def debug(s: String): Unit
  def info(s: String): Unit
  def warn(s: String): Unit
  def error(s: String): Unit
}

object Log {
  def apply[T](clazz: Class[T], module: String): Log = {
    val log = Loggers.get(clazz)
    new Log {
      override def debug(s: String): Unit = log.debug(s"[$module] $s")
      override def info(s: String): Unit = log.info(s"[$module] $s")
      override def warn(s: String): Unit = log.warn(s"[$module] $s")
      override def error(s: String): Unit = log.error(s"[$module] $s")
    }
  }
}
