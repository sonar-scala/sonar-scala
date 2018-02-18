package com.sagacify.sonar.scala

import org.sonar.api.batch.sensor.{Sensor, SensorContext, SensorDescriptor}
import org.sonar.api.measures.{CoreMetrics => CM}

import scala.collection.JavaConverters._
import scala.io.Source

class ScalaSensor(scala: Scala) extends Sensor {

  override def execute(context: SensorContext): Unit = {
    val charset = context.fileSystem().encoding.toString
    val versionProperty = context.config().get("sonar.scala.source")
    val version =
      if (context.config().get("sonar.scala.source").isPresent) versionProperty.get() else "2.11.11"

    val inputFiles =
      context.fileSystem().inputFiles(context.fileSystem().predicates().hasLanguage(scala.getKey))

    inputFiles.asScala.foreach { inputFile =>
      context.newMeasure().on(inputFile).forMetric(CM.FILES).withValue(1).save()

      val sourceCode = Source.fromFile(inputFile.uri, charset).mkString
      val tokens = Scala.tokenize(sourceCode, version)

      context
        .newMeasure()
        .on(inputFile)
        .forMetric(CM.COMMENT_LINES)
        .withValue(int2Integer(Measures.count_comment_lines(tokens)))
        .save()
      context
        .newMeasure()
        .on(inputFile)
        .forMetric(CM.NCLOC)
        .withValue(int2Integer(Measures.count_ncloc(tokens)))
        .save()
      context
        .newMeasure()
        .on(inputFile)
        .forMetric(CM.CLASSES)
        .withValue(int2Integer(Measures.count_classes(tokens)))
        .save()
      context
        .newMeasure()
        .on(inputFile)
        .forMetric(CM.FUNCTIONS)
        .withValue(int2Integer(Measures.count_methods(tokens)))
        .save()

    // context.saveMeasure(inputFile, CM.ACCESSORS, accessors)
    // context.saveMeasure(inputFile, CM.COMPLEXITY_IN_FUNCTIONS, complexityInMethods)
    // context.saveMeasure(inputFile, CM.COMPLEXITY_IN_CLASSES, fileComplexity)
    // context.saveMeasure(inputFile, CM.COMPLEXITY, fileComplexity)
    // context.saveMeasure(inputFile, CM.PUBLIC_API, publicApiChecker.getPublicApi())
    // context.saveMeasure(inputFile, CM.PUBLIC_DOCUMENTED_API_DENSITY, publicApiChecker.getDocumentedPublicApiDensity())
    // context.saveMeasure(inputFile, CM.PUBLIC_UNDOCUMENTED_API, publicApiChecker.getUndocumentedPublicApi())

    }
  }

  override def describe(descriptor: SensorDescriptor): Unit = {
    descriptor.name("Scala Sensor")
    descriptor.onlyOnLanguage(scala.getKey)
  }

}
