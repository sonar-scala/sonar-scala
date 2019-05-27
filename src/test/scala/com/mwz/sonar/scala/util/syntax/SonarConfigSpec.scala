/*
 * Copyright (C) 2018-2019  All sonar-scala contributors
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
import org.scalatest.{FlatSpec, Matchers}
import org.sonar.api.config.internal.MapSettings

class SonarConfigSpec extends FlatSpec with Matchers {
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
}
