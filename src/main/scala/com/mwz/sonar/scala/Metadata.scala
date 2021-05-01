/*
 * Copyright (C) 2018-2021  All sonar-scala contributors
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
import java.nio.file.StandardOpenOption

import cats.data.NonEmptyChain
import cats.effect.ExitCode
import cats.effect.IO
import cats.effect.IOApp
import com.mwz.sonar.scala.metadata._
import com.mwz.sonar.scala.metadata.scalastyle.ScalastyleRules
import com.mwz.sonar.scala.metadata.scalastyle.ScalastyleRulesRepository
import com.mwz.sonar.scala.metadata.scapegoat.ScapegoatRules
import com.mwz.sonar.scala.metadata.scapegoat.ScapegoatRulesRepository
import fs2.Stream
import fs2.io.file._
import fs2.text
import io.circe.Printer
import io.circe.generic.JsonCodec
import io.circe.syntax._
import cats.effect.Resource

@JsonCodec
final case class SonarScalaMetadata(
  rules: Rules,
  repositories: Map[String, RulesRepository]
)

@JsonCodec
final case class Rules(
  scalastyle: NonEmptyChain[Rule],
  scapegoat: NonEmptyChain[Rule]
)

object Metadata extends IOApp {
  private val metadata: SonarScalaMetadata =
    SonarScalaMetadata(
      rules = Rules(sort(ScalastyleRules.rules), sort(ScapegoatRules.rules)),
      repositories = Map(
        ScalastyleRulesRepository.RepositoryKey ->
        ScalastyleRulesRepository.rulesRepository
          .copy(rules = sort(ScalastyleRulesRepository.rulesRepository.rules)),
        ScapegoatRulesRepository.RepositoryKey ->
        ScapegoatRulesRepository.rulesRepository
          .copy(rules = sort(ScapegoatRulesRepository.rulesRepository.rules))
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

  // Chain is missing sortBy, which should be added in 2.2.0.
  private def sort(rules: NonEmptyChain[Rule]): NonEmptyChain[Rule] =
    NonEmptyChain.fromNonEmptyList(rules.toNonEmptyList.sortBy(_.name))

  def run(args: List[String]): IO[ExitCode] = {
    val write: Stream[IO, Unit] = Stream.resource(Resource.unit[IO]).flatMap { blocker =>
      Stream[IO, String](metadata.asJson.printWith(printer))
        .through(text.utf8Encode)
        .through(
          writeAll(
            Paths.get("sonar-scala-metadata.json"),
            blocker,
            List(StandardOpenOption.TRUNCATE_EXISTING)
          )
        )
    }
    write.compile.drain.as(ExitCode.Success)
  }
}
