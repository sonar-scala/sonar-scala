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
package com.buransky.plugins.scala.metrics;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Test;
import com.buransky.plugins.scala.language.Comment;
import com.buransky.plugins.scala.language.CommentType;

import scala.actors.threadpool.Arrays;

public class CommentsAnalyzerTest {

  @Test
  public void shouldCountAllCommentLines() throws IOException {
    List<String> comments = Arrays.asList(new String[] {
        "// this a normal comment",
        "/* this is a normal multiline coment\r\n* last line of this comment */",
        "// also a normal comment"
      });
    CommentsAnalyzer commentAnalyzer = new CommentsAnalyzer(asCommentList(comments, CommentType.NORMAL));
    assertThat(commentAnalyzer.countCommentLines(), is(4));
  }

  @Test
  public void shouldCountAllHeaderCommentLines() throws IOException {
    List<String> comments = Arrays.asList(new String[] {
        "/* this is an one line header comment */",
        "/* this is a normal multiline header coment\r\n* last line of this comment */",
        "/* also a normal header comment */"
      });
    CommentsAnalyzer commentAnalyzer = new CommentsAnalyzer(asCommentList(comments, CommentType.HEADER));
    assertThat(commentAnalyzer.countHeaderCommentLines(), is(4));
  }

  @Test
  public void shouldCountAllCommentedOutLinesOfCode() throws IOException {
    List<String> comments = Arrays.asList(new String[] {
        "// val a = 12",
        "/* list.foreach(println(_))\r\n* def inc(x: Int) = x + 1 */",
        "// this a normal comment"
      });
    CommentsAnalyzer commentAnalyzer = new CommentsAnalyzer(asCommentList(comments, CommentType.NORMAL));
    assertThat(commentAnalyzer.countCommentedOutLinesOfCode(), is(3));
  }

  @Test
  public void shouldCountZeroCommentLinesForEmptyCommentsList() {
    CommentsAnalyzer commentAnalyzer = new CommentsAnalyzer(Collections.<Comment>emptyList());
    assertThat(commentAnalyzer.countCommentLines(), is(0));
  }

  @Test
  public void shouldCountZeroHeaderCommentLinesForEmptyCommentsList() {
    CommentsAnalyzer commentAnalyzer = new CommentsAnalyzer(Collections.<Comment>emptyList());
    assertThat(commentAnalyzer.countHeaderCommentLines(), is(0));
  }

  @Test
  public void shouldCountZeroCommentedOutLinesOfCodeForEmptyCommentsList() {
    CommentsAnalyzer commentAnalyzer = new CommentsAnalyzer(Collections.<Comment>emptyList());
    assertThat(commentAnalyzer.countCommentedOutLinesOfCode(), is(0));
  }

  private List<Comment> asCommentList(List<String> commentsContent, CommentType type) throws IOException {
    List<Comment> comments = new ArrayList<Comment>();
    for (String comment : commentsContent) {
      comments.add(new Comment(comment, type));
    }
    return comments;
  }
}