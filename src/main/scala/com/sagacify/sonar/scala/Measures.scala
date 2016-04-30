package com.sagacify.sonar.scala

import scala.annotation.tailrec

import scalariform.lexer.ScalaLexer
import scalariform.lexer.Token
import scalariform.lexer.Tokens.LINE_COMMENT
import scalariform.lexer.Tokens.MULTILINE_COMMENT
import scalariform.lexer.Tokens.XML_COMMENT
import scalariform.lexer.Tokens.WS
import scalariform.lexer.Tokens.EOF

object Measures {

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
