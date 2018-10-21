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
package org.sonar.api.utils.log

import org.scalatest.{BeforeAndAfter, Suite}

import scala.collection.JavaConverters._

trait SonarLogTester extends BeforeAndAfter { this: Suite =>
  before {
    LogInterceptors.set(new ListInterceptor())
    Loggers.getFactory.setLevel(LoggerLevel.DEBUG)
  }

  after {
    LogInterceptors.set(NullInterceptor.NULL_INSTANCE)
    Loggers.getFactory.setLevel(LoggerLevel.DEBUG)
  }

  def logs: Seq[String] =
    LogInterceptors.get().asInstanceOf[ListInterceptor].logs.asScala

  def logsFor(level: LoggerLevel): Seq[String] =
    LogInterceptors.get().asInstanceOf[ListInterceptor].logs(level).asScala
}
