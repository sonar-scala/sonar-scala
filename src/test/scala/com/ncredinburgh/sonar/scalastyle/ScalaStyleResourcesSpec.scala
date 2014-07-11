/*
 * Sonar Scala Stlye Plugin
 * Copyright (C) 2011 - 2014 All contributors
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
package com.ncredinburgh.sonar.scalastyle

import org.scalatest.{Inspectors, Matchers, FlatSpec}

/**
 * Created by hc185053 on 16/06/2014.
 */
class ScalaStyleResourcesSpec  extends FlatSpec with Matchers with Inspectors {

  "the configuration" should "allow access to description in documentation for a checker" in {
    assert( ScalaStyleResources.longDescription("line.size.limit") contains "Lines that are too long can be hard to read and horizontal scrolling is annoying")
  }

  it should "return all defined checkers" in {
    assert( ScalaStyleResources.allDefinedRules.size == 56)
  }

  it should "give rules a description" in {
    forAll (ScalaStyleResources.allDefinedRules) { r : RepositoryRule => r.description.length should be > 0 }
  }


}
