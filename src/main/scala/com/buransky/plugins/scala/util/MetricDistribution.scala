/*
 * Sonar Scala Plugin
 * Copyright (C) 2011 - 2013 All contributors
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
package org.sonar.plugins.scala.util

import collection.immutable.TreeMap

import org.sonar.api.measures.{ Metric, RangeDistributionBuilder }

class MetricDistribution(metric: Metric, ranges: Array[Number]) {

  var distribution = TreeMap[Double, Int]()

  def add(value: Double) {
    add(value, 1)
  }

  def add(value: Double, count: Int) {
    val oldValue = distribution.getOrElse(value, 0)
    distribution = distribution.updated(value, oldValue + count)
  }

  def add(metricDistribution: MetricDistribution) {
    metricDistribution.distribution.foreach(entry => add(entry._1, entry._2))
  }

  def getMeasure() = {
    val rangeDistribution = new RangeDistributionBuilder(metric, ranges)
    distribution.foreach(entry => rangeDistribution.add(entry._1, entry._2))
    rangeDistribution.build
  }
}