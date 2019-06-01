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

package com.mwz.sonar.scala.util

import cats.effect.IO
import com.mwz.sonar.scala.util.Logger._
import org.scalatest.{FlatSpec, Matchers}
import org.sonar.api.utils.log.LoggerLevel._
import org.sonar.api.utils.log.SonarLogTester

import cats.implicits._

class LoggerSpec extends FlatSpec with Matchers with SonarLogTester {
  trait Context {
    val log: IO[Logger[IO]] = Logger.create(classOf[LoggerSpec], "test")
  }

  it should "log debug" in new Context {
    log.flatMap(_.debug("debug")).unsafeRunSync()
    logsFor(DEBUG) shouldBe Seq("[sonar-scala-test] debug")
  }

  it should "log info" in new Context {
    log.flatMap(_.info("info")).unsafeRunSync()
    logsFor(INFO) shouldBe Seq("[sonar-scala-test] info")
  }

  it should "log warn" in new Context {
    log.flatMap(_.warn("warn")).unsafeRunSync()
    logsFor(WARN) shouldBe Seq("[sonar-scala-test] warn")
  }

  it should "log error" in new Context {
    log.flatMap(_.error("error")).unsafeRunSync()
    logsFor(ERROR) shouldBe Seq("[sonar-scala-test] error")
  }

  it should "default the prefix to sonar-scala" in {
    val log: IO[Logger[IO]] = Logger.create(classOf[LoggerSpec])

    log.flatMap(_.info("info")).unsafeRunSync()
    logsFor(INFO) shouldBe Seq("[sonar-scala] info")
  }
}
