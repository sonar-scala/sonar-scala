package com.mwz.sonar.scala.sensor

import java.nio.file.Path
import java.nio.file.Paths

import com.mwz.sonar.scala.Scala
import com.mwz.sonar.scala.checkstyle.CheckstyleReportParserAPI.FILEPATH
import com.mwz.sonar.scala.util.JavaOptionals._
import com.mwz.sonar.scala.util.Log
import com.mwz.sonar.scala.util.PathUtils.cwd
import com.mwz.sonar.scala.util.PathUtils.getModuleBaseDirectory
import org.sonar.api.batch.fs.FilePredicate
import org.sonar.api.batch.fs.FileSystem
import org.sonar.api.batch.fs.InputFile
import org.sonar.api.batch.rule.ActiveRule
import org.sonar.api.batch.rule.ActiveRules
import org.sonar.api.batch.sensor.Sensor
import org.sonar.api.batch.sensor.SensorContext
import org.sonar.api.config.Configuration

import scala.util.Failure
import scala.util.Success
import scala.util.Try

abstract class IssueReportSensor extends Sensor {

  val name: String

  val reportPathPropertyKey: String

  val repositoryKey: String

  protected val log = Log(this.getClass, name)

  def parseReport(reportPath: Path): Map[FILEPATH, Seq[ReportIssue]]

  def defaultReportPath(settings: Configuration): Path

  def sonarRuleNotFound(issue: ReportIssue)

  def findSonarRule(activeRules: ActiveRules, issue: ReportIssue): Option[ActiveRule]

  /** Saves the Scapegoat information of a module */
  override def execute(context: SensorContext): Unit = {
    val modulePath = getModuleBaseDirectory(context.fileSystem)
    val reportPath = modulePath.resolve(getReportPath(context.config))

    log.info(s"Initializing the $name sensor.")
    log.info(s"Loading the $name report file: '$reportPath'.")
    log.debug(s"The current working directory is: '$cwd'.")

    Try(parseReport(reportPath)) match {
      case Success(issuesByFilename) =>
        log.info(s"Successfully loaded the $name report file.")
        processIssues(context, issuesByFilename)
      case Failure(ex) =>
        log.error(
          s"Aborting the $name sensor execution, " +
          s"cause: an error occurred while reading the $name report file: '$reportPath', " +
          s"the error was: ${ex.getMessage}."
        )
    }
  }

  def processIssues(
    context: SensorContext,
    issuesByFilename: Map[FILEPATH, Seq[ReportIssue]]
  ): Unit = {
    val activeRules: ActiveRules = context.activeRules
    val filesystem = context.fileSystem

    issuesByFilename.foreach {
      case (filename, issues) =>
        log.info(s"Saving the $name issues for file '$filename'.")

        getModuleFile(filename, filesystem) match {
          case Some(file) =>
            issues.foreach { issue =>
              log.debug(s"Try saving the $name ${issue.snippet} issues for file '$filename'")

              // try to retrieve the SonarQube rule for this scapegoat issue
              findSonarRule(activeRules, issue) match {
                case Some(rule) =>
                  // if the rule was found, create a new sonarqube issue for it
                  val sonarqubeIssue = context.newIssue().forRule(rule.ruleKey)

                  sonarqubeIssue.at(
                    sonarqubeIssue
                      .newLocation()
                      .on(file)
                      //.at(issue.column.fold(file.selectLine(issue.line))(column => file.newPointer(issue.line, column)))
                      .at(file.selectLine(issue.line))
                      .message(issue.message)
                  )

                  sonarqubeIssue.save()

                case None =>
                  sonarRuleNotFound(issue)

              }
            }
          case None =>
            log.error(s"The file '$filename' couldn't be found.")
        }
    }
  }

  def getReportPath(settings: Configuration): Path = {
    settings
      .get(reportPathPropertyKey)
      .toOption
      .map(Paths.get(_))
      .getOrElse {
        val reportPath = defaultReportPath(settings)

        log.info(
          s"Missing the property: '$reportPathPropertyKey', " +
          s"using the default value: '$reportPath'."
        )

        reportPath
      }
  }

  /** Returns the module input file with the given filename */
  def getModuleFile(filename: String, fs: FileSystem): Option[InputFile] = {
    val predicates = fs.predicates
    val predicate: FilePredicate = predicates.and(
      predicates.hasLanguage(Scala.LanguageKey),
      predicates.hasType(InputFile.Type.MAIN),
      predicates.or(predicates.hasAbsolutePath(filename), predicates.matchesPathPattern(s"**/$filename"))
    )

    // catch both exceptions and null values
    Try(fs.inputFile(predicate))
      .fold(
        ex => {
          log.error(s"Exception occurred on file `$filename` extracting, ex: ${ex.getStackTrace}")
          None
        },
        file => Option(file)
      )
  }

}
