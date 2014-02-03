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
package com.buransky.plugins.scala.cobertura;

import com.buransky.plugins.scala.language.Scala;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.CoverageExtension;
import org.sonar.api.batch.Sensor;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.resources.Project;
import org.sonar.plugins.cobertura.api.AbstractCoberturaParser;
import org.sonar.plugins.cobertura.api.CoberturaUtils;

import java.io.File;

public class CoberturaSensor implements Sensor, CoverageExtension {

  private static final Logger LOG = LoggerFactory.getLogger(CoberturaSensor.class);
  private static final AbstractCoberturaParser COBERTURA_PARSER = new ScalaCoberturaParser();

  public boolean shouldExecuteOnProject(Project project) {
    return project.getAnalysisType().isDynamic(true) && Scala.INSTANCE.getKey().equals(project.getLanguageKey());
  }

  public void analyse(Project project, SensorContext context) {
    File report = CoberturaUtils.getReport(project);
    if (report != null) {
      parseReport(report, context);
    }
  }

  protected void parseReport(File xmlFile, final SensorContext context) {
    LOG.info("parsing {}", xmlFile);
    COBERTURA_PARSER.parseReport(xmlFile, context);
  }

  @Override
  public String toString() {
    return "Scala CoberturaSensor";
  }

}
