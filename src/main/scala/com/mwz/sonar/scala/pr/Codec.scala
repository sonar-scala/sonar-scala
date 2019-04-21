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
package pr

import cats.Applicative
import io.circe.generic.extras.Configuration
import io.circe.{Encoder, Printer}
import org.http4s
import org.http4s.EntityEncoder
import org.http4s.circe.CirceEntityDecoder

import scala.language.higherKinds

object Codec extends CirceEntityDecoder {
  val defaultPrinter: Printer = Printer.noSpaces.copy(dropNullValues = true)
  implicit val config: Configuration = Configuration.default.withSnakeCaseMemberNames

  implicit def jsonEncoderOf[F[_]: Applicative, A](implicit encoder: Encoder[A]): EntityEncoder[F, A] =
    http4s.circe.jsonEncoderWithPrinterOf(defaultPrinter)
}
