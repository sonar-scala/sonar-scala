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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package com.mwz.sonar.scala
package util

import org.scalatest.{FlatSpec, Matchers}
import org.sonar.api.utils.log.LoggerLevel._
import org.sonar.api.utils.log._

class LogSpec extends FlatSpec with Matchers with SonarLogTester {

  "Log" should "log debug" in {
    val log = Log(classOf[LogSpec], "test")

    log.debug("debug")
    logsFor(DEBUG) shouldBe Seq("[test] debug")
  }

  it should "log info" in {
    val log = Log(classOf[LogSpec], "test")

    log.info("info")
    logsFor(INFO) shouldBe Seq("[test] info")
  }

  it should "log warn" in {
    val log = Log(classOf[LogSpec], "test")

    log.warn("warn")
    logsFor(WARN) shouldBe Seq("[test] warn")
  }

  it should "log error" in {
    val log = Log(classOf[LogSpec], "test")

    log.error("error")
    logsFor(ERROR) shouldBe Seq("[test] error")
  }
}
