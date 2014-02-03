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

import collection.mutable.{ ListBuffer, HashMap }

import org.sonar.api.measures.{ CoreMetrics, Measure, Metric }
import org.sonar.plugins.scala.util.MetricDistribution

import scalariform.lexer.Tokens._
import scalariform.parser._

/**
 * This object is a helper object for measuring complexity
 * in a given Scala source.
 *
 * @author Felix Müller
 * @since 0.1
 */
object ComplexityCalculator {

  private lazy val classComplexityRanges = Array[Number](0, 5, 10, 20, 30, 60, 90)
  private lazy val functionComplexityRanges = Array[Number](1, 2, 4, 6, 8, 10, 12)

  def measureComplexity(source: String) : Int = ScalaParser.parse(source) match {
    case Some(ast) => measureComplexity(ast)
    case _ => 0
  }

  def measureComplexityOfClasses(source: String) : MetricDistribution = {
    measureComplexityDistribution(source, CoreMetrics.CLASS_COMPLEXITY_DISTRIBUTION,
        classComplexityRanges, classOf[TmplDef])
  }

  def measureComplexityOfFunctions(source: String) : MetricDistribution = {
    measureComplexityDistribution(source, CoreMetrics.FUNCTION_COMPLEXITY_DISTRIBUTION,
        functionComplexityRanges, classOf[FunDefOrDcl])
  }

  private def measureComplexityDistribution(source: String, metric: Metric, ranges: Array[Number],
      typeOfTree: Class[_ <: AstNode]) = {

    def allTreesIn(source: String) : Seq[AstNode] = ScalaParser.parse(source) match {
      case Some(ast) => collectTrees(ast, typeOfTree)
      case _ => Nil
    }

    val distribution = new MetricDistribution(metric, ranges)
    allTreesIn(source).foreach(ast => distribution.add(measureComplexity(ast)))
    distribution
  }

  private def measureComplexity(ast: AstNode) : Int = {
    var complexity = 0

    // TODO measure complexity of return statements
    // TODO howto handle nested classes and functions? should
    //      surrounding function complexity consist of inner function and its own or only it own one?
    def measureComplexityOfTree(tree: AstNode) {
      tree match {

        case CaseClause(_, _)
            | DoExpr(_, _, _, _, _)
            | ForExpr(_, _, _, _, _, _, _)
            | FunDefOrDcl(_, _, _, _, _, _, _)
            | IfExpr(_, _, _, _, _)
            | WhileExpr(_, _, _, _) =>
          complexity += 1

        case expr: Expr =>
          if (expr.tokens.head.tokenType == THROW) {
            complexity += 1
          }

        case _ =>
      }

      tree.immediateChildren.foreach(measureComplexityOfTree)
    }

    measureComplexityOfTree(ast)
    complexity
  }

  private def collectTrees(ast: AstNode, typeOfTree: Class[_ <: AstNode]) : Seq[AstNode] = {
    val nodes = ListBuffer[AstNode]()

    def collectTreesOfSpecificType(tree: AstNode) {
      if (tree.getClass == typeOfTree) {
        nodes += tree
      }
      tree.immediateChildren.foreach(collectTreesOfSpecificType)
    }

    collectTreesOfSpecificType(ast)
    nodes
  }
}