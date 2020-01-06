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
package scalastyle

import org.scalastyle._

final case class ScalastyleInspection(
  clazz: String,
  id: String,
  label: String,
  description: String,
  extraDescription: Option[String],
  justification: Option[String],
  defaultLevel: Level,
  params: Seq[Param]
)

final case class Param(
  name: String,
  typ: ParameterType,
  label: String,
  description: String,
  default: String
)

object ScalastyleInspections {
  val AllInspections: List[ScalastyleInspection] = ???
  val AllInspectionsByClass: Map[String, ScalastyleInspection] =
    AllInspections.map(i => i.clazz -> i).toMap
}
