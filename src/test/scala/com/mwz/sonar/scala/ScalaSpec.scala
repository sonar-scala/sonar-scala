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

import java.nio.file.Paths

import org.scalatest.{FlatSpec, Matchers}
import org.sonar.api.config.internal.MapSettings
import scalariform.ScalaVersion

class ScalaSpec extends FlatSpec with Matchers {
  "getFileSuffixes" should "return Scala file suffixes" in {
    val conf = new MapSettings().asConfig()
    new Scala(conf).getFileSuffixes shouldBe Array(".scala")

    val conf2 =
      new MapSettings()
        .setProperty("sonar.scala.file.suffixes", ".scala")
        .asConfig()
    new Scala(conf2).getFileSuffixes shouldBe Array(".scala")
  }

  "getScalaVersion" should "return the available version, if properly set" in {
    val conf = new MapSettings()
      .setProperty("sonar.scala.version", "2.11.11")
      .asConfig()

    Scala.getScalaVersion(conf) shouldBe ScalaVersion(2, 11)
  }

  it should "be able to parse a milestone version" in {
    val conf = new MapSettings()
      .setProperty("sonar.scala.version", "2.13.0-M3")
      .asConfig()

    Scala.getScalaVersion(conf) shouldBe ScalaVersion(2, 13)
  }

  it should "be able to parse a version without patch" in {
    val conf = new MapSettings()
      .setProperty("sonar.scala.version", "2.12")
      .asConfig()

    Scala.getScalaVersion(conf) shouldBe ScalaVersion(2, 12)
  }

  it should "not return the default version if the property is set to '2.11.0'" in {
    val conf = new MapSettings()
      .setProperty("sonar.scala.version", "2.11.0")
      .asConfig()

    val parsedVersion = Scala.getScalaVersion(conf)
    parsedVersion should not be ScalaVersion(2, 12)
    parsedVersion shouldBe ScalaVersion(2, 11)
  }

  it should "return the default version, if the property is not set" in {
    val conf = new MapSettings().asConfig()
    Scala.getScalaVersion(conf) shouldBe ScalaVersion(2, 12)
  }

  it should "return the default version, if the version property has an empty patch" in {
    val conf = new MapSettings()
      .setProperty("sonar.scala.version", "2.12.")
      .asConfig()

    Scala.getScalaVersion(conf) shouldBe ScalaVersion(2, 12)
  }

  it should "return the default version, if the version property only contains the major version" in {
    val conf = new MapSettings()
      .setProperty("sonar.scala.version", "2")
      .asConfig()

    Scala.getScalaVersion(conf) shouldBe ScalaVersion(2, 12)
  }

  "getSourcesPaths" should "return the available sources" in {
    val conf1 = new MapSettings()
      .setProperty("sonar.sources", "sources/directory")
      .asConfig()

    Scala.getSourcesPaths(conf1) shouldBe List(Paths.get("sources/directory"))

    val conf2 = new MapSettings()
      .setProperty("sonar.sources", " sources/directory,  src/2 ")
      .asConfig()

    Scala.getSourcesPaths(conf2) shouldBe List(
      Paths.get("sources/directory"),
      Paths.get("src/2")
    )
  }

  it should "return the default value if not set" in {
    val conf = new MapSettings().asConfig()
    Scala.getSourcesPaths(conf) shouldBe List(Paths.get("src/main/scala"))
  }
}
