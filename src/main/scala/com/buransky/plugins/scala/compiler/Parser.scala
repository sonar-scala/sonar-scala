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
package org.sonar.plugins.scala.compiler

import tools.nsc._
import io.AbstractFile

/**
 * This class is a wrapper for accessing the parser of the Scala compiler
 * from Java in a more convenient way.
 *
 * @author Felix Müller
 * @since 0.1
 */
class Parser {

  import Compiler._

  def parse(code: String) : Tree = {
    val batchSourceFile = new util.BatchSourceFile("", code.toCharArray)
    parse(batchSourceFile, code.toCharArray)
  }

  def parseFile(path: String) = {
    val batchSourceFile = new util.BatchSourceFile(AbstractFile.getFile(path))
    parse(batchSourceFile, batchSourceFile.content.array)
  }

  private def parse(batchSourceFile: util.BatchSourceFile, code: Array[Char]) = {
    val scriptSourceFile = new util.ScriptSourceFile(batchSourceFile, code, 0)
    try {
      val parser = new syntaxAnalyzer.SourceFileParser(scriptSourceFile)
      val tree = parser.templateStatSeq(false)._2
      parser.makePackaging(0, parser.atPos(0, 0, 0)(Ident(nme.EMPTY_PACKAGE_NAME)), tree)
    } catch {
      case _ => {
        val unit = new CompilationUnit(batchSourceFile)
        val unitParser = new syntaxAnalyzer.UnitParser(unit) {
          override def showSyntaxErrors() { }
        }
        unitParser.smartParse()
      }
    }
  }
}