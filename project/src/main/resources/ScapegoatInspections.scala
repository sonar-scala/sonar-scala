/*
 * Sonar Scala Plugin
 * Copyright (C) 2018 All contributors
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not write to the Free Software Foundation
 * Inc. 51 Franklin Street Fifth Floor Boston MA 02110-1301 USA.
 */
package com.mwz.sonar.scala
package scapegoat

import org.sonar.api.batch.rule.Severity

private[scapegoat] sealed trait Level {
  def toRuleSeverity: Severity = this match {
    case Level.Error   => Severity.MAJOR
    case Level.Warning => Severity.MINOR
    case Level.Info    => Severity.INFO
  }
}

private[scapegoat] object Level {
  case object Error extends Level
  case object Warning extends Level
  case object Info extends Level
}

private[scapegoat] final case class ScapegoatInspection(
  id: String,
  name: String,
  description: Option[String],
  defaultLevel: Level
)

object ScapegoatInspections {
  val AllInspections: List[ScapegoatInspection] = ???
}
