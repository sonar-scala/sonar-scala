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

import java.nio.file.Paths

import cats.data.NonEmptyChain
import cats.effect.Blocker
import cats.effect.ExitCode
import cats.effect.IO
import cats.effect.IOApp
import cats.syntax.functor._
import com.mwz.sonar.scala.metadata._
import com.mwz.sonar.scala.metadata.scalastyle.ScalastyleRules
import com.mwz.sonar.scala.metadata.scalastyle.ScalastyleRulesRepository
import fs2.Stream
import fs2.io.file._
import fs2.text
import io.circe.Printer
import io.circe.generic.auto._
import io.circe.syntax._

private final case class SonarScalaMetadata(
  rules: Rules,
  repositories: Map[String, RulesRepository]
)
private final case class Rules(
  scalastyle: NonEmptyChain[Rule]
)

object Metadata extends IOApp {
  private val metadata: SonarScalaMetadata =
    SonarScalaMetadata(
      Rules(ScalastyleRules.rules),
      Map(
        ScalastyleRulesRepository.RepositoryKey ->
        ScalastyleRulesRepository.rulesRepository
      )
    )
  private val printer: Printer =
    Printer.spaces2SortKeys.copy(
      colonLeft = "",
      lbraceLeft = "",
      rbraceRight = "",
      lbracketLeft = "",
      lrbracketsEmpty = "",
      rbracketRight = "",
      arrayCommaLeft = "",
      objectCommaLeft = ""
    )

  def run(args: List[String]): IO[ExitCode] = {
    val write: Stream[IO, Unit] = Stream.resource(Blocker[IO]).flatMap { blocker =>
      Stream[IO, String](metadata.asJson.printWith(printer))
        .through(text.utf8Encode)
        .through(writeAll(Paths.get("sonar-scala-metadata.json"), blocker))
    }
    write.compile.drain.as(ExitCode.Success)
  }
}
