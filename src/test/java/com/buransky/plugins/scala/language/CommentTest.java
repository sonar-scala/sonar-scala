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
package com.buransky.plugins.scala.language;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.io.IOException;

import org.junit.Test;

public class CommentTest {

  @Test
  public void shouldCountOneNumberOfCommentLine() throws IOException {
    Comment comment = new Comment("// This is a comment", CommentType.NORMAL);
    assertThat(comment.getNumberOfLines(), is(1));
  }

  @Test
  public void shouldCountAllNumberOfCommentLines() throws IOException {
    Comment comment = new Comment("/* This is the first comment line\r\n"
          + "* second line\r\n"
          + "* and this the third and last line */",
        CommentType.NORMAL);
    assertThat(comment.getNumberOfLines(), is(3));
  }

  @Test
  public void shouldCountZeorCommentLinesIfCommentIsEmpty() throws IOException {
    Comment comment = new Comment("", CommentType.NORMAL);
    assertThat(comment.getNumberOfLines(), is(0));
  }

  @Test
  public void shouldCountOneCommentedOutLineOfCode() throws IOException {
    Comment comment = new Comment("// val a = 1", CommentType.NORMAL);
    assertThat(comment.getNumberOfCommentedOutLinesOfCode(), is(1));
  }

  @Test
  public void shouldCountAllCommentedOutLinesOfCode() throws IOException {
    Comment comment = new Comment("/* object Hello {\r\n"
          + "*   val b = 1 } */",
        CommentType.NORMAL);
    assertThat(comment.getNumberOfCommentedOutLinesOfCode(), is(2));
  }

  @Test
  public void shouldCountZeorCommentedOutLinesOfCodeIfCommentIsEmpty() throws IOException {
    Comment comment = new Comment("", CommentType.NORMAL);
    assertThat(comment.getNumberOfCommentedOutLinesOfCode(), is(0));
  }

  @Test
  public void shouldNotCountAnyCommentedOutLinesOfCodeForDocComments() throws IOException {
    Comment comment = new Comment("/** This is a doc comment with some code\r\n"
        + "* package hello.world\r\n"
        + "* class Test { val a = 1 } */", CommentType.DOC);
    assertThat(comment.getNumberOfCommentedOutLinesOfCode(), is(0));
  }

  @Test
  public void shouldCountAllBlankCommentLines() throws IOException {
    Comment comment = new Comment("/*\r\n"
    		+ "* this is a multi line comment with some blank lines\r\n"
        + "* \t \t  \r\n"
        + "*/", CommentType.NORMAL);
    assertThat(comment.getNumberOfBlankLines(), is(3));
  }

  @Test
  public void shouldBeNormalComment() throws IOException {
    Comment comment = new Comment("", CommentType.NORMAL);
    assertThat(comment.isDocComment(), is(false));
    assertThat(comment.isHeaderComment(), is(false));
  }

  @Test
  public void shouldBeDocComment() throws IOException {
    Comment comment = new Comment("", CommentType.DOC);
    assertThat(comment.isDocComment(), is(true));
    assertThat(comment.isHeaderComment(), is(false));
  }

  @Test
  public void shouldBeHeaderComment() throws IOException {
    Comment comment = new Comment("", CommentType.HEADER);
    assertThat(comment.isDocComment(), is(false));
    assertThat(comment.isHeaderComment(), is(true));
  }
}