/*
 * Copyright (C) 2018-2022  All sonar-scala contributors
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

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.sonar.api.utils.log.LoggerLevel._
import org.sonar.api.utils.log._

class LogSpec extends AnyFlatSpec with Matchers with SonarLogTester {
  "Log" should "log debug" in {
    val log = Log(classOf[LogSpec], "test")

    log.debug("debug")
    logsFor(DEBUG) shouldBe Seq("[sonar-scala-test] debug")
  }

  it should "log info" in {
    val log = Log(classOf[LogSpec], "test")

    log.info("info")
    logsFor(INFO) shouldBe Seq("[sonar-scala-test] info")
  }

  it should "log warn" in {
    val log = Log(classOf[LogSpec], "test")

    log.warn("warn")
    logsFor(WARN) shouldBe Seq("[sonar-scala-test] warn")
  }

  it should "log error" in {
    val log = Log(classOf[LogSpec], "test")

    log.error("error")
    logsFor(ERROR) shouldBe Seq("[sonar-scala-test] error")
  }

  it should "default the prefix to sonar-scala" in {
    val log = Log(classOf[LogSpec])

    log.info("info")
    logsFor(INFO) shouldBe Seq("[sonar-scala] info")
  }
}
