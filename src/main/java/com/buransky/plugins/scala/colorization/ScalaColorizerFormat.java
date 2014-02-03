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
package com.buransky.plugins.scala.colorization;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.buransky.plugins.scala.language.Scala;
import org.sonar.api.web.CodeColorizerFormat;
import org.sonar.colorizer.CDocTokenizer;
import org.sonar.colorizer.CppDocTokenizer;
import org.sonar.colorizer.JavaAnnotationTokenizer;
import org.sonar.colorizer.JavadocTokenizer;
import org.sonar.colorizer.KeywordsTokenizer;
import org.sonar.colorizer.LiteralTokenizer;
import org.sonar.colorizer.Tokenizer;

/**
 * This class extends Sonar for code colorization of Scala source.
 *
 * @author Felix Müller
 * @since 0.1
 */
public class ScalaColorizerFormat extends CodeColorizerFormat {

  private static final String END_SPAN_TAG = "</span>";

  private static final List<Tokenizer> TOKENIZERS = Arrays.asList(
      new LiteralTokenizer("<span class=\"s\">", END_SPAN_TAG),
      new KeywordsTokenizer("<span class=\"k\">", END_SPAN_TAG, ScalaKeywords.getAllKeywords()),
      new CDocTokenizer("<span class=\"cd\">", END_SPAN_TAG),
      new CppDocTokenizer("<span class=\"cppd\">", END_SPAN_TAG),
      new JavadocTokenizer("<span class=\"j\">", END_SPAN_TAG),
      new JavaAnnotationTokenizer("<span class=\"a\">", END_SPAN_TAG));

  public ScalaColorizerFormat() {
    super(Scala.INSTANCE.getKey());
  }

  @Override
  public List<Tokenizer> getTokenizers() {
    return Collections.unmodifiableList(TOKENIZERS);
  }
}