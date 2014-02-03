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

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.sourceforge.pmd.cpd.AbstractLanguage;
import net.sourceforge.pmd.cpd.TokenEntry;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;
import org.sonar.duplications.cpd.CPD;
import org.sonar.duplications.cpd.Match;

public class ScalaTokenizerTest {

  @Before
  public void init() {
    TokenEntry.clearImages();
  }

  @Test
  public void noDuplications() throws IOException {
    CPD cpd = getCPD(10);
    cpd.add(resourceToFile("/cpd/NoDuplications.scala"));
    cpd.go();
    assertThat(getMatches(cpd).size(), is(0));
  }

  @Test
  public void noDuplicationsWith6Tokens() throws IOException {
    CPD cpd = getCPD(6);
    cpd.add(resourceToFile("/cpd/Duplications5Tokens.scala"));
    cpd.go();
    assertThat(getMatches(cpd).size(), is(0));
  }

  @Test
  public void duplicationWith5Tokens() throws IOException {
    CPD cpd = getCPD(5);
    cpd.add(resourceToFile("/cpd/Duplications5Tokens.scala"));
    cpd.go();
    List<Match> matches = getMatches(cpd);
    assertThat(matches.size(), is(1));
    assertThat(matches.get(0).getFirstMark().getBeginLine(), is(2));
    assertThat(matches.get(0).getSecondMark().getBeginLine(), is(5));
  }

  @Test
  public void newLineTokenEnables5TokenDuplication() throws IOException {
    CPD cpd = getCPD(5);
    cpd.add(resourceToFile("/cpd/NewlineToken.scala"));
    cpd.go();
    List<Match> matches = getMatches(cpd);
    assertThat(matches.get(0).getFirstMark().getBeginLine(), is(2));
    assertThat(matches.get(0).getSecondMark().getBeginLine(), is(3));
  }

  @Test
  public void newLineAndNewLinesTokensNo5TokensDuplication() throws IOException {
    CPD cpd = getCPD(5);
    cpd.add(resourceToFile("/cpd/NewlinesToken.scala"));
    cpd.go();
    assertThat(getMatches(cpd).size(), is(0));
  }

  @Test
  public void twoDuplicatedBlocks() throws IOException {
    CPD cpd = getCPD(5);
    cpd.add(resourceToFile("/cpd/TwoDuplicatedBlocks.scala"));
    cpd.go();
    List<Match> matches = getMatches(cpd);
    assertThat(matches.get(0).getFirstMark().getBeginLine(), is(2));
    assertThat(matches.get(0).getSecondMark().getBeginLine(), is(7));
    assertThat(matches.get(0).getLineCount(), is(4));
  }

  private File resourceToFile(String path) {
    return FileUtils.toFile(getClass().getResource(path));
  }

  private CPD getCPD(int minimumTokens) {
    AbstractLanguage language = new AbstractLanguage(new ScalaTokenizer(), "scala") {
    };
    CPD cpd = new CPD(minimumTokens, language);
    cpd.setEncoding(Charset.defaultCharset().name());
    cpd.setLoadSourceCodeSlices(false);
    return cpd;
  }

  private List<Match> getMatches(CPD cpd) {
    List<Match> matches = new ArrayList<Match>();

    Iterator<Match> iterator = cpd.getMatches();
    while (iterator.hasNext()) {
      matches.add(iterator.next());
    }

    return matches;
  }

}
