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
import io.circe._
import io.circe.generic.JsonCodec
import io.circe.generic.encoding._
import org.sonar.api.server.rule.RuleParamType
import shapeless._
import shapeless.record._

@JsonCodec
final case class RulesRepository(
  key: String,
  name: String,
  rules: NonEmptyChain[Rule]
)

object Rule {
  implicit val ruleEncoder: Encoder[Rule] =
    ReprObjectEncoder.deriveReprAsObjectEncoder.contramap { rule =>
      LabelledGeneric[Rule]
        .to(rule)
        .-(Symbol("sonarMdDescription"))
        .renameField(Symbol("mdDescription"), Symbol("description"))
    }
}

@JsonCodec(decodeOnly = true)
final case class Rule(
  key: String,
  name: String,
  mdDescription: String,
  sonarMdDescription: String,
  severity: Severity,
  template: Boolean,
  params: Chain[Param]
)

@JsonCodec
final case class Param(
  name: String,
  typ: ParamType,
  description: String,
  default: String
)

sealed trait ParamType extends EnumEntry
object ParamType extends Enum[ParamType] with CirceEnum[ParamType] with CatsEnum[ParamType] {
  final case object String extends ParamType
  final case object Text extends ParamType
  final case object Boolean extends ParamType
  final case object Integer extends ParamType
  final case object Float extends ParamType
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
  final case object Info extends Severity
  final case object Minor extends Severity
  final case object Major extends Severity
  final case object Critical extends Severity
  final case object Blocker extends Severity
  val values = findValues
}
