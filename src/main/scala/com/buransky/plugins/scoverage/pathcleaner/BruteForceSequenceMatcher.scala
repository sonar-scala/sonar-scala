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
import com.buransky.plugins.scoverage.util.PathUtil
import com.buransky.plugins.scoverage.util.PathUtil.PathSeq
import scala.collection.JavaConverters._
import scala.annotation.tailrec
import org.sonar.api.utils.log.Loggers

object BruteForceSequenceMatcher {
  val extensions = Seq(".java", ".scala")

  /**
   * Returns a sequence of all files in the given source directory, and all its subdirectories, that has any valid extension
   *
   * @author BalmungSan
   */
  private[pathcleaner] def listFilesRecursive(sourceDir: File): Seq[PathSeq] = {
    @tailrec
    def recursiveSearch(remainingFiles: Vector[File], paths: List[PathSeq]): List[PathSeq] =
      remainingFiles match {
        case file +: rest => {
          if (file.isDirectory) {
            recursiveSearch(rest ++ file.listFiles, paths)
          } else if (file.isFile && extensions.exists(ext => file.getName.endsWith(ext))) {
            val path = PathUtil.splitPath(file.getAbsolutePath)
            recursiveSearch(rest, path :: paths)
          } else {
            recursiveSearch(rest, paths)
          }
        }
        case Vector() => paths
      }

    recursiveSearch(Vector(sourceDir), Nil)
  }
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
      absPathCandidates <- filesMap.get(reportPath.lastOption.getOrElse(""))
      path              <- absPathCandidates.find(absPath => absPath.endsWith(reportPath))
    } yield path.drop(sourcePathLength)

    relPathOption
  }

  // mock able helpers that allow us to remove the dependency to the real file system during tests

  private[pathcleaner] def initSourceDir(): File = {
    sourcePath
      .split(",")
      .headOption
      .map { first =>
        val firstFile = new File(first)
        if (firstFile.isAbsolute) {
          firstFile
        } else {
          val sourceDir = new File(baseDir, first)
          sourceDir
        }
      }
      .orNull
  }

  private[pathcleaner] def initFilesMap(): Map[String, Seq[PathSeq]] = {
    val paths = BruteForceSequenceMatcher.listFilesRecursive(sourceDir)

    // group them by filename, in case multiple files have the same name
    paths.groupBy(path => path.lastOption.getOrElse(""))
  }
}
