/*
 * Sonar Scala Plugin
 * Copyright (C) 2011 - 2013 All contributors
 * dev@sonar.codehaus.org
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package org.sonar.plugins.scala.compiler

import tools.nsc.ast.parser.Tokens._

import java.util.Arrays
import org.junit.runner.RunWith
import org.scalatest.FlatSpec
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.junit.JUnitRunner



import collection.JavaConversions._
import com.buransky.plugins.scala.language.{CommentType, Comment}
import com.buransky.plugins.scala.util.FileTestUtils

@RunWith(classOf[JUnitRunner])
class LexerSpec extends FlatSpec with ShouldMatchers {

  private val lexer = new Lexer()

  private val headerComment = "/*\r\n * This comment describes the\r\n" +
      " * content of the file.\r\n */"

  "A lexer" should "tokenize a simple declaration of a value" in {
    val tokens = lexer.getTokens("val a = " + "\r\n" + "42")
    tokens should equal (Arrays.asList(Token(VAL, 1), Token(IDENTIFIER, 1), Token(EQUALS, 1), Token(INTLIT, 2)))
  }

  it should "tokenize a doc comment" in {
    val comments = getCommentsOf("DocComment1")
    comments should have size(1)
    comments should contain (new Comment("/** Hello World */", CommentType.DOC))
  }

  it should "tokenize a header comment" in {
    val comments = getCommentsOf("SimpleHeaderComment")
    comments should have size(1)
    comments should contain (new Comment(headerComment, CommentType.HEADER))
  }

  it should "not tokenize a header comment when it is not the first comment" in {
    val comments = getCommentsOf("NormalCommentWithHeaderComment")
    comments should have size(2)
    comments should contain (new Comment("// Just a test", CommentType.NORMAL))
    comments should contain (new Comment(headerComment, CommentType.NORMAL))
  }

  it should "not tokenize a header comment when it is not starting with /*" in {
    val comments = getCommentsOf("HeaderCommentWithWrongStart")
    comments should have size(1)
    comments should contain (new Comment("/**\r\n * This comment describes the\r\n" +
      " * content of the file.\r\n */", CommentType.DOC))
  }

  it should "not tokenize a header comment when there was code before" in {
    val comments = getCommentsOf("HeaderCommentWithCodeBefore")
    comments should have size(1)
    comments should contain (new Comment(headerComment, CommentType.NORMAL))
  }

  // TODO add more specs for lexer

  private def getCommentsOf(fileName: String) = {
    val path = FileTestUtils.getRelativePath("/lexer/" + fileName + ".txt")
    lexer.getCommentsOfFile(path)
  }
}