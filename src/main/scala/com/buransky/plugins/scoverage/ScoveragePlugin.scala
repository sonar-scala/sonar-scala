/*
 * Sonar Scoverage Plugin
 * Copyright (C) 2013 Rado Buransky
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
package com.buransky.plugins.scoverage

import com.buransky.plugins.scoverage.measure.ScalaMetrics
import com.buransky.plugins.scoverage.sensor.ScoverageSensor
import com.buransky.plugins.scoverage.widget.ScoverageWidget
import org.sonar.api.{Extension, SonarPlugin}

import scala.collection.JavaConversions._
import scala.collection.mutable.ListBuffer

/**
 * Plugin entry point.
 *
 * @author Rado Buransky
 */
class ScoveragePlugin extends SonarPlugin {
  override def getExtensions: java.util.List[Class[_ <: Extension]] =
    ListBuffer(
      classOf[ScoverageExtensionProvider],
      classOf[ScalaMetrics],
      classOf[ScoverageSensor],
      classOf[ScoverageWidget]
    )

  override val toString = getClass.getSimpleName
}
