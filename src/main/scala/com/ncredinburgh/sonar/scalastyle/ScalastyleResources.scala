/*
 * Sonar Scalastyle Plugin
 * Copyright (C) 2014 All contributors
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

import java.io.InputStream
import com.typesafe.config.ConfigFactory
import org.scalastyle.ScalastyleError
import org.sonar.api.server.rule.RuleParamType
import scala.io.Source
import scala.xml.{Elem, Node, XML}

/**
 * Provides access to the various .property and XML files that Scalastyle provides
 * to describe its checkers.
 */
object ScalastyleResources {

  // accessing scalastyles definition and documentation.xml files
  private val definitions = xmlFromClassPath("/scalastyle_definition.xml")
  private val documentation = xmlFromClassPath("/scalastyle_documentation.xml")

  // accessing scalastyles reference.conf (includes additional data such as key.label)
  private val cfg = ConfigFactory.load(this.getClass.getClassLoader)

  def allDefinedRules: Seq[RepositoryRule] =
    for {
      checker <- definitions \\ "checker"
      clazz = (checker \ "@class").text
      id = (checker \ "@id").text
      desc = description(id)
      params = nodeToParams(checker, id)
    } yield RepositoryRule(clazz, id, desc, params)

  def nodeToParams(checker: Node, id: String): List[Param] =
    for {
      parameter <- (checker \\ "parameter").toList
      ruleParamKey = nodeToRuleParamKey(parameter)
      ruleParamType = nodeToRuleParamType(parameter)
      description = nodeToPropertyDescription(parameter, id)
      defaultValue = nodeToDefaultValue(parameter)
    } yield Param(ruleParamKey, ruleParamType, description, defaultValue)

  def description(key: String): String =
    descriptionFromDocumentation(key) getOrElse cfg.getConfig(key).getString("description")

  def label(key: String): String = cfg.getConfig(key).getString("label")

  private def descriptionFromDocumentation(key: String): Option[String] = {
    documentation \\ "scalastyle-documentation" \ "check" find { _ \\ "@id" exists (_.text == key) } match {
      case Some(node) =>
        val justification = {
          val text = (node \ "justification").text
          if (text.trim != "") Some(ScalastyleDocFormatter.format(text)) else None
        }
        val extraDescription = {
          val text = (node \ "extra-description").text
          if (text.trim != "") Some(ScalastyleDocFormatter.format(text)) else None
        }
        (justification, extraDescription) match {
          case (Some(j), Some(ed)) => Some(s"$j\n$ed")
          case (Some(j), None)     => Some(j)
          case _                   => None
        }
      case None => None
    }
  }

  private def nodeToRuleParamKey(n: Node): String = (n \ "@name").text.trim

  private def nodeToRuleParamType(n: Node): RuleParamType = (n \ "@type").text.trim match {
    case "string" =>
      if ((n \ "@name").text == "regex") {
        RuleParamType.STRING
      } else if ((n \ "@name").text == "header") {
        RuleParamType.TEXT
      } else {
        RuleParamType.STRING
      }
    case "integer" => RuleParamType.INTEGER
    case "boolean" => RuleParamType.BOOLEAN
    case _         => RuleParamType.STRING
  }

  private def nodeToPropertyDescription(node: Node, id: String): String = {
    val key = nodeToRuleParamKey(node)
    description(s"$id.$key")
  }

  private def nodeToDefaultValue(n: Node): String = (n \ "@default").text.trim

  private def xmlFromClassPath(s: String): Elem = XML.load(fromClassPath(s))

  private def fromClassPath(s: String): InputStream = classOf[ScalastyleError].getResourceAsStream(s)
}

object ScalastyleDocFormatter {

  private case class Out(pre: Boolean, appended: Boolean, text: String)
  private case class LineWithLeadingSpaces(spaceCount: Int, empty: Boolean, line: String)
  private case class DocLine(pre: Boolean, empty: Boolean, line: String)

  private def empty(line: String) = line.trim == ""
  private def countLeadingSpaces(line: String) = {
    val count = line.takeWhile(_ == ' ').length
    LineWithLeadingSpaces(count, empty(line), line)
  }
  private val margin = 2

  def format(in: String): String = {
    val linesWithLeadingSpaces = Source.fromString(in).getLines().map(countLeadingSpaces).toList
    val docLines = linesWithLeadingSpaces.map(l => DocLine(l.spaceCount > margin, l.empty, l.line))

    docLines.foldLeft(Out(pre = false, appended = false, "")) {
      case (out @ Out(false, false, text), line) =>
        if (line.empty) out
        else if (line.pre) Out(pre = true, appended = true, text + s"<p><pre>${line.line}\n")
        else Out(pre = false, appended = true, text + s"<p>${line.line.trim}\n")

      case (out @ Out(false, true, text), line) =>
        if (line.empty) out.copy(appended = false, text = text.trim + "</p>\n")
        else if (line.pre) Out(pre = true, appended = true, text + s"</p>\n<p><pre>${line.line}\n")
        else Out(pre = false, appended = true, text + s"${line.line.trim}\n")

      case (out @ Out(true, false, text), line) =>
        if (line.empty) out.copy(text = text + "\n")
        else if (line.pre) Out(pre = true, appended = true, text + s"${line.line}\n")
        else Out(pre = false, appended = true, text.trim + s"</pre></p>\n<p>${line.line.trim}\n")

      case (out @ Out(true, true, text), line) =>
        if (line.empty) out.copy(appended = false, text = text + "\n")
        else if (line.pre) Out(pre = true, appended = true, text + s"${line.line}\n")
        else Out(pre = false, appended = true, text + s"</pre></p>\n<p>${line.line.trim}\n")

    } match {
      case Out(true, _, text) =>
        text.trim + "</pre></p>"
      case Out(false, true, text) =>
        text.trim + "</p>"
      case Out(false, false, text) =>
        text.trim
    }
  }
}
