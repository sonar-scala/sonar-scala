package com.sagacify.sonar.scala;

import org.scalatest._
import org.mockito.Matchers.any;
import org.mockito.Matchers.eq;
import org.mockito.Mockito.mock;
import org.mockito.Mockito.times;
import org.mockito.Mockito.verify;
import org.mockito.Mockito.verifyNoMoreInteractions;

import java.io.IOException;
import java.nio.file.Paths
import org.sonar.plugins.scala.Scala

import scala.collection.JavaConversions._

import org.junit.Before;
import org.junit.Test;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.batch.fs.internal.DefaultFileSystem;
import org.sonar.api.config.Settings;
import org.sonar.api.measures.{CoreMetrics => CM}
import org.sonar.api.measures.Measure;
import org.sonar.api.measures.Metric;
import org.sonar.api.resources.Project;

import org.sonar.api.batch.fs.internal.DefaultInputFile;

class ScalaSensorSpec extends FlatSpec with Matchers {

  val NUMBER_OF_FILES = 3;

  val scala = new Scala(new Settings())

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
    c.fs.add(new DefaultInputFile("p", "fake.scala").setLanguage("scala"));
    assert(c.sensor.shouldExecuteOnProject(c.project))
  }

  it should "only execute on a scala project" in {
    val c = context
    c.fs.add(new DefaultInputFile("p", "fake.php").setLanguage("php"));
    assert(! c.sensor.shouldExecuteOnProject(c.project))
  }

  it should "correctly measure ScalaFile1" in {
    val c = context
    c.fs.add(
      new DefaultInputFile("p", "ScalaFile1.scala").setLanguage("scala"));
    val sensorContext = mock(classOf[SensorContext])
    c.sensor.analyse(c.project, sensorContext)

    val inputFiles = c.fs.inputFiles(
        c.fs.predicates().hasLanguage(scala.getKey()))

    inputFiles.foreach{ file =>
      verify(sensorContext, times(1))
          .saveMeasure(file, CM.FILES, 1)
      verify(sensorContext, times(1))
          .saveMeasure(file, CM.COMMENT_LINES, 0)

    }
  }

  it should "correctly measure ScalaFile2" in {

    val c = context
    c.fs.add(
      new DefaultInputFile("p", "ScalaFile2.scala").setLanguage("scala"));
    val sensorContext = mock(classOf[SensorContext])
    c.sensor.analyse(c.project, sensorContext)

    val inputFiles = c.fs.inputFiles(
        c.fs.predicates().hasLanguage(scala.getKey()))

    inputFiles.foreach{ file =>
      verify(sensorContext, times(1))
          .saveMeasure(file, CM.FILES, 1)
      verify(sensorContext, times(1))
          .saveMeasure(file, CM.COMMENT_LINES, 1)

    }
  }
}
