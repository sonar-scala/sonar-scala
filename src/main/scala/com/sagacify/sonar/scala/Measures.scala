package com.sagacify.sonar.scala

import scala.annotation.tailrec
import scalariform.lexer.ScalaLexer
import scalariform.lexer.Token
import scalariform.lexer.Tokens._

object Measures {
  def countClasses(tokens: List[Token]): Int = {
    var count = 0
    tokens.foreach(token => if (token.tokenType == CLASS || token.tokenType == OBJECT) count += 1)
    count
  }

  def countMethods(tokens: List[Token]): Int = {
    var count = 0
    tokens.foreach(token => if (token.tokenType == DEF) count += 1)
    count
  }

  /* applied on raw source code */

  /* applied on lines of code */

  /* applied on tokenised code */

  @tailrec
  def countCommentLines(tokens: List[Token], i: Int = 0): Int = {
    tokens match {
      case Nil => i
      case token :: tail if token.tokenType.isComment =>
        token.tokenType match {
          case LINE_COMMENT =>
            countCommentLines(tail, i + 1)
          case MULTILINE_COMMENT =>
            countCommentLines(tail, i + token.rawText.count(_ == '\n') + 1)
          case XML_COMMENT =>
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
        case token :: tail if token.tokenType == WS && token.text.contains('\n') =>
          tail
        case token :: tail if token.tokenType == LINE_COMMENT =>
          tail
        case token :: tail =>
          getNextLine(tail)
      }
    }

    tokens match {
      case Nil => i
      case token :: tail if token.tokenType == WS =>
        countNonCommentLines(tail, i)
      case token :: tail if token.tokenType == EOF => i
      case token :: tail =>
        if (!token.tokenType.isNewline & !token.tokenType.isComment)
          countNonCommentLines(getNextLine(tail), i + 1)
        else countNonCommentLines(tail, i)
    }
  }
}
