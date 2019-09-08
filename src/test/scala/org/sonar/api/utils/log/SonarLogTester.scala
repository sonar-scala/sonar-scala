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

package org.sonar.api.utils.log

import scala.collection.JavaConverters._

import org.scalatest.{BeforeAndAfter, Suite}

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
