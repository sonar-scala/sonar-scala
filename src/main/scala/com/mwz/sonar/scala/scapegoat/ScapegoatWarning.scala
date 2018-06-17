package com.mwz.sonar.scala.scapegoat

import inspections.Level
import java.nio.file.Path

/** Represents a warning in the scapegoat report */
private[scapegoat] final case class ScapegoatWarning(
  line: Int,
  text: String,
  snippet: String,
  level: Level,
  file: Path,
  inspectionId: String
)
