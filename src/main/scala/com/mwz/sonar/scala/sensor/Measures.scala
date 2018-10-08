/*
 * Sonar Scala Plugin
 * Copyright (C) 2018 All contributors
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
package com.mwz.sonar.scala
package sensor

import scalariform.lexer.{Token, Tokens}

import scala.annotation.tailrec
import scala.util.matching.Regex

/** Scala Sensor Metrics */
object Measures {

  val NewLineRegex: Regex = "(\r\n)|\r|\n".r

  def countClasses(tokens: List[Token]): Int = {
    tokens.foldLeft(0) { // scalastyle:ignore
      case (acc, token) =>
        val tokenType = token.tokenType
        if (tokenType == Tokens.CLASS || tokenType == Tokens.OBJECT) acc + 1
        else acc
    }
  }

  def countMethods(tokens: List[Token]): Int = {
    tokens.foldLeft(0) { // scalastyle:ignore
      case (acc, token) =>
        if (token.tokenType == Tokens.DEF) acc + 1
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
            countCommentLines(tail, i + token.rawText.count(_ == '\n') + 1)
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
            if token.tokenType == Tokens.WS &&
            NewLineRegex.findFirstIn(token.text).nonEmpty =>
          tail
        case token :: tail if token.tokenType == Tokens.LINE_COMMENT =>
          tail
        case token :: tail =>
          getNextLine(tail)
      }
    }

    tokens match {
      case Nil => i
      case token :: tail if token.tokenType == Tokens.WS =>
        countNonCommentLines(tail, i)
      case token :: tail if token.tokenType == Tokens.EOF => i
      case token :: tail =>
        if (!token.tokenType.isNewline & !token.tokenType.isComment)
          countNonCommentLines(getNextLine(tail), i + 1)
        else countNonCommentLines(tail, i)
    }
  }
}
