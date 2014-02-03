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
package org.sonar.plugins.scala.metrics

import org.sonar.plugins.scala.compiler.{ Compiler, Parser }

/**
 * This object is a helper object for counting all types
 * in a given Scala source.
 *
 * @author Felix Müller
 * @since 0.1
 */
object TypeCounter {

  import Compiler._

  private lazy val parser = new Parser()

  def countTypes(source: String) = {

    def countTypeTrees(tree: Tree, foundTypes: Int = 0) : Int = tree match {

      // recursive descent until found a syntax tree with countable type declaration
      case PackageDef(_, content) =>
        foundTypes + onList(content, countTypeTrees(_, 0))

      case Template(_, _, content) =>
        foundTypes + onList(content, countTypeTrees(_, 0))

      case DocDef(_, content) =>
        countTypeTrees(content, foundTypes)

      case CaseDef(pat, guard, body) =>
        foundTypes + countTypeTrees(pat) + countTypeTrees(guard) + countTypeTrees(body)

      case DefDef(_, _, _, _, _, content) =>
        countTypeTrees(content, foundTypes)

      case ValDef(_, _, _, content) =>
        countTypeTrees(content, foundTypes)

      case Assign(_, rhs) =>
        countTypeTrees(rhs, foundTypes)

      case LabelDef(_, _, rhs) =>
        countTypeTrees(rhs, foundTypes)

      case If(cond, thenBlock, elseBlock) =>
        foundTypes + countTypeTrees(cond) + countTypeTrees(thenBlock) + countTypeTrees(elseBlock)

      case Block(stats, expr) =>
        foundTypes + onList(stats, countTypeTrees(_, 0)) + countTypeTrees(expr)

      case Match(selector, cases) =>
        foundTypes + countTypeTrees(selector) + onList(cases, countTypeTrees(_, 0))

      case Try(block, catches, finalizer) =>
        foundTypes + countTypeTrees(block) + onList(catches, countTypeTrees(_, 0)) + countTypeTrees(finalizer)

      /*
       * Countable type declarations are classes, traits and objects.
       * ClassDef represents classes and traits.
       * ModuleDef is the syntax tree for object declarations.
       */

      case ClassDef(_, _, _, content) =>
        countTypeTrees(content, foundTypes + 1)

      case ModuleDef(_, _, content) =>
        countTypeTrees(content, foundTypes + 1)

      case _ =>
        foundTypes
    }

    countTypeTrees(parser.parse(source))
  }
}