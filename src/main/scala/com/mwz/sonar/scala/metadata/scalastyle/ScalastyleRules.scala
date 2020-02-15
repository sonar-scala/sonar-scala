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
package scalastyle

import cats.data.Chain
import cats.instances.char._
import cats.instances.int._
import cats.instances.string._
import cats.syntax.eq._
import cats.syntax.option._
import com.mwz.sonar.scala.scalastyle.ScalastyleInspection
import com.mwz.sonar.scala.scalastyle.ScalastyleInspections
import com.mwz.sonar.scala.scalastyle.{Param => ScalastyleParam}
import org.scalastyle.Level
import org.scalastyle._

object ScalastyleRules {
  lazy val rules: Chain[Rule] =
    Chain.fromSeq(ScalastyleInspections.AllInspections.map(toRule))

  private[metadata] def toRule(inspection: ScalastyleInspection): Rule = {
    Rule(
      key = inspection.clazz,
      name = inspection.label,
      description = formatDescription(inspection),
      severity = toSeverity(inspection.defaultLevel),
      params = Chain.fromSeq(inspection.params.map(p => toParam(inspection.clazz, inspection.label, p)))
    )
  }

  private[metadata] def formatDescription(inspection: ScalastyleInspection): String = {
    s"*${inspection.description}*" +
    inspection.justification.map(s => s"\n\n${format(s)}").orEmpty +
    inspection.extraDescription.map(s => s"\n\n${format(s)}").orEmpty
  }

  /**
   * Reformat the text from Scalastyle docs into a markdown format.
   */
  private final case class Acc(indent: Boolean, isEmpty: Boolean, text: String)
  private[metadata] def format(s: String): String = {
    s.linesIterator.foldLeft(Acc(indent = false, isEmpty = true, "")) {
      case (acc, l) =>
        // Remove all backslashes as they are unnecessary.
        val line = l.replace("\\", "")
        val trailingSpaces = line.takeWhile(_ === ' ').length
        // Trim the text and replace ` with `` for inline code blocks.
        val trimmed = line.trim.replace("`", "``")

        acc match {
          // Empty line.
          case _ if trimmed.length === 0 =>
            acc.copy(isEmpty = true)

          // Previous line indented.
          case Acc(true, _, text) =>
            if (trailingSpaces <= 2)
              Acc(indent = false, isEmpty = false, s"$text\n`` $trimmed")
            else
              Acc(indent = true, isEmpty = false, s"$text\n$line")

          // Previous line not indented.
          case Acc(false, isEmpty, text) =>
            if (trailingSpaces <= 2)
              if (isEmpty) {
                val space = if (text.isEmpty) "" else "\n"
                Acc(indent = false, isEmpty = false, s"$text$space$trimmed")
              } else
                Acc(indent = false, isEmpty = false, s"$text\n$trimmed")
            else
              Acc(indent = true, isEmpty = false, s"$text\n``\n$line")
        }
    } match {
      // Close code block.
      case Acc(true, _, text) => s"$text\n``"
      case acc                => acc.text
    }
  }

  private[metadata] def toSeverity(level: Level): Severity = level match {
    case InfoLevel    => Severity.Info
    case WarningLevel => Severity.Minor
    case ErrorLevel   => Severity.Major
  }

  private[metadata] def toParam(ruleClass: String, name: String, param: ScalastyleParam): Param = {
    Param(
      name = param.name,
      typ = toParamType(ruleClass, name, param.typ),
      description = s"${param.label}: ${param.description}",
      default = param.default
    )
  }

  private[metadata] def toParamType(ruleClass: String, name: String, typ: ParameterType): ParamType =
    typ match {
      // The TEXT parameter type is used for header parameter of the HeaderMatchesChecker inspection.
      case StringType
          if ruleClass === "org.scalastyle.file.HeaderMatchesChecker" &&
          name === "header" =>
        ParamType.Text
      case StringType =>
        ParamType.String
      case IntegerType =>
        ParamType.Int
      case BooleanType =>
        ParamType.Bool
    }
}
