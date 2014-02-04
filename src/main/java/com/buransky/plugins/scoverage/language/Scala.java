package com.buransky.plugins.scoverage.language;

import org.sonar.api.resources.AbstractLanguage;

public class Scala extends AbstractLanguage {

  public static final Scala INSTANCE = new Scala();

  public Scala() {
    super("scala", "Scala");
  }

  public String[] getFileSuffixes() {
    return new String[] { "scala" };
  }
}