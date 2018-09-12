package com.mwz.sonar.scala.sensor

trait ReportIssue {

  def line: Int

  def inspectionClass: String

  def message: String

  def internalKey: String

}
