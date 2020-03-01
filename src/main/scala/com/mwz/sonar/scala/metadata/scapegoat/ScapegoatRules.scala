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
      name = inspection.name,
      mdDescription = inspection.description.getOrElse(""),
      sonarMdDescription = inspection.description.getOrElse("No description"),
      severity = toSeverity(inspection.defaultLevel),
      template = false,
      params = Chain.empty
    )
  }

  private[metadata] def toSeverity(level: Level): Severity = level match {
    case Level.Error   => Severity.Major
    case Level.Warning => Severity.Minor
    case Level.Info    => Severity.Info
  }
}
