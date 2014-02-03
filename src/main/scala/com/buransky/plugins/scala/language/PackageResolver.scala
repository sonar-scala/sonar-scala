/*
 * Sonar Scala Plugin
 * Copyright (C) 2011 - 2013 All contributors
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
package org.sonar.plugins.scala.language

import org.sonar.plugins.scala.compiler.{ Compiler, Parser }

/**
 * This object is a helper object for resolving the package name of
 * a given Scala file.
 *
 * @author Felix Müller
 * @since 0.1
 */
object PackageResolver {

  import Compiler._

  private lazy val parser = new Parser()

  /**
   * This function resolves the upper package name of a given file.
   *
   * @param path the path of the given file
   * @return the upper package name
   */
  def resolvePackageNameOfFile(path: String) : String = {

    def traversePackageDefs(tree: Tree) : Seq[String] = tree match {

      case PackageDef(Ident(name), List(p: PackageDef)) =>
        List(name.toString()) ++ traversePackageDefs(p)

      case PackageDef(s: Select, List(p: PackageDef)) =>
        traversePackageDefs(s) ++ traversePackageDefs(p)

      case PackageDef(Ident(name), _) =>
        List(name.toString())

      case PackageDef(s: Select, _) =>
        traversePackageDefs(s)

      case Select(Ident(identName), name) =>
        List(identName.toString(), name.toString())

      case Select(qualifiers, name) =>
        traversePackageDefs(qualifiers) ++ List(name.toString())

      case _ =>
        Nil
    }

    val packageName = traversePackageDefs(parser.parseFile(path)).foldLeft("")(_ + "." + _)
    if (packageName.length() > 0) {
      packageName.substring(1)
    } else {
      packageName
    }
  }
}