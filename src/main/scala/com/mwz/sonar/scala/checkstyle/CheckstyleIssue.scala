package com.mwz.sonar.scala.checkstyle

final case class CheckstyleIssue(
  line: Int,
  column: Option[Int] = None,
  severity: String,
  text: String,
  snippet: String,
  inspectionClass: String
) {

  def message: String = if (snippet.isEmpty) text else s"$text\n$snippet"

}
