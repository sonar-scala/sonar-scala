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

import cats.data.Chain
import cats.data.NonEmptyChain
import enumeratum._
import org.sonar.api.server.rule.RuleParamType

final case class RulesRepository(
  key: String,
  name: String,
  rules: NonEmptyChain[Rule]
)

final case class Rule(
  key: String,
  name: String,
  description: String,
  severity: Severity,
  template: Boolean,
  params: Chain[Param]
)

final case class Param(
  name: String,
  typ: ParamType,
  description: String,
  default: String
)

sealed trait ParamType extends EnumEntry
object ParamType extends Enum[ParamType] with CirceEnum[ParamType] with CatsEnum[ParamType] {
  case object String extends ParamType
  case object Text extends ParamType
  case object Boolean extends ParamType
  case object Integer extends ParamType
  case object Float extends ParamType
  val values = findValues

  implicit class ParamTypeSyntax(private val paramType: ParamType) extends AnyVal {
    def asSonarRuleParamType: RuleParamType = paramType match {
      case String  => RuleParamType.STRING
      case Text    => RuleParamType.TEXT
      case Boolean => RuleParamType.BOOLEAN
      case Integer => RuleParamType.INTEGER
      case Float   => RuleParamType.FLOAT
    }
  }
}

sealed trait Severity extends EnumEntry
object Severity extends Enum[Severity] with CirceEnum[Severity] with CatsEnum[Severity] {
  case object Info extends Severity
  case object Minor extends Severity
  case object Major extends Severity
  case object Critical extends Severity
  case object Blocker extends Severity
  val values = findValues
}
