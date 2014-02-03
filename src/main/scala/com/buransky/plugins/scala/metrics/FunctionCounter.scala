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
 * This object is a helper object for counting all functions
 * in a given Scala source.
 *
 * @author Felix Müller
 * @since 0.1
 */
object FunctionCounter {

  import Compiler._

  private lazy val parser = new Parser()

  // TODO improve counting functions
  def countFunctions(source: String) = {

    def countFunctionTrees(tree: Tree, foundFunctions: Int = 0) : Int = tree match {

      // recursive descent until found a syntax tree with countable functions
      case PackageDef(_, content) =>
        foundFunctions + onList(content, countFunctionTrees(_, 0))

      case Template(_, _, content) =>
        foundFunctions + onList(content, countFunctionTrees(_, 0))

      case ClassDef(_, _, _, content) =>
        countFunctionTrees(content, foundFunctions)

      case ModuleDef(_, _, content) =>
        countFunctionTrees(content, foundFunctions)

      case DocDef(_, content) =>
        countFunctionTrees(content, foundFunctions)

      case ValDef(_, _, _, content) =>
        countFunctionTrees(content, foundFunctions)

      case Block(stats, expr) =>
        foundFunctions + onList(stats, countFunctionTrees(_, 0)) + countFunctionTrees(expr)

      case Apply(_, args) =>
        foundFunctions + onList(args, countFunctionTrees(_, 0))

      case Assign(_, rhs) =>
        countFunctionTrees(rhs, foundFunctions)

      case LabelDef(_, _, rhs) =>
        countFunctionTrees(rhs, foundFunctions)

      case If(cond, thenBlock, elseBlock) =>
        foundFunctions + countFunctionTrees(cond) + countFunctionTrees(thenBlock) + countFunctionTrees(elseBlock)

      case Match(selector, cases) =>
        foundFunctions + countFunctionTrees(selector) + onList(cases, countFunctionTrees(_, 0))

      case CaseDef(pat, guard, body) =>
        foundFunctions + countFunctionTrees(pat) + countFunctionTrees(guard) + countFunctionTrees(body)

      case Try(block, catches, finalizer) =>
        foundFunctions + countFunctionTrees(block) + onList(catches, countFunctionTrees(_, 0))

      case Throw(expr) =>
        countFunctionTrees(expr, foundFunctions)

      case Function(_, body) =>
        countFunctionTrees(body, foundFunctions)

      /*
       * Countable function declarations are functions and methods.
       */

      case defDef: DefDef =>
        if (isEmptyConstructor(defDef)) {
          countFunctionTrees(defDef.rhs, foundFunctions)
        } else {
          countFunctionTrees(defDef.rhs, foundFunctions + 1)
        }

      case _ =>
        foundFunctions
    }

    countFunctionTrees(parser.parse(source))
  }
}