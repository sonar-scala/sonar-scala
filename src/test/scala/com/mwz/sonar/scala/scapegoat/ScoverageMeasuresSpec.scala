/*
 * Copyright (C) 2018-2020  All sonar-scala contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU General Lesser Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.mwz.sonar.scala.scapegoat

import java.lang.Integer

import com.mwz.sonar.scala.scoverage.ScoverageMeasures
import org.scalacheck.ScalacheckShapeless._
import org.scalacheck._
import org.scalatest.Inspectors
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks
import org.sonar.api.ce.measure.Component.Type
import org.sonar.api.ce.measure.test.TestComponent
import org.sonar.api.ce.measure.test.TestMeasureComputerContext
import org.sonar.api.ce.measure.test.TestMeasureComputerDefinition._
import org.sonar.api.ce.measure.test.TestSettings

@SuppressWarnings(Array("DisableSyntax.null"))
class ScoverageMeasuresSpec extends AnyFlatSpec with Matchers with ScalaCheckDrivenPropertyChecks {
  val typeGen: Gen[Type] = Gen.oneOf(Seq(Type.DIRECTORY, Type.MODULE, Type.PROJECT))
  def measuresGen(size: Int): Gen[List[Integer]] = Gen.posNum[Int].map(num => List.fill(size)(num))

  val sumMetrics = List(
    "sonar-scala-scoverage-total-statements",
    "sonar-scala-scoverage-covered-statements",
    "sonar-scala-scoverage-total-branches",
    "sonar-scala-scoverage-covered-branches"
  )

  val percentageMetrics = List(
    "sonar-scala-scoverage-statement-coverage",
    "sonar-scala-scoverage-branch-coverage"
  )

  it should "compute measures on directories, modules and the project" in {
    forAll(typeGen, Gen.chooseNum(1, 15)) { (componentType, children) =>
      forAll(measuresGen(children), measuresGen(children), measuresGen(children), measuresGen(children)) {
        (statements, coveredStatements, branches, coveredBranches) =>
          val component = new TestComponent("test", componentType, null)
          val settings = new TestSettings()
          val definition = new MeasureComputerDefinitionBuilderImpl()
            .setInputMetrics(sumMetrics: _*)
            .setOutputMetrics((sumMetrics ++ percentageMetrics): _*)
            .build()
          val ctx = new TestMeasureComputerContext(component, settings, definition)
          addMeasures(ctx, sumMetrics.zip(List(statements, coveredStatements, branches, coveredBranches)))
          new ScoverageMeasures().compute(ctx)

          val expectedSumMeasures: List[(String, Integer)] =
            sumMetrics.zip(
              List(sum(statements), sum(coveredStatements), sum(branches), sum(coveredBranches))
            )

          val expectedPercentageMeasures: List[(String, Double)] =
            percentageMetrics zip expectedSumMeasures.grouped(2).map {
              case List((_, total), (_, actual)) =>
                if (total > 0)
                  (BigDecimal.valueOf(actual.doubleValue) / total.intValue * 100)
                    .setScale(scale = 2, mode = BigDecimal.RoundingMode.HALF_EVEN)
                    .toDouble
                else 0
            }

          Inspectors.forAll(expectedSumMeasures) {
            case (key, value) =>
              ctx.getMeasure(key).getIntValue shouldBe value
          }

          Inspectors.forAll(expectedPercentageMeasures) {
            case (key, value) =>
              ctx.getMeasure(key).getDoubleValue shouldBe value
          }
      }
    }
  }

  it should "not compute measures on files" in {
    val component =
      new TestComponent(
        "test",
        Type.FILE,
        new TestComponent.FileAttributesImpl("scala", true)
      )
    val settings = new TestSettings()
    val definition = new MeasureComputerDefinitionBuilderImpl()
      .setInputMetrics("sonar-scala-scoverage-total-statements")
      .setOutputMetrics("sonar-scala-scoverage-total-statements")
      .build()
    val ctx = new TestMeasureComputerContext(component, settings, definition)
    ctx.addMeasure("sonar-scala-scoverage-total-statements", 5: Integer)
    new ScoverageMeasures().compute(ctx)

    ctx.getMeasure("sonar-scala-scoverage-total-statements").getIntValue shouldBe 5
  }

  def addMeasures(ctx: TestMeasureComputerContext, values: List[(String, List[Integer])]): Unit =
    values.foreach {
      case (key, summed) =>
        ctx.addChildrenMeasures(key, summed: _*)
    }

  def sum(values: List[Integer]): Integer =
    values.fold[Integer](0)(_ + _)
}
