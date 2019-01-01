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

package com.mwz.sonar
package scala
package junit

import java.io.File
import java.nio.file.Paths

import org.scalatest.{FlatSpec, LoneElement, Matchers}
import org.sonar.api.batch.fs.InputFile
import org.sonar.api.batch.fs.internal.{DefaultFileSystem, TestInputFileBuilder}

class JUnitReportParserSpec extends FlatSpec with Matchers with WithFiles with LoneElement {
  it should "get report files" in {
    withFiles("file.xml", "file2.xml", "other.txt") { files =>
      val directories = files.map(_.getParentFile).distinct.toList
      val baseDir = directories.loneElement
      val fs = new DefaultFileSystem(baseDir)
      val parser = new JUnitReportParser(fs)

      parser.reportFiles(directories) should contain theSameElementsAs List(
        baseDir.getAbsoluteFile.toPath.resolve("file.xml").toFile,
        baseDir.getAbsoluteFile.toPath.resolve("file2.xml").toFile
      )
    }
  }

  it should "parse report files" in {
    val fs = new DefaultFileSystem(Paths.get("./"))
    val parser = new JUnitReportParser(fs)

    val expected = JUnitReport("TestFile", tests = 8, errors = 3, failures = 2, skipped = 1, time = 0.049f)

    parser
      .parseReportFiles(List(new File("./src/test/resources/junit/report.xml")))
      .loneElement shouldBe expected
  }

  it should "resolve files" in {
    val fs = new DefaultFileSystem(Paths.get("./"))
    val parser = new JUnitReportParser(fs)

    val testFile = TestInputFileBuilder
      .create("", "path/to/tests/TestFile.scala")
      .build()

    val tests = List(Paths.get("path/to/tests"))
    val report = JUnitReport("TestFile", tests = 8, errors = 3, failures = 2, skipped = 1, time = 0.049f)
    val expected: Map[InputFile, JUnitReport] = Map(testFile -> report)

    fs.add(testFile)
    parser.resolveFiles(tests, List(report)) shouldBe expected
  }

  it should "parse" in {
    val fs = new DefaultFileSystem(Paths.get("./"))
    val parser = new JUnitReportParser(fs)

    val tests = List(Paths.get("path/to/tests"))
    val directories = List(new File("src/test/resources/junit"))
    val testFile = TestInputFileBuilder
      .create("", "path/to/tests/TestFile.scala")
      .build()
    val report = JUnitReport("TestFile", tests = 8, errors = 3, failures = 2, skipped = 1, time = 0.049f)
    val expected: Map[InputFile, JUnitReport] = Map(testFile -> report)

    fs.add(testFile)
    parser.parse(tests, directories) shouldBe expected
  }
}
