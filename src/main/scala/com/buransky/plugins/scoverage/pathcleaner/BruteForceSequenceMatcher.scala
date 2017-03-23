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
package com.buransky.plugins.scoverage.pathcleaner

import java.io.File
import org.apache.commons.io.FileUtils
import BruteForceSequenceMatcher._
import com.buransky.plugins.scoverage.util.PathUtil
import scala.collection.JavaConversions._
import org.sonar.api.utils.log.Loggers

object BruteForceSequenceMatcher {

  val extensions = Array[String]("java", "scala")

  type PathSeq = Seq[String]
}

/**
  * Helper that allows to convert a report path into a source folder relative path by testing it against
  * the tree of source files.
  *
  * Assumes that all report paths of a given report have a common root. Dependent of the scoverage
  * report this root is either something outside the actual project (absolute path), the base dir of the project
  * (report path relative to base dir) or some sub folder of the project.
  *
  * By reverse mapping a report path against the tree of all file children of the source folder the correct filesystem file
  * can be found and the report path can be converted into a source dir relative path.  *
  *
  * @author Michael Zinsmaier
  */
class BruteForceSequenceMatcher(baseDir: File, sourcePath: String) extends PathSanitizer {

  private val sourceDir = initSourceDir()
  require(sourceDir.isAbsolute)
  require(sourceDir.isDirectory)

  private val log = Loggers.get(classOf[BruteForceSequenceMatcher])
  private val sourcePathLength = PathUtil.splitPath(sourceDir.getAbsolutePath).size
  private val filesMap = initFilesMap()


  def getSourceRelativePath(reportPath: PathSeq): Option[PathSeq] = {
    // match with file system map of files
    val relPathOption = for {
      absPathCandidates <- filesMap.get(reportPath.last)
      path <- absPathCandidates.find(absPath => absPath.endsWith(reportPath))
    } yield path.drop(sourcePathLength)

    relPathOption
  }

  // mock able helpers that allow us to remove the dependency to the real file system during tests

  private[pathcleaner] def initSourceDir(): File = {
    sourcePath.split(",").headOption.map { first =>
      val sourceDir = new File(baseDir, first)
      sourceDir
    }.getOrElse(null)
  }

  private[pathcleaner] def initFilesMap(): Map[String, Seq[PathSeq]] = {
    val srcFiles = FileUtils.iterateFiles(sourceDir, extensions, true)
    val paths = srcFiles.map(file => PathUtil.splitPath(file.getAbsolutePath)).toSeq

    // group them by filename, in case multiple files have the same name
    paths.groupBy(path => path.last)
  }

}
