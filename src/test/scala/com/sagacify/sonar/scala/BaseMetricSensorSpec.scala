package com.sagacify.sonar.scala;

import java.nio.file.Paths

import org.mockito.Mockito.{mock, times, verify}
import org.scalatest._
import org.sonar.api.batch.SensorContext
import org.sonar.api.batch.fs.internal.{DefaultFileSystem, TestInputFileBuilder}
import org.sonar.api.config.internal.MapSettings
import org.sonar.api.measures.{CoreMetrics => CM}
import org.sonar.api.resources.Project

import scala.collection.JavaConversions._;

class ScalaSensorSpec extends FlatSpec with Matchers {

  val NUMBER_OF_FILES = 3;

  val scala = new Scala(new MapSettings().asConfig())

  def context = new {
    val fs = new DefaultFileSystem(Paths.get("./src/test/resources"))
    val project = mock(classOf[Project])
    val sensor = new ScalaSensor(scala, fs)
  }

  // val project = mock(classOf[Project])
  // val sensorContext = mock(classOf[SensorContext])
  // val sensor = new BaseMetricsSensor(new Scala(new Settings()), fs)

  "A ScalaSensor" should "execute on a scala project" in {
    val c = context
    // use TestInputFileBuilder here?
    //c.fs.add(new DefaultInputFile("p", "fake.scala").setLanguage("scala"));
    c.fs.add(TestInputFileBuilder.create("p", "fake.scala").setLanguage("scala").build())
    assert(c.sensor.shouldExecuteOnProject(c.project))
  }

  it should "only execute on a scala project" in {
    val c = context
    c.fs.add(TestInputFileBuilder.create("p", "fake.php").setLanguage("php").build())
    assert(! c.sensor.shouldExecuteOnProject(c.project))
  }

  it should "correctly measure ScalaFile1" in {
    val c = context
    c.fs.add(TestInputFileBuilder.create("", "src/test/resources/ScalaFile1.scala").setLanguage("scala").build())
    val sensorContext = mock(classOf[SensorContext])
    c.sensor.analyse(c.project, sensorContext)

    val inputFiles = c.fs.inputFiles(c.fs.predicates().hasLanguage(scala.getKey()))

    inputFiles.foreach{ file =>
      verify(sensorContext, times(1))
            .saveMeasure(file, CM.FILES, double2Double(1))
      verify(sensorContext, times(1))
          .saveMeasure(file, CM.COMMENT_LINES, double2Double(0))
      verify(sensorContext, times(1))
          .saveMeasure(file, CM.CLASSES, double2Double(1))
      verify(sensorContext, times(1))
        .saveMeasure(file, CM.FUNCTIONS, double2Double(1))

    }
  }

  it should "correctly measure ScalaFile2" in {

    val c = context
    c.fs.add(TestInputFileBuilder.create("", "src/test/resources/ScalaFile2.scala").setLanguage("scala").build())
    val sensorContext = mock(classOf[SensorContext])
    c.sensor.analyse(c.project, sensorContext)

    val inputFiles = c.fs.inputFiles(c.fs.predicates().hasLanguage(scala.getKey()))

    inputFiles.foreach{ file =>
      verify(sensorContext, times(1))
          .saveMeasure(file, CM.FILES, double2Double(1))
      verify(sensorContext, times(1))
          .saveMeasure(file, CM.COMMENT_LINES, double2Double(1))
      verify(sensorContext, times(1))
        .saveMeasure(file, CM.CLASSES, double2Double(2))
      verify(sensorContext, times(1))
        .saveMeasure(file, CM.FUNCTIONS, double2Double(2))
    }
  }
}
