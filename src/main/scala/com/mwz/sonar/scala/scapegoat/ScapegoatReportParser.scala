package com.mwz.sonar.scala.scapegoat

import java.nio.file.{Path, Paths}

/** Used to mock the scoverage report parser in tests */
private[scapegoat] trait ScapegoatReportParserAPI {

  /** Parses the scapegoat xml report and returns all scapegoat warnings by filename */
  def parse(scapegoatReportPath: Path): Map[Path, Seq[ScapegoatWarning]]
}

/** Scapegoat XML reports parser */
private[scapegoat] trait ScapegoatReportParser extends ScapegoatReportParserAPI {
  override final def parse(scapegoatReportPath: Path): Map[Path, Seq[ScapegoatWarning]] =
    ???
}
