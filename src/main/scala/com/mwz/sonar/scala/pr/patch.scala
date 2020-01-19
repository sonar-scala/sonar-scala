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
package pr

import scala.util.Try
import scala.util.matching.Regex

import cats.instances.either._
import cats.instances.list._
import cats.syntax.foldable._

final case class PatchLine(value: Int) {
  def inc: PatchLine = PatchLine(value + 1)
}
final case class FileLine(value: Int) {
  def inc: FileLine = FileLine(value + 1)
}
final case class Patch(fileLine: FileLine, patchLine: PatchLine, fileToPatch: Map[FileLine, PatchLine])
final case class PatchError(text: String)

object Patch {
  private val PatchChunkStartRegex: Regex =
    new Regex("""@@ \-(\d+),(\d+) \+(\d+),(\d+) @@""", "origStart", "origSize", "newStart", "newSize")

  /**
   * Parse a raw patch to extract how file lines are mapped to patch lines.
   */
  // TODO: Maybe it would be more practical if the mapping was reversed: PatchLine -> FileLine?
  def parse(patch: String): Either[PatchError, Map[FileLine, PatchLine]] =
    patch
      .replaceAll("(\r\n)|\r|\n|\\\\n", "\n")
      .lines
      .toList
      .foldLeftM[Either[PatchError, ?], Patch](Patch(FileLine(0), PatchLine(0), Map.empty)) {
        case (patch, line) =>
          line match {
            // Start of a hunk.
            case l if l.startsWith("@@") =>
              // Parse the start of the hunk & get the starting line of the file.
              PatchChunkStartRegex
                .findFirstMatchIn(l)
                .flatMap(regexMatch => Try(regexMatch.group("newStart").toInt).toOption)
                .fold[Either[PatchError, Patch]](Left(PatchError(l)))(start =>
                  Right(Patch(FileLine(start), patch.patchLine.inc, patch.fileToPatch))
                )
            // Keep track of added and context (unmodified) lines.
            case l if l.startsWith("+") || l.startsWith(" ") =>
              Right(
                Patch(
                  patch.fileLine.inc,
                  patch.patchLine.inc,
                  patch.fileToPatch + (patch.fileLine -> patch.patchLine)
                )
              )
            // Skip removed and new lines.
            case _ =>
              Right(Patch(patch.fileLine, patch.patchLine.inc, patch.fileToPatch))
          }
      }
      .map(_.fileToPatch)
      .filterOrElse(_.nonEmpty, PatchError(patch))
}
