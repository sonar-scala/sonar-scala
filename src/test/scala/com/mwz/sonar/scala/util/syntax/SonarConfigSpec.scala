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

package com.mwz.sonar.scala
package util
package syntax

import java.nio.file.Paths

import com.mwz.sonar.scala.util.syntax.SonarConfig._
import org.scalatest.OptionValues
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.sonar.api.config.internal.MapSettings

class SonarConfigSpec extends AnyFlatSpec with Matchers with OptionValues {
  "config" should "get paths" in {
    val conf = new MapSettings()
      .setProperty("path", "this/is/a/path, another/path")
      .asConfig()
    val defaultPaths = List(Paths.get("default/path"), Paths.get("default/path2"))

    conf.getPaths("path", defaultPaths) shouldBe List(
      Paths.get("this/is/a/path"),
      Paths.get("another/path")
    )
    conf.getPaths("not.a.path", defaultPaths) shouldBe defaultPaths
  }

  it should "get a boolean" in {
    val conf = new MapSettings()
      .setProperty("bool.true", "true")
      .setProperty("bool.true2", "TRUE")
      .setProperty("bool.false", "false")
      .asConfig()

    conf.getAs[Boolean]("bool.true") shouldBe true
    conf.getAs[Boolean]("bool.true2") shouldBe true
    conf.getAs[Boolean]("bool.false") shouldBe false
    conf.getAs[Boolean]("not.a.bool") shouldBe false
  }

  it should "get a string" in {
    val conf = new MapSettings()
      .setProperty("text", "hello")
      .setProperty("number", "55")
      .setProperty("bool", "true")
      .asConfig()

    conf.getAs[String]("text").value shouldBe "hello"
    conf.getAs[String]("number").value shouldBe "55"
    conf.getAs[String]("bool").value shouldBe "true"
    conf.getAs[String]("empty") shouldBe empty
  }
}
