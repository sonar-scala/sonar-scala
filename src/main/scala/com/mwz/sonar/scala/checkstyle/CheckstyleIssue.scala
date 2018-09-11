package com.mwz.sonar.scala.checkstyle

final case class CheckstyleIssue(
  line: Int,
  column: Option[Int],
  source: String,
  severity: String,
  message: String
)
