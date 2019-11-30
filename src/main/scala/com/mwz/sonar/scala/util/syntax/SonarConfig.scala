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
package syntax

import java.nio.file.{Path, Paths}

import cats.instances.string._
import cats.syntax.eq._
import com.mwz.sonar.scala.util.syntax.Optionals._
import org.sonar.api.config.Configuration

object SonarConfig {
  implicit final class ConfigOps(private val configuration: Configuration) extends AnyVal {
    /**
     * Get a list of paths for the given key.
     * Fall back to the default value.
     */
    def getPaths(key: String, default: List[Path]): List[Path] = {
      val values: List[String] =
        configuration
          .getStringArray(key)
          .filter(_.nonEmpty)
          .toList

      if (values.isEmpty) default
      else values.map(p => Paths.get(p.trim))
    }

    /**
     * Get a boolean property for the given key.
     * Defaults to false.
     */
    @SuppressWarnings(Array("UnusedMethodParameter"))
    def getValue[T](key: String)(implicit ev: T =:= Boolean): Boolean = {
      configuration
        .get(key)
        .toOption
        .exists(_.toLowerCase === "true")
    }
  }
}
