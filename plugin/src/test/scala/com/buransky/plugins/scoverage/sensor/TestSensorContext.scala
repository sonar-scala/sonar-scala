/*
 * Sonar Scoverage Plugin
 * Copyright (C) 2013 Rado Buransky
 * dev@sonar.codehaus.org
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package com.buransky.plugins.scoverage.sensor

import java.lang.Double
import java.util.Date
import java.{io, util}

import org.sonar.api.batch.fs.{FileSystem, InputFile, InputPath}
import org.sonar.api.batch.rule.ActiveRules
import org.sonar.api.batch.sensor.dependency.NewDependency
import org.sonar.api.batch.sensor.duplication.NewDuplication
import org.sonar.api.batch.sensor.highlighting.NewHighlighting
import org.sonar.api.batch.sensor.issue.NewIssue
import org.sonar.api.batch.sensor.measure.NewMeasure
import org.sonar.api.batch.{AnalysisMode, Event, SensorContext}
import org.sonar.api.config.Settings
import org.sonar.api.design.Dependency
import org.sonar.api.measures.{Measure, MeasuresFilter, Metric}
import org.sonar.api.resources.{ProjectLink, Resource}
import org.sonar.api.rules.Violation

import scala.collection.mutable

class TestSensorContext extends SensorContext {

  private val measures = mutable.Map[String, Measure[_ <: io.Serializable]]()

  override def saveDependency(dependency: Dependency): Dependency = ???

  override def isExcluded(reference: Resource): Boolean = ???

  override def deleteLink(key: String): Unit = ???

  override def isIndexed(reference: Resource, acceptExcluded: Boolean): Boolean = ???

  override def saveViolations(violations: util.Collection[Violation]): Unit = ???

  override def getParent(reference: Resource): Resource = ???

  override def getOutgoingDependencies(from: Resource): util.Collection[Dependency] = ???

  override def saveSource(reference: Resource, source: String): Unit = ???

  override def getMeasures[M](filter: MeasuresFilter[M]): M = ???

  override def getMeasures[M](resource: Resource, filter: MeasuresFilter[M]): M = ???

  override def deleteEvent(event: Event): Unit = ???

  override def saveViolation(violation: Violation, force: Boolean): Unit = ???

  override def saveViolation(violation: Violation): Unit = ???

  override def saveResource(resource: Resource): String = ???

  override def getEvents(resource: Resource): util.List[Event] = ???

  override def getDependencies: util.Set[Dependency] = ???

  override def getIncomingDependencies(to: Resource): util.Collection[Dependency] = ???

  override def index(resource: Resource): Boolean = ???

  override def index(resource: Resource, parentReference: Resource): Boolean = ???

  override def saveLink(link: ProjectLink): Unit = ???

  override def getMeasure[G <: io.Serializable](metric: Metric[G]): Measure[G] = measures.get(metric.getKey).orNull.asInstanceOf[Measure[G]]

  override def getMeasure[G <: io.Serializable](resource: Resource, metric: Metric[G]): Measure[G] = ???

  override def getChildren(reference: Resource): util.Collection[Resource] = ???

  override def createEvent(resource: Resource, name: String, description: String, category: String, date: Date): Event = ???

  override def getResource[R <: Resource](reference: R): R = ???

  override def getResource(inputPath: InputPath): Resource = ???

  override def saveMeasure(measure: Measure[_ <: io.Serializable]): Measure[_ <: io.Serializable] = ???

  override def saveMeasure(metric: Metric[_ <: io.Serializable], value: Double): Measure[_ <: io.Serializable] = ???

  override def saveMeasure(resource: Resource, metric: Metric[_ <: io.Serializable], value: Double): Measure[_ <: io.Serializable] = ???

  override def saveMeasure(resource: Resource, measure: Measure[_ <: io.Serializable]): Measure[_ <: io.Serializable] = {
    measures.put(resource.getKey, measure)
    measure
  }

  override def saveMeasure(inputFile: InputFile, metric: Metric[_ <: io.Serializable], value: Double): Measure[_ <: io.Serializable] = ???

  override def saveMeasure(inputFile: InputFile, measure: Measure[_ <: io.Serializable]): Measure[_ <: io.Serializable] = ???

  override def newDuplication(): NewDuplication = ???

  override def activeRules(): ActiveRules = ???

  override def newHighlighting(): NewHighlighting = ???

  override def analysisMode(): AnalysisMode = ???

  override def fileSystem(): FileSystem = ???

  override def newDependency(): NewDependency = ???

  override def settings(): Settings = ???

  override def newMeasure[G <: io.Serializable](): NewMeasure[G] = ???

  override def newIssue(): NewIssue = ???
}