/*
 * Sonar Scala Plugin
 * Copyright (C) 2018 All contributors
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
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

    conf.getPaths("path", "default/path") shouldBe List(
      Paths.get("this/is/a/path"),
      Paths.get("another/path")
    )
    conf.getPaths("not.a.path", "default/path") shouldBe List(Paths.get("default/path"))
  }

  it should "get a boolean" in {
    val conf = new MapSettings()
      .setProperty("bool.true", "true")
      .setProperty("bool.true2", "TRUE")
      .setProperty("bool.false", "false")
      .asConfig()

    conf.getValue[Boolean]("bool.true") shouldBe true
    conf.getValue[Boolean]("bool.true2") shouldBe true
    conf.getValue[Boolean]("bool.false") shouldBe false
    conf.getValue[Boolean]("not.a.bool") shouldBe false
  }
}
