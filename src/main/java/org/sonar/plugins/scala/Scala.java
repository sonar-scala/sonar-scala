/*
 * Sonar Scala Plugin
 * Copyright (C) 2016-2016 SonarSource SA
 * mailto:contact AT sonarsource DOT com
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
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.sonar.plugins.scala;

import org.sonar.api.config.Settings;
import org.sonar.api.resources.AbstractLanguage;

import scalariform.lexer.ScalaLexer;
import scalariform.lexer.Token;

import com.google.common.collect.Lists;
import org.apache.commons.lang.StringUtils;

import java.util.List;

/**
 * This language cover JavaServer Pages (Scala).
 *
 */
public class Scala extends AbstractLanguage {

    /**
     * Scala key
     */
    public static final String KEY = "scala";

    /**
     * Scala language name
     */
    public static final String NAME = "Scala";


    /**
     * Key of the file suffix parameter
     */
    public static final String FILE_SUFFIXES_KEY = "sonar.scala.file.suffixes";

    /**
     * Default Java files knows suffixes
     */
    public static final String DEFAULT_FILE_SUFFIXES = ".scala";

    /**
     * Settings of the plugin.
     */
    private final Settings settings;

    /**
     * Default constructor
     */
    public Scala(Settings settings) {
        super(KEY, NAME);
        this.settings = settings;
    }

    /**
     * {@inheritDoc}
     *
     * @see org.sonar.api.resources.AbstractLanguage#getFileSuffixes()
     */
    @Override
    public String[] getFileSuffixes() {
        String[] suffixes = filterEmptyStrings(settings.getStringArray(FILE_SUFFIXES_KEY));
        if (suffixes.length == 0) {
            suffixes = StringUtils.split(DEFAULT_FILE_SUFFIXES, ",");
        }
        return suffixes;
    }

    private static String[] filterEmptyStrings(String[] stringArray) {
        List<String> nonEmptyStrings = Lists.newArrayList();
        for (String string : stringArray) {
            if (StringUtils.isNotBlank(string.trim())) {
                nonEmptyStrings.add(string.trim());
            }
        }
        return nonEmptyStrings.toArray(new String[nonEmptyStrings.size()]);
    }


    public static scala.collection.immutable.List<Token> tokenize(String sourceCode, String scalaVersion) {
        return ScalaLexer.createRawLexer(sourceCode, false, scalaVersion).toList();
    }

}