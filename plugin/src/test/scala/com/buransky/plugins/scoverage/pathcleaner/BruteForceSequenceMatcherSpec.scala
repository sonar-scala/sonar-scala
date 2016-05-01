/*
 * Sonar Scoverage Plugin
 * Copyright (C) 2013 Rado Buransky
 * dev@sonar.codehaus.org
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
package com.buransky.plugins.scoverage.pathcleaner

import org.junit.runner.RunWith
import org.scalatest.mock.MockitoSugar
import org.scalatest.junit.JUnitRunner
import org.scalatest.FlatSpec
import com.buransky.plugins.scoverage.pathcleaner.BruteForceSequenceMatcher.PathSeq
import org.scalatest.Matchers
import java.io.File
import org.mockito.Mockito._

@RunWith(classOf[JUnitRunner])
class BruteForceSequenceMatcherSpec extends FlatSpec with Matchers with MockitoSugar {
   
  // file-map of all files under baseDir/sonar.sources organized by their filename
  val filesMap: Map[String, Seq[PathSeq]] = Map (
    "rootTestFile.scala" -> List(List("testProject", "main", "rootTestFile.scala")),
    "nestedTestFile.scala" -> List(List("testProject", "main", "some", "folders", "nestedTestFile.scala")),
    "multiFile.scala" -> List(
                              List("testProject", "main", "some", "multiFile.scala"),
                              List("testProject", "main", "some", "folder", "multiFile.scala")
                              )
  )

  // baseDir = testProject   sonar.sources = main
  val testee = new BruteForceSequenceMatcherTestee("/testProject/main", filesMap)
  
  
  
  behavior of "BruteForceSequenceMatcher with absolute report filenames"
  
  it should "provide just the filename for top level files" in {
    testee.getSourceRelativePath(List("testProject", "main", "rootTestFile.scala")).get shouldEqual List("rootTestFile.scala")
  }
  
  it should "provide the filename and the folders for nested files" in {
    testee.getSourceRelativePath(List("testProject", "main", "some", "folders", "nestedTestFile.scala")).get shouldEqual List("some", "folders", "nestedTestFile.scala")
  }
  
  it should "find the correct file if multiple files with same name exist" in {
    testee.getSourceRelativePath(List("testProject", "main", "some", "multiFile.scala")).get shouldEqual List("some", "multiFile.scala")
    testee.getSourceRelativePath(List("testProject", "main", "some", "folder", "multiFile.scala")).get shouldEqual List("some", "folder", "multiFile.scala")
  }
  
  
  
  
  behavior of "BruteForceSequenceMatcher with filenames relative to the base dir"
  
  it should "provide just the filename for top level files" in {
    testee.getSourceRelativePath(List("main", "rootTestFile.scala")).get shouldEqual List("rootTestFile.scala")
  }
  
  it should "provide the filename and the folders for nested files" in {
    testee.getSourceRelativePath(List("main", "some", "folders", "nestedTestFile.scala")).get shouldEqual List("some", "folders", "nestedTestFile.scala")
  }
  
  it should "find the correct file if multiple files with same name exist" in {
    testee.getSourceRelativePath(List("main", "some", "multiFile.scala")).get shouldEqual List("some", "multiFile.scala")
    testee.getSourceRelativePath(List("main", "some", "folder", "multiFile.scala")).get shouldEqual List("some", "folder", "multiFile.scala")
  }
  
  
  
  
  behavior of "BruteForceSequenceMatcher with filenames relative to the src dir"
  
  it should "provide just the filename for top level files" in {
    testee.getSourceRelativePath(List("rootTestFile.scala")).get shouldEqual List("rootTestFile.scala")
  }
  
  it should "provide the filename and the folders for nested files" in {
    testee.getSourceRelativePath(List("some", "folders", "nestedTestFile.scala")).get shouldEqual List("some", "folders", "nestedTestFile.scala")
  }
  
  it should "find the correct file if multiple files with same name exist" in {
    testee.getSourceRelativePath(List("some", "multiFile.scala")).get shouldEqual List("some", "multiFile.scala")
    testee.getSourceRelativePath(List("some", "folder", "multiFile.scala")).get shouldEqual List("some", "folder", "multiFile.scala")
  }
  
  
  

  class BruteForceSequenceMatcherTestee(absoluteSrcPath: String, filesMap: Map[String, Seq[PathSeq]])
      extends BruteForceSequenceMatcher(mock[File], "") {

    def srcDir = {
      val dir = mock[File]
      when(dir.isAbsolute).thenReturn(true)
      when(dir.isDirectory).thenReturn(true)
      when(dir.getAbsolutePath).thenReturn(absoluteSrcPath)
      dir
    }

    override private[pathcleaner] def initSourceDir(): File = srcDir
    override private[pathcleaner] def initFilesMap(): Map[String, Seq[PathSeq]] = filesMap
  }
}
