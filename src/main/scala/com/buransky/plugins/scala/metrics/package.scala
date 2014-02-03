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
package org.sonar.plugins.scala

import org.sonar.plugins.scala.compiler.Compiler._

package object metrics {

  def isEmptyBlock(block: Tree) = block match {
    case literal: Literal =>
      val isEmptyConstant = literal.value match {
        case Constant(value) => value.toString().equals("()")
        case _ => false
      }
      literal.isEmpty || isEmptyConstant

    case _ => block.isEmpty
  }

  def isEmptyConstructor(constructor: DefDef) = {
    if (constructor.name.startsWith(nme.CONSTRUCTOR) ||
          constructor.name.startsWith(nme.MIXIN_CONSTRUCTOR)) {

      constructor.rhs match {

        case Block(stats, expr) =>
          if (stats.size == 0) {
            true
          } else {
            stats.size == 1 &&
                (stats(0).toString().startsWith("super." + nme.CONSTRUCTOR) ||
                    stats(0).toString().startsWith("super." + nme.MIXIN_CONSTRUCTOR)) &&
                isEmptyBlock(expr)
          }

        case _ =>
          constructor.isEmpty
      }
    } else {
      false
    }
  }

  /**
   * Helper function which applies a function on every AST in a given list and
   * sums up the results.
   */
  def onList(trees: List[Tree], treeFunction: Tree => Int) = {
    trees.map(treeFunction).foldLeft(0)(_ + _)
  }
}