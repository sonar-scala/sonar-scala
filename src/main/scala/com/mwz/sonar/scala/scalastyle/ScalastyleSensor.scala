package com.mwz.sonar.scala.scalastyle

import java.nio.file.Path
import java.nio.file.Paths

import com.mwz.sonar.scala.Scala
import com.mwz.sonar.scala.checkstyle.CheckstyleIssue
import com.mwz.sonar.scala.checkstyle.CheckstyleReportParserAPI
import com.mwz.sonar.scala.checkstyle.CheckstyleReportParserAPI.FILEPATH
import com.mwz.sonar.scala.scalastyle.ScalastyleSensor._
import com.mwz.sonar.scala.scapegoat.ScapegoatSensor
import com.mwz.sonar.scala.util.JavaOptionals._
import com.mwz.sonar.scala.util.Log
import com.mwz.sonar.scala.util.PathUtils.cwd
import com.mwz.sonar.scala.util.PathUtils.getModuleBaseDirectory
import org.sonar.api.batch.fs.FilePredicate
import org.sonar.api.batch.fs.FileSystem
import org.sonar.api.batch.fs.InputFile
import org.sonar.api.batch.sensor.Sensor
import org.sonar.api.batch.sensor.SensorContext
import org.sonar.api.batch.sensor.SensorDescriptor
import org.sonar.api.config.Configuration

import scala.util.Failure
import scala.util.Success
import scala.util.Try

final class ScalastyleSensor(checkstyleReportParser: CheckstyleReportParserAPI) extends Sensor {

  private[this] val log = Log(classOf[ScapegoatSensor], "scalastyle")

  override def describe(descriptor: SensorDescriptor): Unit =
    descriptor
      .createIssuesForRuleRepository(ScalastyleRulesRepository.RepositoryKey)
      .name(SensorName)
      .onlyOnFileType(InputFile.Type.MAIN)
      .onlyOnLanguage(Scala.LanguageKey)

  /** Saves the Scapegoat information of a module */
  override def execute(context: SensorContext): Unit = {
    val modulePath = getModuleBaseDirectory(context.fileSystem)
    val reportPath = modulePath.resolve(getScalastyleReportPath(context.config))

    log.info("Initializing the Scalastyle sensor.")
    log.info(s"Loading the scalastyle report file: '$reportPath'")
    log.debug(s"The current working directory is: '$cwd'.")

    Try(checkstyleReportParser.parse(reportPath)) match {
      case Success(scalastyleIssuesByFilename) =>
        log.info("Successfully loaded the scapegoat report file.")
        processScalastyleErrors(context, scalastyleIssuesByFilename)
      case Failure(ex) =>
        log.error(
          "Aborting the scapegoat sensor execution, " +
          s"cause: an error occurred while reading the scapegoat report file: '$reportPath', " +
          s"the error was: ${ex.getMessage}."
        )
    }
  }

  /** Returns the path to the scapegoat report for this module */
  private[scalastyle] def getScalastyleReportPath(settings: Configuration): Path = {
    settings
      .get(ScalastyleReportPathPropertyKey)
      .toOption
      .map(path => Paths.get(path))
      .getOrElse {
        val defaultScapegoatReportPath = getDefaultScalastyleReportPath

        log.info(
          s"Missing the property: '$ScalastyleReportPathPropertyKey', " +
          s"using the default value: '$defaultScapegoatReportPath'."
        )

        defaultScapegoatReportPath
      }
  }

  /** Process all scalastyle warnings */
  private[this] def processScalastyleErrors(
    context: SensorContext,
    scapegoatIssuesByFilename: Map[FILEPATH, Seq[CheckstyleIssue]]
  ): Unit = {
    val activeRules = context.activeRules
    val filesystem = context.fileSystem

    scapegoatIssuesByFilename.foreach {
      case (filename, scalastyleIssues) =>
        log.info(s"Saving the scalastyles issues for file '$filename'.")

        getModuleFile(filename, filesystem) match {
          case Some(file) =>
            scalastyleIssues.foreach { scalastyleIssue =>
              log.debug(s"Try saving the scapegoat ${scalastyleIssue.source} issues for file '$filename'")

              // try to retrieve the SonarQube rule for this scapegoat issue
              Option(
                activeRules.findByInternalKey(
                  ScalastyleRulesRepository.RepositoryKey,
                  scalastyleIssue.source
                )
              ) match {
                case Some(rule) =>
                  // if the rule was found, create a new sonarqube issue for it
                  val sonarqubeIssue = context.newIssue().forRule(rule.ruleKey)

                  sonarqubeIssue.at(
                    sonarqubeIssue
                      .newLocation()
                      .on(file)
                      //.at(scalastyleIssue.column.fold(file.selectLine(scalastyleIssue.line))(column => file.newPointer(scalastyleIssue.line, column)) )
                      .at(file.selectLine(scalastyleIssue.line))
                      .message(scalastyleIssue.message)
                  )

                  sonarqubeIssue.save()
                case None =>
                  // if the rule was not found,
                  // check if it is because the rule is not activated in the current quality profile,
                  // or if it is because the inspection does not exist in the scapegoat rules repository
                  val inspectionExists =
                    ScalastyleInspections.AllInspections.exists(
                      inspection => inspection.id == scalastyleIssue.source
                    )
                  if (inspectionExists)
                    log.debug(
                      s"The rule: ${scalastyleIssue.source}, " +
                      "was not activated in the current quality profile."
                    )
                  else
                    log.warn(
                      s"The inspection: ${scalastyleIssue.source}, " +
                      "does not exist in the scapegoat rules repository."
                    )
              }
            }
          case None =>
            log.error(s"The file '$filename' couldn't be found.")
        }
    }
  }

  /** Returns the module input file with the given filename */
  private[scalastyle] def getModuleFile(filename: String, fs: FileSystem): Option[InputFile] = {
    val predicates = fs.predicates
    val predicate: FilePredicate = predicates.and(
      predicates.hasLanguage(Scala.LanguageKey),
      predicates.hasType(InputFile.Type.MAIN),
      predicates.hasAbsolutePath(filename)
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

private[scalastyle] object ScalastyleSensor {
  final val SensorName = "Scalastyle Sensor"
  final val ScalastyleReportPathPropertyKey = "sonar.scala.scalastyle.reportPath"

  def getDefaultScalastyleReportPath: Path =
    Paths.get(
      "target",
      "scalastyle-result.xml"
    )
}
