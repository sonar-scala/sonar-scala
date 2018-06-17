package com.mwz.sonar.scala
package scapegoat

import org.sonar.api.batch.fs.InputFile
import org.sonar.api.batch.sensor.{Sensor, SensorContext, SensorDescriptor}

/** Main sensor for importing Scapegoat reports to SonarQube */
final class ScoverageSensor extends ScapegoatSensorInternal with ScapegoatReportParser

/** Implementation of the sensor */
private[scapegoat] abstract class ScapegoatSensorInternal extends Sensor {
  // cake pattern to mock the scapegoat report parser in tests
  scapegoatReportParser: ScapegoatReportParserAPI =>

  /** Populates the descriptor of this sensor */
  override def describe(descriptor: SensorDescriptor): Unit =
    descriptor
      .createIssuesForRuleRepository(ScapegoatRulesRepository.RepositoryKey)
      .name(ScapegoatSensorInternal.SensorName)
      .onlyOnFileType(InputFile.Type.MAIN)
      .onlyOnLanguage(Scala.LanguageKey)

  /** Saves the Scapegoat information of a module */
  override def execute(context: SensorContext): Unit = {
    ???
  }
}

private[scapegoat] object ScapegoatSensorInternal {
  private[scapegoat] val SensorName: String = "Scapegoat Sensor"
}
