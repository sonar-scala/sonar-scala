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

import java.util.List;

import org.apache.commons.lang.StringUtils;

/**
 * This class implements the computation of basic
 * line metrics for a {@link ScalaFile}.
 *
 * @author Felix Müller
 * @since 0.1
 */
public class LinesAnalyzer {

  private final List<String> lines;
  private final CommentsAnalyzer commentsAnalyzer;

  public LinesAnalyzer(List<String> lines, CommentsAnalyzer commentsAnalyzer) {
    this.lines = lines;
    this.commentsAnalyzer = commentsAnalyzer;
  }

  public int countLines() {
    return lines.size();
  }

  public int countLinesOfCode() {
    return countLines() - countBlankLines() - commentsAnalyzer.countCommentLines()
        - commentsAnalyzer.countHeaderCommentLines() - commentsAnalyzer.countBlankCommentLines();
  }

  private int countBlankLines() {
    int numberOfBlankLines = 0;
    for (String line : lines) {
      if (StringUtils.isBlank(line)) {
        numberOfBlankLines++;
      }
    }
    return numberOfBlankLines;
  }
}