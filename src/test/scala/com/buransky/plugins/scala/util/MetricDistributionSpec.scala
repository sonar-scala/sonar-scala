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

import org.junit.runner.RunWith
import org.scalatest.FlatSpec
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.junit.JUnitRunner
import org.sonar.api.measures.CoreMetrics

@RunWith(classOf[JUnitRunner])
class MetricDistributionSpec extends FlatSpec with ShouldMatchers {

  val metric = CoreMetrics.CLASS_COMPLEXITY_DISTRIBUTION
  val ranges = Array[Number](1, 5, 10)

  "A metric distribution" should "increment occurence of value" in {
    val distribution = new MetricDistribution(metric, ranges)
    distribution.add(1.0)
    distribution.getMeasure.getData should be ("1=1;5=0;10=0")
  }

  it should "increment occurence of all values" in {
    val distribution = new MetricDistribution(metric, ranges)
    distribution.add(1.0)
    distribution.add(10.0)
    distribution.add(5.0)
    distribution.getMeasure.getData should be ("1=1;5=1;10=1")
  }

  it should "increase occurence of value by submitted number" in {
    val distribution = new MetricDistribution(metric, ranges)
    distribution.add(1.0, 3)
    distribution.getMeasure.getData should be ("1=3;5=0;10=0")
  }

  it should "increase occurence of all values by submitted number" in {
    val distribution = new MetricDistribution(metric, ranges)
    distribution.add(1.0, 3)
    distribution.add(10.0, 8)
    distribution.add(5.0, 2)
    distribution.getMeasure.getData should be ("1=3;5=2;10=8")
  }

  it should "increase occurence of value by submitted distribution" in {
    val distribution = new MetricDistribution(metric, ranges)
    distribution.add(1.0, 3)

    val otherDistribution = new MetricDistribution(metric, ranges)
    otherDistribution.add(distribution)

    otherDistribution.getMeasure.getData should be ("1=3;5=0;10=0")
  }

  it should "increase occurence of all values by submitted distribution" in {
    val distribution = new MetricDistribution(metric, ranges)
    distribution.add(1.0, 3)
    distribution.add(10.0, 8)
    distribution.add(5.0, 2)

    val otherDistribution = new MetricDistribution(metric, ranges)
    otherDistribution.add(distribution)

    otherDistribution.getMeasure.getData should be ("1=3;5=2;10=8")
  }

  it should "output an empty distribution properly" in {
    val distribution = new MetricDistribution(metric, ranges)
    distribution.getMeasure.getData should be ("1=0;5=0;10=0")
  }

  it should "copy an empty distribution properly" in {
    val distribution = new MetricDistribution(metric, ranges)

    val otherDistribution = new MetricDistribution(metric, ranges)
    otherDistribution.add(distribution)

    otherDistribution.getMeasure.getData should be ("1=0;5=0;10=0")
  }
}