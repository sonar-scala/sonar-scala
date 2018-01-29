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
package com.buransky.plugins.scoverage.xml

import java.io.File

import com.buransky.plugins.scoverage._
import com.buransky.plugins.scoverage.util.PathUtil
import org.sonar.api.utils.log.Loggers

import scala.annotation.tailrec
import scala.collection.mutable
import scala.io.Source
import scala.xml.parsing.ConstructingParser
import scala.xml.{MetaData, NamespaceBinding, Text}
import com.buransky.plugins.scoverage.pathcleaner.PathSanitizer

/**
 * Scoverage XML parser based on ConstructingParser provided by standard Scala library.
 *
 * @author Rado Buransky
 */
class XmlScoverageReportConstructingParser(source: Source, pathSanitizer: PathSanitizer) extends ConstructingParser(source, false) {
  private val log = Loggers.get(classOf[XmlScoverageReportConstructingParser])

  private val CLASS_ELEMENT = "class"
  private val FILENAME_ATTRIBUTE = "filename"
  private val STATEMENT_ELEMENT = "statement"
  private val START_ATTRIBUTE = "start"
  private val LINE_ATTRIBUTE = "line"
  private val INVOCATION_COUNT_ATTRIBUTE = "invocation-count"

  val statementsInFile: mutable.HashMap[String, List[CoveredStatement]] = mutable.HashMap.empty
  var currentFilePath: Option[String] = None

  def parse(): ProjectStatementCoverage = {
    // Initialize
    nextch()

    // Parse
    document()

    // Transform map to project
    projectFromMap(statementsInFile.toMap)
  }

  override def elemStart(pos: Int, pre: String, label: String, attrs: MetaData, scope: NamespaceBinding) {
    label match {
      case CLASS_ELEMENT =>
        currentFilePath = Some(fixLeadingSlash(getText(attrs, FILENAME_ATTRIBUTE)))
        log.debug("Current file path: " + currentFilePath.get)

      case STATEMENT_ELEMENT =>
        currentFilePath match {
          case Some(cfp) =>
            val start = getInt(attrs, START_ATTRIBUTE)
            val line = getInt(attrs, LINE_ATTRIBUTE)
            val hits = getInt(attrs, INVOCATION_COUNT_ATTRIBUTE)

            // Add covered statement to the mutable map
            val pos = StatementPosition(line, start)
            addCoveredStatement(cfp, CoveredStatement(pos, pos, hits))

            log.debug("Statement added: " + line + ", " + hits + ", " + start)

          case None => throw new ScoverageException("Current file path not set!")
        }
      case _ => // Nothing to do
    }

    super.elemStart(pos, pre, label, attrs, scope)
  }

  private def addCoveredStatement(filePath: String, coveredStatement: CoveredStatement) {
    statementsInFile.get(filePath) match {
      case None => statementsInFile.put(filePath, List(coveredStatement))
      case Some(s) => statementsInFile.update(filePath, coveredStatement :: s)
    }
  }

  /**
   * Remove this when scoverage is fixed! It's just a hack.
   * Old Scoverage has incorrectly added leading '/' to relative file paths.
   */
  private def fixLeadingSlash(filePath: String) = {
    if (filePath.startsWith(File.separator) && !new File(filePath).exists()) {
      filePath.drop(File.separator.length)
    }
    else
      filePath
  }

  private def getInt(attrs: MetaData, name: String) = getText(attrs, name).toInt

  private def getText(attrs: MetaData, name: String): String = {
    attrs.get(name) match {
      case Some(attr) =>
        attr match {
          case text: Text => text.toString()
          case _ => throw new ScoverageException("Not a text attribute!")
        }
      case None =>  throw new ScoverageException("Attribute doesn't exit! [" + name + "]")
    }
  }

  private case class DirOrFile(name: String, var children: List[DirOrFile],
                               coverage: Option[FileStatementCoverage]) {
    def get(name: String) = children.find(_.name == name)

    @tailrec
    final def add(chain: DirOrFile) {
      get(chain.name) match {
        case None => children = chain :: children
        case Some(child) =>
          chain.children match {
            case h :: t =>
              if (t != Nil)
                throw new IllegalStateException("This is not a linear chain!")
              child.add(h)
            case _ => // Duplicate file? Should not happen.
          }
      }
    }

    def toStatementCoverage: NodeStatementCoverage = {
      val childNodes = children.map(_.toStatementCoverage)

      childNodes match {
        case Nil => coverage match {
          case None => FileStatementCoverage("Nothing", 0, 0, List.empty[CoveredStatement])
          case _ => coverage.get
        }
        case _ => DirectoryStatementCoverage(name, childNodes)
      }
    }

    def toProjectStatementCoverage: ProjectStatementCoverage = {
      toStatementCoverage match {
        case node: NodeStatementCoverage => ProjectStatementCoverage("", node.children)
        case _ => throw new ScoverageException("Illegal statement coverage!")
      }
    }
  }

  private def projectFromMap(statementsInFile: Map[String, List[CoveredStatement]]):
    ProjectStatementCoverage = {

    // Transform to file statement coverage
    val files = fileStatementCoverage(statementsInFile)

    // Transform file paths to chain of case classes
    val chained = files.map(fsc => pathToChain(fsc._1, fsc._2)).flatten

    // Merge chains into one tree
    val root = DirOrFile("", Nil, None)
    chained.foreach(root.add)

    // Transform file system tree into coverage structure tree
    root.toProjectStatementCoverage
  }

  private def pathToChain(filePath: String, coverage: FileStatementCoverage): Option[DirOrFile] = {
    // helper
    def convertToDirOrFile(relPath: Seq[String]) = {
      // Get directories
      val dirs = for (i <- 0 to relPath.length - 2)
        yield DirOrFile(relPath(i), Nil, None)

      // Chain directories
      for (i <- 0 to dirs.length - 2)
        dirs(i).children = List(dirs(i + 1))

      // Get file
      val file = DirOrFile(relPath(relPath.length - 1).toString, Nil, Some(coverage))

      if (dirs.isEmpty) {
        // File in root dir
        file
      } else {
        // Append file
        dirs.last.children = List(file)
        dirs.head
      }
    }
    
    // processing
    val path = PathUtil.splitPath(filePath)

    if (path.length < 1)
      throw new ScoverageException("Path cannot be empty!")

    pathSanitizer.getSourceRelativePath(path) match {
      case Some(relPath) => Some(convertToDirOrFile(relPath))
      case None => {
        log.warn(s"skipping file coverage results for $path, was not able to retrieve the file in the configured source dir")
        None
      }
    }
  }

  private def fileStatementCoverage(statementsInFile: Map[String, List[CoveredStatement]]):
    Map[String, FileStatementCoverage] = {
    statementsInFile.map { sif =>
      val fileStatementCoverage = FileStatementCoverage(PathUtil.splitPath(sif._1).last,
        sif._2.length, coveredStatements(sif._2), sif._2)

      (sif._1, fileStatementCoverage)
    }
  }

  private def coveredStatements(statements: Iterable[CoveredStatement]) =
    statements.count(_.hitCount > 0)
}
