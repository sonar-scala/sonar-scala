package com.sagacify.sonar.scala

import scala.annotation.tailrec
import scalariform.lexer.ScalaLexer
import scalariform.lexer.Token
import scalariform.lexer.Tokens._

object Measures {
  def count_classes(tokens: List[Token]): Int = {
    var count = 0
    tokens.foreach(token => if (token.tokenType == CLASS || token.tokenType == OBJECT) count += 1)

    count
  }

  final def count_methods(tokens: List[Token]): Int = {
    var count = 0
    tokens.foreach(token => if (token.tokenType == DEF) count += 1)

    count
  }

  /* applied on raw source code */

  /* applied on lines of code */

  /* applied on tokenised code */

  @tailrec
  final def count_comment_lines(tokens: List[Token], i: Int = 0): Int = {
    tokens match {
      case Nil => i
      case token :: tail if token.tokenType.isComment => {
          token.tokenType match {
            case LINE_COMMENT =>
              count_comment_lines(tail, i + 1)
            case MULTILINE_COMMENT =>
              count_comment_lines(tail, i + token.rawText.count(_ == '\n') + 1)
            case XML_COMMENT =>
              new scala.NotImplementedError("XML ?!"); i
          }
        }
      case _ :: tail => count_comment_lines(tail, i)
    }
  }

  @tailrec
  final def count_ncloc(tokens: List[Token], i: Int = 0): Int = {

    @tailrec
    def get_next_line(tokens: List[Token]): List[Token] = {
      tokens match {
        case Nil => Nil
        case token :: tail if token.tokenType == WS &&
                              token.text.contains('\n') => tail
        case token :: tail if token.tokenType == LINE_COMMENT => tail
        case token :: tail => get_next_line(tail)
      }
    }

    tokens match {
      case Nil => i
      case token :: tail if token.tokenType == WS => count_ncloc(tail, i)
      case token :: tail if token.tokenType == EOF => i
      case token :: tail =>
        if( !token.tokenType.isNewline & !token.tokenType.isComment) {
          count_ncloc(get_next_line(tail), i + 1)
        } else {
          count_ncloc(tail, i)
        }
    }
  }

}
