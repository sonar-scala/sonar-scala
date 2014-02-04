package com.buransky.plugins.scoverage.language;

import org.apache.commons.lang.StringUtils;
import org.sonar.api.resources.*;
import org.sonar.api.utils.WildcardPattern;

import java.io.File;

public class ScalaFile extends Resource<Directory> {

    private final boolean isUnitTest;
    private final String directory;
    private final String fileName;
    private final Directory parent;

    public ScalaFile(String directory, String fileName, boolean isUnitTest) {
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

    /**
     * Shortcut for {@link #fromInputFile(org.sonar.api.resources.InputFile, boolean)} for source files.
     */
    public static ScalaFile fromInputFile(InputFile inputFile) {
        return ScalaFile.fromInputFile(inputFile, false);
    }

    /**
     * Creates a {@link ScalaFile} from a file in the source directories.
     *
     * @param inputFile  the file object with relative path
     * @param isUnitTest whether it is a unit test file or a source file
     * @return the {@link ScalaFile} created if exists, null otherwise
     */
    public static ScalaFile fromInputFile(InputFile inputFile, boolean isUnitTest) {
        if (inputFile == null || inputFile.getFile() == null || inputFile.getRelativePath() == null) {
            return null;
        }

        return new ScalaFile(inputFile.getFileBaseDir().getPath(), inputFile.getFile().getName(), isUnitTest);
    }
}