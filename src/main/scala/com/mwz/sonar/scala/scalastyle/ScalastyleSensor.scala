package com.mwz.sonar.scala.scalastyle

import java.nio.file.Path
import java.nio.file.Paths

import com.mwz.sonar.scala.Scala
import com.mwz.sonar.scala.checkstyle.CheckstyleReportParserAPI
import com.mwz.sonar.scala.scalastyle.ScalastyleSensor._
import com.mwz.sonar.scala.sensor.IssueReportSensor
import com.mwz.sonar.scala.sensor.ReportIssue
import org.sonar.api.batch.fs.InputFile
import org.sonar.api.batch.rule.ActiveRule
import org.sonar.api.batch.rule.ActiveRules
import org.sonar.api.batch.sensor.SensorDescriptor
import org.sonar.api.config.Configuration

final class ScalastyleSensor(checkstyleReportParser: CheckstyleReportParserAPI) extends IssueReportSensor {

  override val name: String = "scalastyle"
  override val reportPathPropertyKey: String = ScalastyleReportPathPropertyKey

  override val repositoryKey: String = ScalastyleRulesRepository.RepositoryKey

  override def parseReport(reportPath: Path): Map[String, Seq[ReportIssue]] =
    checkstyleReportParser.parse(reportPath)

  override def defaultReportPath(settings: Configuration): Path = Paths.get(
    "target",
    "scalastyle-result.xml"
  )

  override def sonarRuleNotFound(issue: ReportIssue): Unit = {
    val inspectionExists =
      ScalastyleInspections.AllInspections.exists(
        inspection => inspection.id == issue.snippet
      )
    if (inspectionExists)
      log.debug(
        s"The rule: ${issue.snippet}, " +
        "was not activated in the current quality profile."
      )
    else
      log.warn(
        s"The inspection: ${issue.snippet}, " +
        "does not exist in the scapegoat rules repository."
      )
  }

  def findSonarRule(activeRules: ActiveRules, issue: ReportIssue): Option[ActiveRule] = {
    Option(
      activeRules.findByInternalKey(
        repositoryKey,
        issue.internalKey
      )
    )
  }

  override def describe(descriptor: SensorDescriptor): Unit =
    descriptor
      .createIssuesForRuleRepository(ScalastyleRulesRepository.RepositoryKey)
      .name(SensorName)
      .onlyOnFileType(InputFile.Type.MAIN)
      .onlyOnLanguage(Scala.LanguageKey)
}

private[scalastyle] object ScalastyleSensor {
  final val SensorName = "Scalastyle Sensor"
  final val ScalastyleReportPathPropertyKey = "sonar.scala.scalastyle.reportPath"
}
