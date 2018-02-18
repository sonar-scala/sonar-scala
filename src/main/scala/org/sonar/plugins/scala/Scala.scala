/*
 * Sonar Scala Plugin
 * Copyright (C) 2018 All contributors
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
package org.sonar.plugins.scala

import org.sonar.api.config.Configuration
import org.sonar.api.resources.AbstractLanguage

object Scala {
  final val KEY = "scala"
  val Name = "Scala"
  val FileSuffixesKey = "sonar.scala.file.suffixes"
  val DefaultFileSuffixes = List(".scala")
}

class Scala(settings: Configuration) extends AbstractLanguage(Scala.KEY, Scala.Name) {
  override def getFileSuffixes: Array[String] = {
    val suffixes = settings.getStringArray(Scala.FileSuffixesKey).toList
    val filtered = suffixes.filter(_.trim.nonEmpty)
    Some(filtered).filter(_.nonEmpty).getOrElse(Scala.DefaultFileSuffixes).toArray
  }
}
