/*
 * Copyright (C) 2018-2022  All sonar-scala contributors
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
package sensor

import scala.annotation.tailrec
import scala.util.matching.Regex

import cats.kernel.Eq
import cats.syntax.eq._
import scalariform.lexer.Token
import scalariform.lexer.TokenType
import scalariform.lexer.Tokens

/** Scala Sensor Metrics */
object Measures {
  implicit val tokenTypeEq = Eq.fromUniversalEquals[TokenType]
  val NewLineRegex: Regex = "(\r\n)|\r|\n".r

  def countClasses(tokens: List[Token]): Int = {
    tokens.foldLeft(0) { // scalastyle:ignore org.scalastyle.scalariform.NamedArgumentChecker
      case (acc, token) =>
        val tokenType = token.tokenType
        if (tokenType === Tokens.CLASS || tokenType === Tokens.OBJECT) acc + 1
        else acc
    }
  }

  def countMethods(tokens: List[Token]): Int = {
    tokens.foldLeft(0) { // scalastyle:ignore org.scalastyle.scalariform.NamedArgumentChecker
      case (acc, token) =>
        if (token.tokenType === Tokens.DEF) acc + 1
        else acc
    }
  }

  @tailrec
  def countCommentLines(tokens: List[Token], i: Int = 0): Int = {
    tokens match {
      case Nil => i
      case token :: tail if token.tokenType.isComment =>
        token.tokenType match {
          case Tokens.LINE_COMMENT =>
            countCommentLines(tail, i + 1)
          case Tokens.MULTILINE_COMMENT =>
            countCommentLines(tail, i + token.rawText.count(_ === '\n') + 1)
          case Tokens.XML_COMMENT =>
            new scala.NotImplementedError("XML ?!"); i
          case _ => i // Not a comment!
        }
      case _ :: tail => countCommentLines(tail, i)
    }
  }

  @tailrec
  def countNonCommentLines(tokens: List[Token], i: Int = 0): Int = {
    @tailrec
    def getNextLine(tokens: List[Token]): List[Token] = {
      tokens match {
        case Nil =>
          Nil
        case token :: tail
            if token.tokenType === Tokens.WS &&
              NewLineRegex.findFirstIn(token.text).nonEmpty =>
          tail
        case token :: tail if token.tokenType === Tokens.LINE_COMMENT =>
          tail
        case _ :: tail =>
          getNextLine(tail)
      }
    }

    tokens match {
      case Nil => i
      case token :: tail if token.tokenType === Tokens.WS =>
        countNonCommentLines(tail, i)
      case token :: _ if token.tokenType === Tokens.EOF => i
      case token :: tail =>
        if (!token.tokenType.isNewline & !token.tokenType.isComment)
          countNonCommentLines(getNextLine(tail), i + 1)
        else countNonCommentLines(tail, i)
    }
  }
}
