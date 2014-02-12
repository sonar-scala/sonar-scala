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

import org.sonar.api.batch.SensorContext
import org.sonar.api.resources.Resource
import org.sonar.api.measures.{Measure, Metric}
import scala.collection.mutable

class TestSensorContext extends SensorContext {
  private val measures = mutable.Map[String, Measure]()

  def createEvent(x$1: org.sonar.api.resources.Resource,x$2: String,x$3: String,x$4: String,x$5: java.util.Date): org.sonar.api.batch.Event = ???
  def deleteEvent(x$1: org.sonar.api.batch.Event): Unit = ???
  def deleteLink(x$1: String): Unit = ???
  def getChildren(x$1: org.sonar.api.resources.Resource): java.util.Collection[org.sonar.api.resources.Resource] = ???
  def getDependencies(): java.util.Set[org.sonar.api.design.Dependency] = ???
  def getEvents(x$1: org.sonar.api.resources.Resource): java.util.List[org.sonar.api.batch.Event] = ???
  def getIncomingDependencies(x$1: org.sonar.api.resources.Resource): java.util.Collection[org.sonar.api.design.Dependency] = ???
  def getMeasure(x$1: org.sonar.api.resources.Resource,x$2: org.sonar.api.measures.Metric): org.sonar.api.measures.Measure = ???
  def getMeasure(x$1: org.sonar.api.measures.Metric): org.sonar.api.measures.Measure = ???
  def getMeasures[M](x$1: org.sonar.api.resources.Resource,x$2: org.sonar.api.measures.MeasuresFilter[M]): M = ???
  def getMeasures[M](x$1: org.sonar.api.measures.MeasuresFilter[M]): M = ???
  def getOutgoingDependencies(x$1: org.sonar.api.resources.Resource): java.util.Collection[org.sonar.api.design.Dependency] = ???
  def getParent(x$1: org.sonar.api.resources.Resource): org.sonar.api.resources.Resource = ???
  def getResource[R <: org.sonar.api.resources.Resource](x$1: R): R = ???
  def index(x$1: org.sonar.api.resources.Resource,x$2: org.sonar.api.resources.Resource): Boolean = ???
  def index(x$1: org.sonar.api.resources.Resource): Boolean = ???
  def isExcluded(x$1: org.sonar.api.resources.Resource): Boolean = ???
  def isIndexed(x$1: org.sonar.api.resources.Resource,x$2: Boolean): Boolean = ???
  def saveDependency(x$1: org.sonar.api.design.Dependency): org.sonar.api.design.Dependency = ???
  def saveLink(x$1: org.sonar.api.resources.ProjectLink): Unit = ???

  def saveMeasure(resource: Resource, measure: Measure): Measure = {
    measures.put(resource.getKey, measure)
    measure
  }

  def saveMeasure(x$1: Resource,x$2: Metric,x$3: java.lang.Double): Measure = ???
  def saveMeasure(x$1: org.sonar.api.measures.Metric,x$2: java.lang.Double): org.sonar.api.measures.Measure = ???
  def saveMeasure(x$1: org.sonar.api.measures.Measure): org.sonar.api.measures.Measure = ???
  def saveResource(x$1: org.sonar.api.resources.Resource): String = ???
  def saveSource(x$1: org.sonar.api.resources.Resource,x$2: String): Unit = ???
  def saveViolation(x$1: org.sonar.api.rules.Violation): Unit = ???
  def saveViolation(x$1: org.sonar.api.rules.Violation,x$2: Boolean): Unit = ???
  def saveViolations(x$1: java.util.Collection[org.sonar.api.rules.Violation]): Unit = ???
}
