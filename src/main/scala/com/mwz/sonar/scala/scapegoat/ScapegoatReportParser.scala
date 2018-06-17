package com.mwz.sonar.scala.scapegoat

import inspections.Level
import java.nio.file.Path
import scala.xml.XML

/** Used to mock the scoverage report parser in tests */
private[scapegoat] trait ScapegoatReportParserAPI {

  /** Parses the scapegoat xml report and returns all scapegoat warnings by filename */
  def parse(scapegoatReportPath: Path): Map[String, Seq[ScapegoatWarning]]
}

/** Scapegoat XML reports parser */
private[scapegoat] trait ScapegoatReportParser extends ScapegoatReportParserAPI {
  override final def parse(scapegoatReportPath: Path): Map[String, Seq[ScapegoatWarning]] = {
    val scapegoatXMLReport = XML.loadFile(scapegoatReportPath.toFile)

    val scapegoatWarnings = for {
      warning <- scapegoatXMLReport \\ "warning"
      line = (warning \@ "line").toInt
      text = warning \@ "text"
      snippet = warning \@ "snippet"
      level = Level.fromName(warning \@ "level")
      file = (warning \@ "file").replace(".", "/") // scalastyle:ignore LiteralArguments
      inspectionId = warning \@ "inspection"
    } yield ScapegoatWarning(line, text, snippet, level, file, inspectionId)

    scapegoatWarnings.groupBy(warning => warning.file)
  }
}
