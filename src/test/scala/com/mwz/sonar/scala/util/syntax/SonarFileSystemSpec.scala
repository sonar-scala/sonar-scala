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
package syntax

import java.nio.file.Path
import java.nio.file.Paths

import com.mwz.sonar.scala.util.syntax.SonarFileSystem._
import org.mockito.ArgumentMatchers._
import org.mockito.Mockito._
import org.scalatest.OptionValues
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.mockito.MockitoSugar
import org.sonar.api.batch.fs.FileSystem
import org.sonar.api.batch.fs.internal.DefaultFileSystem

class SonarFileSystemSpec extends AnyFlatSpec with Matchers with OptionValues with MockitoSugar {
  it should "attempt to resolve paths" in {
    val fs = new DefaultFileSystem(Paths.get("./"))

    val paths = List(Paths.get("path/1"), Paths.get("path/2"))
    fs.resolve(paths) shouldBe List(
      Paths.get("./").resolve("path/1").toAbsolutePath.normalize.toFile,
      Paths.get("./").resolve("path/2").toAbsolutePath.normalize.toFile
    )

    val path: Option[Path] = Some(Paths.get("another/path"))
    fs.resolve(path).value shouldBe
    Paths.get("./").resolve("another/path").toAbsolutePath.normalize.toFile
  }

  it should "handle exceptions gracefully" in {
    val fs = mock[FileSystem]
    val path = List(Paths.get("path"))

    when(fs.resolvePath(any())).thenThrow(new RuntimeException())

    fs.resolve(path) shouldBe empty
  }
}
