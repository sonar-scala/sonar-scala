package com.mwz.sonar.scala

import java.nio.file.Paths

import org.scalatest.{FlatSpec, Matchers}
import org.sonar.api.config.internal.MapSettings

class ScalaTest extends FlatSpec with Matchers {
  "getScalaVersion" should "return the available version" in {
    val conf = new MapSettings()
      .setProperty("sonar.scala.version", "123")
      .asConfig()

    Scala.getScalaVersion(conf) shouldBe "123"
  }

  it should "return the default value if not set" in {
    val conf = new MapSettings().asConfig()
    Scala.getScalaVersion(conf) shouldBe "2.11.0"
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
