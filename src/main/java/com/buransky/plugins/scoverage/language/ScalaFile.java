package com.buransky.plugins.scoverage.language;

public class ScalaFile extends org.sonar.api.resources.File {
    public ScalaFile(String key) {
        super(Scala.INSTANCE, key);
    }

    public ScalaFile(String directory, String fileName) {
        super(Scala.INSTANCE, directory, fileName);
    }
}