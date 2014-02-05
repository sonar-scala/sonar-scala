package com.buransky.plugins.scoverage.resource;

import com.buransky.plugins.scoverage.language.Scala;
import org.sonar.api.resources.Directory;
import org.sonar.api.resources.Language;
import org.sonar.api.resources.Resource;

public class ScalaDirectory extends Directory {
    private final String name;
    private final ScalaDirectory parent;

    public ScalaDirectory(String key) {
        super(key);

        int i = getKey().lastIndexOf(SEPARATOR);
        if (i > 0) {
            parent = new ScalaDirectory(key.substring(0, i));
            name = key.substring(i + 1);
        }
        else {
            name = key;
            parent = null;
        }
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Language getLanguage() {
        return Scala.INSTANCE;
    }

    @Override
    public Resource getParent() {
        return parent;
    }
}
