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

import org.apache.commons.lang.StringUtils;
import org.sonar.api.resources.*;
import org.sonar.api.utils.WildcardPattern;

import java.io.File;

/**
 * This class implements a Scala source file for Sonar.
 *
 * @author Felix Müller
 * @since 0.1
 */
public class ScalaRealFile extends Resource<Directory> {

    private final boolean isUnitTest;
    private final String directory;
    private final String fileName;
    private final Directory parent;

    public ScalaRealFile(String directory, String fileName, boolean isUnitTest) {
        super();
        this.isUnitTest = isUnitTest;

        this.directory = (directory == null) ? "" : directory.trim();
        this.fileName = fileName.trim();

        parent = new Directory(directory);
        setKey(getLongName());
    }

    @Override
    public String getName() {
        return fileName;
    }

    @Override
    public String getLongName() {
        return directory + File.pathSeparatorChar + fileName;
    }

    @Override
    public String getDescription() {
        return null;
    }

    @Override
    public Language getLanguage() {
        return Scala.INSTANCE;
    }

    @Override
    public String getScope() {
        return Scopes.FILE;
    }

    @Override
    public String getQualifier() {
        return isUnitTest ? Qualifiers.UNIT_TEST_FILE : Qualifiers.FILE;
    }

    @Override
    public Directory getParent() {
        return parent;
    }

    @Override
    public boolean matchFilePattern(String antPattern) {
        final String patternWithoutFileSuffix = StringUtils.substringBeforeLast(antPattern, ".");
        final WildcardPattern matcher = WildcardPattern.create(patternWithoutFileSuffix, ".");
        return matcher.match(getKey());
    }

    public boolean isUnitTest() {
        return isUnitTest;
    }

    /**
     * Shortcut for {@link #fromInputFile(org.sonar.api.resources.InputFile, boolean)} for source files.
     */
    public static ScalaRealFile fromInputFile(InputFile inputFile) {
        return ScalaRealFile.fromInputFile(inputFile, false);
    }

    /**
     * Creates a {@link com.buransky.plugins.scala.language.ScalaRealFile} from a file in the source directories.
     *
     * @param inputFile  the file object with relative path
     * @param isUnitTest whether it is a unit test file or a source file
     * @return the {@link com.buransky.plugins.scala.language.ScalaRealFile} created if exists, null otherwise
     */
    public static ScalaRealFile fromInputFile(InputFile inputFile, boolean isUnitTest) {
        if (inputFile == null || inputFile.getFile() == null || inputFile.getRelativePath() == null) {
            return null;
        }

        return new ScalaRealFile(inputFile.getFileBaseDir().getPath(), inputFile.getFile().getName(), isUnitTest);
    }
}