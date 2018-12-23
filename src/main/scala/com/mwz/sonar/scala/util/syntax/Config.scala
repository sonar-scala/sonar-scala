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

import java.nio.file.{Path, Paths}

import cats.instances.string._
import cats.syntax.eq._
import com.mwz.sonar.scala.util.JavaOptionals._
import org.sonar.api.config.Configuration

object Config {
  implicit final class ConfigOps(val configuration: Configuration) extends AnyVal {

    /**
     * Get a list of paths for the given key.
     * Fall back to the default value.
     */
    def getPaths(key: String, default: String): List[Path] =
      configuration
        .get(key)
        .toOption
        .filter(_.nonEmpty)
        .getOrElse(default)
        .split(',') // scalastyle:ignore org.scalastyle.scalariform.NamedArgumentChecker
        .map(p => Paths.get(p.trim))
        .toList

    /**
     * Get a boolean property for the given key.
     * Defaults to false.
     */
    def getValue[T](key: String)(implicit ev: T =:= Boolean): Boolean = {
      configuration
        .get(key)
        .toOption
        .exists(_.toLowerCase === "true")
    }
  }
}
