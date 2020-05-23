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
import cats.data.NonEmptyChain
import cats.instances.int._
import cats.instances.string._
import cats.syntax.eq._
import cats.syntax.option._
import org.scalastyle.Level
import org.scalastyle._

object ScalastyleRules {
  // TODO: Refactor AllInspections to be a NonEmptyChain.
  lazy val rules: NonEmptyChain[Rule] =
    NonEmptyChain.fromChainUnsafe(
      Chain.fromSeq(ScalastyleInspections.AllInspections.map(toRule))
    )

  private[metadata] def toRule(inspection: ScalastyleInspection): Rule = {
    Rule(
      key = inspection.clazz,
      name = inspection.label,
      mdDescription = mdDescription(inspection),
      sonarMdDescription = sonarMdDescription(inspection),
      severity = toSeverity(inspection.defaultLevel),
      template = inspection.params.nonEmpty,
      params = Chain.fromSeq(inspection.params.map(p => toParam(inspection.clazz, inspection.label, p)))
    )
  }

  private[metadata] def mdDescription(inspection: ScalastyleInspection): String = {
    s"*${inspection.description}*" +
    inspection.justification.map(s => s"\n\n$s").orEmpty +
    inspection.extraDescription.map(s => s"\n\n$s").orEmpty
  }

  private[metadata] def sonarMdDescription(inspection: ScalastyleInspection): String = {
    s"*${inspection.description}*" +
    inspection.justification.map(s => s"\n\n${format(s)}").orEmpty +
    inspection.extraDescription.map(s => s"\n\n${format(s)}").orEmpty
  }

  /**
   * Reformat the text from Scalastyle docs into a SonarQube markdown format.
   */
  private final case class Acc(codeBlock: Boolean, isEmpty: Boolean, text: String, prev: String)
  private[metadata] def format(s: String): String = {
    s.linesIterator
      .foldLeft(Acc(codeBlock = false, isEmpty = true, "", "")) {
        case (acc, l) =>
          // Remove all backslashes as they are unnecessary.
          val line = l.replace("\\", "")
          val trimmed = line.trim
          val trippleQuote = trimmed.contains("```")
          // Replace all code blocks (inline and multiline) with ``.
          val withInlineCode = line.replaceAll("^```(scala)?$", "`").replace("`", "``")
          val trimmedWithInlineCode = withInlineCode.trim

          acc match {
            // Empty line.
            case _ if trimmedWithInlineCode.length === 0 =>
              acc.copy(isEmpty = true)

            // Previous line is code block.
            case Acc(true, isEmpty, text, prev) =>
              if (!trippleQuote) {
                val closed = prev.contains("``") && isEmpty
                val space = if (closed) s" " else s"\n"
                Acc(
                  codeBlock = prev.contains("``") && !isEmpty,
                  isEmpty = false,
                  s"$text$space$withInlineCode",
                  withInlineCode
                )
              } else
                Acc(
                  codeBlock = true,
                  isEmpty = false,
                  s"$text\n$trimmedWithInlineCode",
                  trimmedWithInlineCode
                )

            // Previous line not code block.
            case Acc(false, isEmpty, text, _) =>
              if (!trippleQuote)
                if (isEmpty) {
                  val space = if (text.isEmpty) "" else "\n"
                  Acc(
                    codeBlock = false,
                    isEmpty = false,
                    s"$text$space$trimmedWithInlineCode",
                    trimmedWithInlineCode
                  )
                } else
                  Acc(
                    codeBlock = false,
                    isEmpty = false,
                    s"$text\n$trimmedWithInlineCode",
                    trimmedWithInlineCode
                  )
              else
                Acc(
                  codeBlock = true,
                  isEmpty = false,
                  s"$text\n$trimmedWithInlineCode",
                  trimmedWithInlineCode
                )
          }
      }
      .text
  }

  private[metadata] def toSeverity(level: Level): Severity =
    level match {
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
      case StringType  => ParamType.String
      case IntegerType => ParamType.Integer
      case BooleanType => ParamType.Boolean
    }
}
