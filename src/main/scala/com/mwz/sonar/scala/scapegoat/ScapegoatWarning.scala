package com.mwz.sonar.scala.scapegoat

import inspections.Level

/** Represents a warning in the scapegoat report */
private[scapegoat] final case class ScapegoatWarning(
  line: Int,
  text: String,
  snippet: String,
  level: Level,
  file: String,
  inspectionId: String
) {
  def message: String =
    if (snippet.isEmpty) text
    else s"$text\n$snippet"
}
