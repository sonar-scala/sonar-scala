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
package com.buransky.plugins.scoverage.resource

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{Matchers, FlatSpec}
import com.buransky.plugins.scoverage.language.Scala

@RunWith(classOf[JUnitRunner])
class SingleDirectorySpec extends FlatSpec with Matchers {
  behavior of "SingleDirectory"

  it should "work for simple relative unix name" in {
    val dir = new SingleDirectory("a", new Scala)
    dir.getName should equal("a")
    dir.getParent should equal(null)
  }

  it should "work for simple absolute unix name" in {
    val dir = new SingleDirectory("/a", new Scala)
    dir.getName should equal("a")
    dir.getParent should equal(null)
  }

  it should "work for complex relative unix name" in {
    complexCase("a/b/c/d/eee", List("eee", "d", "c", "b", "a"))
  }

  it should "work for complex absolute unix name" in {
    complexCase("/a/b/c/d/eee", List("eee", "d", "c", "b", "a"))
  }

  private def complexCase(path: String, dirs: List[String]) {
    def check(dir: SingleDirectory, dirs: List[String]) {
      dir.getName should equal(dirs.head)

      dir.getParent match {
        case p: SingleDirectory => check(p, dirs.tail)
        case _ => dir.getParent should equal(null)
      }
    }

    check(new SingleDirectory(path, new Scala), dirs)
  }
}
