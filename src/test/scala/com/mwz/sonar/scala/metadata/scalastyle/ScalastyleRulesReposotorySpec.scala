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

package com.mwz.sonar.scala.metadata
package scalastyle

import cats.data.Chain
import cats.data.NonEmptyChain
import enumeratum.scalacheck._
import org.scalacheck.Prop._
import org.scalacheck.ScalacheckShapeless._
import org.scalacheck._
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.scalacheck.Checkers
import org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks

class ScalastyleRulesRepositorySpec
    extends AnyFlatSpec
    with Matchers
    with ScalaCheckDrivenPropertyChecks
    with Checkers {

  implicit def chainArbOf[T](implicit a: Arbitrary[T]): Arbitrary[Chain[T]] =
    Arbitrary(Gen.listOf[T](a.arbitrary).map(Chain.apply))

  def nonEmptyChainOf[T](implicit a: Arbitrary[T]): Gen[Chain[T]] =
    Gen.nonEmptyListOf[T](a.arbitrary).map(Chain.apply)

  it should "not create an additional rule from a non-template rule" in {
    forAll { rule: Rule =>
      val extraParam = ScalastyleRulesRepository.extraParam(rule.key)
      val expected = rule.copy(
        template = false,
        params = Chain(extraParam)
      )
      ScalastyleRulesRepository.fromTemplate(rule.copy(params = Chain.empty)) shouldBe NonEmptyChain.one(
        expected
      )
    }
  }

  it should "not create an additional rule if the instance is blacklisted" in {
    check(
      forAllNoShrink(
        Arbitrary.arbitrary[Rule],
        nonEmptyChainOf[Param],
        Gen.oneOf(ScalastyleRulesRepository.SkipTemplateInstances.toList)
      ) { (rule, params, className) =>
        val newRule = rule.copy(key = className, params = params)
        val extraParam = ScalastyleRulesRepository.extraParam(newRule.key)
        val expected = newRule.copy(
          key = className + "-template",
          template = true,
          params = params :+ extraParam
        )

        ScalastyleRulesRepository.fromTemplate(newRule) === NonEmptyChain.one(expected)
      }
    )
  }

  it should "create an additional rule for templates" in {
    check(
      forAllNoShrink(
        Arbitrary.arbitrary[Rule],
        nonEmptyChainOf[Param]
      ) { (rule, params) =>
        val newRule = rule.copy(params = params)
        val extraParam = ScalastyleRulesRepository.extraParam(newRule.key)
        val instance = newRule.copy(
          template = false,
          params = params :+ extraParam
        )
        val template = instance.copy(
          key = instance.key + "-template",
          template = true
        )

        ScalastyleRulesRepository.fromTemplate(newRule) === NonEmptyChain(template, instance)
      }
    )
  }

  it should "create an additional param with scalastyle class name" in {
    val expected = Param(
      name = "ruleClass",
      typ = ParamType.String,
      description = "Scalastyle's rule (checker) class name.",
      default = "scalastyle.class.name.test"
    )

    ScalastyleRulesRepository.extraParam("scalastyle.class.name.test") shouldBe expected
  }
}
