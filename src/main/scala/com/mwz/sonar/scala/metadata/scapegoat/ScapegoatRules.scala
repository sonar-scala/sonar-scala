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

package com.mwz.sonar.scala.metadata
package scapegoat

import cats.data.Chain
import cats.data.NonEmptyChain

object ScapegoatRules {
  // TODO: Refactor AllInspections to be a NonEmptyChain.
  lazy val rules: NonEmptyChain[Rule] =
    NonEmptyChain.fromChainUnsafe(
      Chain.fromSeq(ScapegoatInspections.AllInspections.map(toRule))
    )

  private[metadata] def toRule(inspection: ScapegoatInspection): Rule = {
    Rule(
      key = inspection.id,
      name = inspection.name.replaceAll("`", ""),
      mdDescription = mdDescription(inspection),
      sonarMdDescription = sonarMdDescription(inspection),
      severity = toSeverity(inspection.defaultLevel),
      template = false,
      params = Chain.empty
    )
  }

  private[metadata] def mdDescription(inspection: ScapegoatInspection): String =
    s"*${inspection.description}*" +
    s"\n\n${inspection.explanation}"

  // SonarQube's markdown parser converts any '=' characters in the middle of a sentence
  // into headings, so we're using a 7th level heading (which doesn't have any style properties)
  // to make it ignore the '=' characters and make it look like regular text.
  // Any inline code blocks between `` are also ignored until Scapegoat has all code blocks wrapped in ``.
  private[metadata] def sonarMdDescription(inspection: ScapegoatInspection): String =
    s"*${inspection.description.replaceAll("`", "")}*" +
    s"\n\n======= ${inspection.explanation.replaceAll("`", "")}"

  private[metadata] def toSeverity(level: Level): Severity =
    level match {
      case Level.Error   => Severity.Major
      case Level.Warning => Severity.Minor
      case Level.Info    => Severity.Info
    }
}
