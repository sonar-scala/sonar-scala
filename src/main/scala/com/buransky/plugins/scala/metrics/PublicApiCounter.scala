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

import reflect.generic.ModifierFlags
import org.sonar.plugins.scala.compiler.{ Compiler, Parser }

/**
 * This object is a helper object for counting public api members.
 *
 * @author Felix Müller
 * @since 0.1
 */
object PublicApiCounter {

  import Compiler._

  private lazy val parser = new Parser()

  private case class PublicApi(isDocumented: Boolean)

  def countPublicApi(source: String) = {
    countPublicApiTrees(parser.parse(source)).size
  }

  def countUndocumentedPublicApi(source: String) = {
    countPublicApiTrees(parser.parse(source)).count(!_.isDocumented)
  }

  private def countPublicApiTrees(tree: Tree, wasDocDefBefore: Boolean = false,
      foundPublicApiMembers: List[PublicApi] = Nil) : List[PublicApi] = tree match {

    // recursive descent until found a syntax tree with countable public api declarations
    case PackageDef(_, content) =>
      foundPublicApiMembers ++ content.flatMap(countPublicApiTrees(_, false, Nil))

    case Template(_, _, content) =>
      foundPublicApiMembers ++ content.flatMap(countPublicApiTrees(_, false, Nil))

    case DocDef(_, content) =>
      countPublicApiTrees(content, true, foundPublicApiMembers)

    case Block(stats, expr) =>
      foundPublicApiMembers ++ stats.flatMap(countPublicApiTrees(_, false, Nil)) ++ countPublicApiTrees(expr)

    case Apply(_, args) =>
      foundPublicApiMembers ++ args.flatMap(countPublicApiTrees(_, false, Nil))

    case classDef: ClassDef if (classDef.mods.hasFlag(ModifierFlags.PRIVATE)) =>
      countPublicApiTrees(classDef.impl, false, foundPublicApiMembers)

    case moduleDef: ModuleDef if (moduleDef.mods.hasFlag(ModifierFlags.PRIVATE)) =>
      countPublicApiTrees(moduleDef.impl, false, foundPublicApiMembers)

    case defDef: DefDef if (isEmptyConstructor(defDef) || defDef.mods.hasFlag(ModifierFlags.PRIVATE)) =>
      countPublicApiTrees(defDef.rhs, false, foundPublicApiMembers)

    case valDef: ValDef if (valDef.mods.hasFlag(ModifierFlags.PRIVATE)) =>
      countPublicApiTrees(valDef.rhs, false, foundPublicApiMembers)

    /*
     * Countable public api declarations are classes, objects, traits, functions,
     * methods and attributes with public access.
     */

    case ClassDef(_, _, _, impl) =>
      countPublicApiTrees(impl, false, foundPublicApiMembers ++ List(PublicApi(wasDocDefBefore)))

    case ModuleDef(_, _, impl) =>
      countPublicApiTrees(impl, false, foundPublicApiMembers ++ List(PublicApi(wasDocDefBefore)))

    case defDef: DefDef =>
      foundPublicApiMembers ++ List(PublicApi(wasDocDefBefore))

    case valDef: ValDef =>
      foundPublicApiMembers ++ List(PublicApi(wasDocDefBefore))

    case _ =>
      foundPublicApiMembers
  }
}