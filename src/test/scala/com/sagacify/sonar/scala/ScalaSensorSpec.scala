package com.sagacify.sonar.scala

import java.nio.file.Paths

import org.scalatest._
import org.sonar.api.batch.fs.internal.TestInputFileBuilder
import org.sonar.api.batch.sensor.internal.{DefaultSensorDescriptor, SensorContextTester}
import org.sonar.api.batch.sensor.measure.Measure
import org.sonar.api.config.internal.MapSettings
import org.sonar.api.measures.{CoreMetrics => CM}

class ScalaSensorSpec extends FlatSpec with Matchers with OptionValues {

  val scala = new Scala(new MapSettings().asConfig())

  val sensor = new ScalaSensor(scala)

  "A ScalaSensor" should "correctly set descriptor" in {
    val descriptor = new DefaultSensorDescriptor
    sensor.describe(descriptor)

    descriptor.name() shouldBe "Scala Sensor"
    descriptor.languages() should have size 1
    descriptor.languages().iterator().next() shouldBe "scala"
  }

  it should "correctly measure ScalaFile1" in {
    val context = SensorContextTester.create(Paths.get("./src/test/resources"))
    val inputFile = TestInputFileBuilder.create("", "src/test/resources/ScalaFile1.scala").setLanguage("scala").build()
    context.fileSystem().add(inputFile)
    sensor.execute(context)

    val componentKey = inputFile.moduleKey() + inputFile.key()

    checkMetric(context, componentKey, CM.FILES_KEY, 1)
    checkMetric(context, componentKey, CM.COMMENT_LINES_KEY, 0)
    checkMetric(context, componentKey, CM.CLASSES_KEY, 1)
    checkMetric(context, componentKey, CM.FUNCTIONS_KEY, 1)
    checkMetric(context, componentKey, CM.NCLOC_KEY, 6)
  }

  it should "correctly measure ScalaFile2" in {
    val context = SensorContextTester.create(Paths.get("./src/test/resources"))
    val inputFile = TestInputFileBuilder.create("", "src/test/resources/ScalaFile2.scala").setLanguage("scala").build()
    context.fileSystem().add(inputFile)
    sensor.execute(context)

    val componentKey = inputFile.moduleKey() + inputFile.key()

    checkMetric(context, componentKey, CM.FILES_KEY, 1)
    checkMetric(context, componentKey, CM.COMMENT_LINES_KEY, 1)
    checkMetric(context, componentKey, CM.CLASSES_KEY, 2)
    checkMetric(context, componentKey, CM.FUNCTIONS_KEY, 2)
  }

  private def checkMetric(sensorContext: SensorContextTester, componentKey: String, metricKey: String, value: Int): Unit = {
    val measure: Option[Measure[Integer]] = Option(sensorContext.measure(componentKey, metricKey))
    measure shouldBe defined
    measure.value.value() shouldBe int2Integer(value)
  }
}
