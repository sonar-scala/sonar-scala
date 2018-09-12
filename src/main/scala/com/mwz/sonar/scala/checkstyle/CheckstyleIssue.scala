package com.mwz.sonar.scala.checkstyle

import com.mwz.sonar.scala.sensor.ReportIssue

final case class CheckstyleIssue(
  line: Int,
  column: Option[Int],
  snippet: String,
  severity: String,
  message: String
) extends ReportIssue {
  override def internalKey: String = snippet
}
