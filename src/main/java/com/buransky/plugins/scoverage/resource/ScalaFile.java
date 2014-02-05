package com.buransky.plugins.scoverage.resource;

import com.buransky.plugins.scoverage.language.Scala;
import org.sonar.api.resources.File;
import org.sonar.api.resources.Language;
import org.sonar.api.resources.Resource;

public class ScalaFile extends Resource<ScalaDirectory> {
    private final File file;
    private ScalaDirectory parent;

    public ScalaFile(String key) {
        if (key == null)
            throw new IllegalArgumentException("Key cannot be null!");

        file = new File(key);
        setKey(key);
    }

    @Override
    public String getName() {
        return file.getName();
    }

    @Override
    public String getLongName() {
        return file.getLongName();
    }

    @Override
    public String getDescription() {
        return file.getDescription();
    }

    @Override
    public Language getLanguage() {
        return Scala.INSTANCE;
    }

    @Override
    public String getScope() {
        return file.getScope();
    }

    @Override
    public String getQualifier() {
        return file.getQualifier();
    }

    @Override
    public ScalaDirectory getParent() {
        if (parent == null) {
            parent = new ScalaDirectory(file.getParent().getKey());
        }
        return parent;
    }

    @Override
    public boolean matchFilePattern(String antPattern) {
        return file.matchFilePattern(antPattern);
    }

    @Override
    public String toString() {
        return file.toString();
    }
}