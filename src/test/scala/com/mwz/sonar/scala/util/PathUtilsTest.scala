package com.mwz.sonar.scala.util

import java.io.File
import java.nio.file.{Path, Paths}

import org.scalatest.{FlatSpec, Matchers}

class PathUtilsTest extends FlatSpec with Matchers {
  val cwd: Path = PathUtils.cwd

  "relativize" should "successfully resolve a relative suffix path against a 'next' path" in {
    PathUtils.relativize(
      base = Paths.get("."),
      next = Paths.get(""),
      fullOrSuffix = Paths.get("suffix")
    ) shouldBe Paths.get("suffix")

    PathUtils.relativize(
      base = Paths.get("."),
      next = Paths.get("next"),
      fullOrSuffix = Paths.get("suffix")
    ) shouldBe Paths.get(s"next${File.separator}suffix")

    PathUtils.relativize(
      base = cwd,
      next = Paths.get("next"),
      fullOrSuffix = Paths.get("suffix/test")
    ) shouldBe Paths.get(s"next${File.separator}suffix/test")
  }

  it should "construct a relative path between the 'base' path and an absolute suffix" in {
    PathUtils.relativize(
      base = cwd,
      next = Paths.get(""),
      fullOrSuffix = cwd.resolve("suffix/test")
    ) shouldBe Paths.get("suffix/test")
  }
}
