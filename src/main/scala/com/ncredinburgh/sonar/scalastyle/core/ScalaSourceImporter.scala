/*
 * Sonar Scala Style Plugin
 * Copyright (C) 2014 All contributors
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
package com.ncredinburgh.sonar.scalastyle.core

/**
 * Created by hc185053 on 13/06/2014.
 */
import org.sonar.api.batch.AbstractSourceImporter;
import org.sonar.api.batch.Phase;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.resources.InputFileUtils;
import org.sonar.api.resources.ProjectFileSystem;

@Phase(name = Phase.Name.PRE)
class ScalaSourceImporter(lang : ScalaLanguage) extends AbstractSourceImporter(lang) {

  override def analyse( fileSystem : ProjectFileSystem,  context : SensorContext) : Unit = {
    parseDirs(context, InputFileUtils.toFiles(fileSystem.mainFiles(ScalaLanguage.KEY)), fileSystem.getSourceDirs(), false, fileSystem.getSourceCharset());
  }

  override def toString() : String = getClass().getSimpleName()

}
