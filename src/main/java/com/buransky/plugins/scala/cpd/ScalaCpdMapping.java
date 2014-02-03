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
package com.buransky.plugins.scala.cpd;

import com.buransky.plugins.scala.language.Scala;
import net.sourceforge.pmd.cpd.Tokenizer;

import org.sonar.api.batch.AbstractCpdMapping;
import org.sonar.api.resources.Language;

/**
 * Glue Sonar and PMD CPD together.
 *
 * @since 0.1
 */
public class ScalaCpdMapping extends AbstractCpdMapping {

  private final Scala scala;

  public ScalaCpdMapping(Scala scala) {
    this.scala = scala;
  }

  public Tokenizer getTokenizer() {
    return new ScalaTokenizer();
  }

  public Language getLanguage() {
    return scala;
  }

}
