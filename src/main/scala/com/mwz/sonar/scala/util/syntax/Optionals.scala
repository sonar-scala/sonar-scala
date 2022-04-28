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

import java.util.Optional

/**
 * Scala.Option <-> Java.Optional conversions.
 */
object Optionals {

  /**
   * Transform Scala Option to a Java Optional.
   */
  implicit final class OptionOps[T >: Null](private val opt: Option[T]) extends AnyVal {
    def toOptional: Optional[T] = Optional.ofNullable(opt.orNull)
  }

  /**
   * Transform Java Optional to a Scala Option.
   */
  implicit final class OptionalOps[T](private val opt: Optional[T]) extends AnyVal {
    def toOption: Option[T] = opt.map[Option[T]](Some(_)).orElse(None)
  }
}
