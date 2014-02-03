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

import scalariform.parser._

/**
 * This object is a helper object for counting all statements
 * in a given Scala source.
 *
 * @author Felix Müller
 * @since 0.1
 */
object StatementCounter {

  def countStatements(source: String) = {

    def countStatementTreesOnList(trees: List[AstNode]) : Int = {
      trees.map(countStatementTrees(_)).foldLeft(0)(_ + _)
    }

    def countStatementsOfDefOrDcl(body: AstNode) : Int = {
      val bodyStatementCount = countStatementTrees(body)
      if (bodyStatementCount == 0) {
        1
      } else {
        bodyStatementCount
      }
    }

    def countStatementTrees(tree: AstNode, foundStatements: Int = 0) : Int = tree match {

      case AnonymousFunction(_, _, body) =>
        foundStatements + countStatementTrees(body)

      case FunDefOrDcl(_, _, _, _, _, funBodyOption, _) =>
        funBodyOption match {
          case Some(funBody) =>
            foundStatements + countStatementsOfDefOrDcl(funBody)
          case _ =>
            foundStatements
        }

      case PatDefOrDcl(_, _, _, _, equalsClauseOption) =>
        equalsClauseOption match {
          case Some(equalsClause) =>
            foundStatements + countStatementsOfDefOrDcl(equalsClause._2)
          case _ =>
            foundStatements
        }

      case ForExpr(_, _, _, _, _, yieldOption, body) =>
        val bodyStatementCount = countStatementTrees(body)
        yieldOption match {
          case Some(_) =>
            if (bodyStatementCount == 0) {
              foundStatements + 2
            } else {
              foundStatements + bodyStatementCount + 1
            }

          case _ =>
            foundStatements + bodyStatementCount + 1
        }

      case IfExpr(_, _, _, body, elseClauseOption) =>
        elseClauseOption match {
          case Some(elseClause) =>
            foundStatements + 1 + countStatementTrees(body) + countStatementTrees(elseClause)
          case _ =>
            foundStatements + 1 + countStatementTrees(body)
        }

      case ElseClause(_, _, elseBody) =>
        countStatementTrees(elseBody, foundStatements + 1)

      case CallExpr(exprDotOpt, _, _, newLineOptsAndArgumentExpr, _) =>
        val bodyStatementCount = countStatementTreesOnList(newLineOptsAndArgumentExpr.map(_._2))
        if (bodyStatementCount > 1) {
          foundStatements + 1 + bodyStatementCount
        } else {
          foundStatements + 1
        }

      case InfixExpr(_, _, _, _) | PostfixExpr(_, _) =>
        foundStatements + 1

      case _ =>
        foundStatements + countStatementTreesOnList(tree.immediateChildren)
    }

    ScalaParser.parse(source) match {
      case Some(ast) => countStatementTrees(ast)
      case _ => 0
    }
  }
}