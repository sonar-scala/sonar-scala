package com.sagacify.sonar.scala

import org.sonar.plugins.scala.Scala
import com.buransky.plugins.scoverage.measure.ScalaMetrics
import com.buransky.plugins.scoverage.sensor.ScoverageSensor
import com.buransky.plugins.scoverage.widget.ScoverageWidget
import com.ncredinburgh.sonar.scalastyle.{ScalastyleQualityProfile, ScalastyleRepository, ScalastyleSensor}
import org.sonar.api.SonarPlugin

import scala.collection.JavaConversions._
import scala.collection.mutable.ListBuffer

/**
 * Plugin entry point.
 */
class ScalaPlugin extends SonarPlugin {

  override def getExtensions: java.util.List[Class[_]] =
    ListBuffer[Class[_]] (
      classOf[Scala],
      classOf[ScalaSensor],
      classOf[ScalastyleRepository],
      classOf[ScalastyleQualityProfile],
      classOf[ScalastyleSensor],
      classOf[ScalaMetrics],
      classOf[ScoverageSensor],
      classOf[ScoverageWidget]
    )

  override val toString = getClass.getSimpleName

}
