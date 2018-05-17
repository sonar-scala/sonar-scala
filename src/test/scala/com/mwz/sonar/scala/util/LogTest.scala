package com.mwz.sonar.scala
package util

import org.scalatest.{FlatSpec, Matchers}
import org.sonar.api.utils.log.LoggerLevel._
import org.sonar.api.utils.log._

class LogTest extends FlatSpec with Matchers with SonarLogTester {

  "Log" should "log debug" in {
    val log = Log(classOf[LogTest], "test")

    log.debug("debug")
    logsFor(DEBUG) shouldBe Seq("[test] debug")
  }

  it should "log info" in {
    val log = Log(classOf[LogTest], "test")

    log.info("info")
    logsFor(INFO) shouldBe Seq("[test] info")
  }

  it should "log warn" in {
    val log = Log(classOf[LogTest], "test")

    log.warn("warn")
    logsFor(WARN) shouldBe Seq("[test] warn")
  }

  it should "log error" in {
    val log = Log(classOf[LogTest], "test")

    log.error("error")
    logsFor(ERROR) shouldBe Seq("[test] error")
  }
}
