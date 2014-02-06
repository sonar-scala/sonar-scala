package com.buransky.plugins.scoverage.xml

import com.buransky.plugins.scoverage._
import scala.io.Source
import scala.xml.parsing.ConstructingParser
import scala.xml.{Text, NamespaceBinding, MetaData}
import org.apache.log4j.Logger
import scala.collection.mutable
import java.nio.file.Paths
import scala.annotation.tailrec
import java.io.File

class XmlScoverageReportParser(source: Source) extends ConstructingParser(source, false)
  with ScoverageReportParser {
  private val log = Logger.getLogger(classOf[XmlScoverageReportParser])

  private val CLASS_ELEMENT = "class"
  private val FILENAME_ATTRIBUTE = "filename"
  private val STATEMENT_ELEMENT = "statement"
  private val START_ATTRIBUTE = "start"
  private val LINE_ATTRIBUTE = "line"
  private val INVOCATION_COUNT_ATTRIBUTE = "invocation-count"

  val statementsInFile: mutable.HashMap[String, List[CoveredStatement]] = mutable.HashMap.empty
  var currentFilePath: Option[String] = None

  def parse(): ProjectStatementCoverage = {
    // Initialze
    nextch()

    // Parse
    document()

    // Transform map to project
    projectFromMap(statementsInFile.toMap)
  }

  override def elemStart(pos: Int, pre: String, label: String, attrs: MetaData, scope: NamespaceBinding) {
    label match {
      case CLASS_ELEMENT => {
        currentFilePath = Some(fixLeadingSlash(getText(attrs, FILENAME_ATTRIBUTE)))
        log.debug("Current file path: " + currentFilePath.get)
      }
      case STATEMENT_ELEMENT => {
        currentFilePath match {
          case Some(cfp) => {
            val start = getInt(attrs, START_ATTRIBUTE)
            val line = getInt(attrs, LINE_ATTRIBUTE)
            val hits = getInt(attrs, INVOCATION_COUNT_ATTRIBUTE)

            // Add covered statement to the mutable map
            val pos = StatementPosition(line, start)
            addCoveredStatement(cfp, CoveredStatement(pos, pos, hits))

            log.debug("Statement added: " + line + ", " + hits + ", " + start)
          }
          case None => throw new ScoverageException("Current file path not set!")
        }
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
   * Remove this when scoverage is fixed!
   */
  private def fixLeadingSlash(filePath: String) = {
    if (filePath.startsWith(File.separator))
      filePath.drop(File.separator.length)
    else
      filePath
  }

  private def getInt(attrs: MetaData, name: String) = getText(attrs, name).toInt

  private def getText(attrs: MetaData, name: String): String = {
    attrs.get(name) match {
      case Some(attr) => {
        attr match {
          case text: Text => text.toString
          case _ => throw new ScoverageException("Not a text attribute!")
        }
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
        case Some(child) => {
          chain.children match {
            case h :: t => {
              if (t != Nil)
                throw new IllegalStateException("This is not a linear chain!")

              child.add(h)
            }
            case _ => // Duplicate file? Should not happen.
          }
        }
      }
    }

    def toStatementCoverage: StatementCoverage = {
      val childNodes = children.map(_.toStatementCoverage)

      childNodes match {
        case Nil => coverage.get
        case _ => DirectoryStatementCoverage(name, childNodes)
      }
    }
  }

  private def projectFromMap(statementsInFile: Map[String, List[CoveredStatement]]):
    ProjectStatementCoverage = {

    // Transform to file statement coverage
    val files = fileStatementCoverage(statementsInFile)

    // Transform file paths to chain of case classes
    val chained = files.map(fsc => pathToChain(fsc._1, fsc._2))

    // Merge chains into one tree
    val root = DirOrFile("", Nil, None)
    chained.foreach(root.add(_))

    // Transform file system tree into coverage structure tree
    ProjectStatementCoverage("", List(root.toStatementCoverage))
  }

  private def pathToChain(filePath: String, coverage: FileStatementCoverage): DirOrFile = {
    val path = Paths.get(filePath)

    val names = for (i <- 0 to path.getNameCount - 1)
      yield path.getFileName.toString

    names.foldLeft(DirOrFile("", Nil, Some(coverage))) { (dirOrFile, name) =>
      val child = DirOrFile(name, Nil, dirOrFile.coverage)
      dirOrFile.children = List(child)
      child
    }
  }

  private def fileStatementCoverage(statementsInFile: Map[String, List[CoveredStatement]]):
    Map[String, FileStatementCoverage] = {
    statementsInFile.map { sif =>
      val fileStatementCoverage = FileStatementCoverage(Paths.get(sif._1).getFileName.toString,
        sif._2.length, coveredStatements(sif._2), sif._2)

      (sif._1, fileStatementCoverage)
    }
  }

  private def coveredStatements(statements: Iterable[CoveredStatement]) =
    statements.count(_.hitCount > 0)
}

object XmlScoverageReportParser {
  def apply(scoverageReportPath: String): XmlScoverageReportParser = {
    require(scoverageReportPath != null)
    require(!scoverageReportPath.trim.isEmpty)

    new XmlScoverageReportParser(sourceFromFile(scoverageReportPath))
  }

  private def sourceFromFile(scoverageReportPath: String) = {
    try {
      Source.fromFile(scoverageReportPath)
    }
    catch {
      case ex: Exception => throw ScoverageException("Cannot parse file! [" + scoverageReportPath + "]", ex)
    }
  }
}