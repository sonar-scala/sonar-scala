package com.mwz.sonar.scala.util

import java.nio.file.Paths

import com.mwz.sonar.scala.util.PathUtils._
import org.scalatest.{FlatSpec, Matchers}
import org.sonar.api.batch.fs.internal.DefaultFileSystem

class PathUtilsTest extends FlatSpec with Matchers {
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
    ) shouldBe Paths.get(s"next/suffix")

    PathUtils.relativize(
      base = cwd,
      next = Paths.get("next"),
      fullOrSuffix = Paths.get("suffix/test")
    ) shouldBe Paths.get(s"next/suffix/test")
  }

  it should "construct a relative path between the 'base' path and an absolute suffix" in {
    PathUtils.relativize(
      base = cwd,
      next = Paths.get(""),
      fullOrSuffix = cwd.resolve("suffix/test")
    ) shouldBe Paths.get("suffix/test")
  }

  "stripOutPrefix" should "successfully strip out the prefix" in {
    PathUtils.stripOutPrefix(
      prefix = Paths.get("a/b"),
      path = Paths.get("a/b/c")
    ) shouldBe Paths.get("c")

    PathUtils.stripOutPrefix(
      prefix = Paths.get("x/y"),
      path = Paths.get("a/b/c")
    ) shouldBe Paths.get("a/b/c")
  }

  "getModuleBaseDirectory" should "get module base directory" in {
    getModuleBaseDirectory(new DefaultFileSystem(cwd)) shouldBe Paths.get("")
    getModuleBaseDirectory(
      new DefaultFileSystem(cwd.resolve("module"))
    ) shouldBe Paths.get("module")
  }
}
