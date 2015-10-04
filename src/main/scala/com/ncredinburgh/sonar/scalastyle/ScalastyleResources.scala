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
import java.util.Properties
import org.scalastyle.ScalastyleError
import org.sonar.api.PropertyType
import scala.xml.{Elem, XML, Node}

/**
 * Provides access to the various .property and XML files that Scalastyle provides
 * to describe its checkers.
 */
object ScalastyleResources {

  private val definitions = xmlFromClassPath("/scalastyle_definition.xml")
  private val documentation = xmlFromClassPath("/scalastyle_documentation.xml")
  private val properties = new Properties()

  properties.load(fromClassPath("/scalastyle_messages.properties"))

  // Scalastyle does not provide descriptions for some checkers so add our own
  properties.load(this.getClass.getResourceAsStream("/scalastyle_override_messages.properties"))

  def allDefinedRules: Seq[RepositoryRule] = for {
    checker <- definitions \\ "checker"
    clazz = (checker \ "@class").text
    id = (checker \ "@id").text
    longDesc = longDescription(id)
    params = nodeToParams(checker, id)
  } yield RepositoryRule(clazz, id, longDesc, params)

  def nodeToParams(checker: Node, id: String): List[Param] = for {
    parameter <- (checker \\ "parameter").toList
    key = nodeToParameterKey(parameter)
    propertyType = nodeToPropertyType(parameter)
    description = nodeToPropertyDescription(parameter, id)
    defaultValue = nodeToDefaultValue(parameter)
  } yield Param(key, propertyType, description, defaultValue)

  def longDescription(key: String): String = descriptionFromDocumentation(key) getOrElse shortDescription(key)

  def shortDescription(key: String): String = getMessage(key + ".description")

  private def descriptionFromDocumentation(key: String): Option[String] = {
    documentation \\ "scalastyle-documentation" \ "check" find { _ \\ "@id" exists (_.text == key) } match {
      case Some(node) => {
        val description =  (node \ "justification").text.trim
        if (description != "") Some(description) else None
      }
      case None => None
    }
  }

  private def getMessage(key: String): String = properties.getProperty(key)

  private def nodeToParameterKey(n: Node): String = (n \ "@name").text.trim

  private def nodeToPropertyType(n: Node): PropertyType = (n \ "@type").text.trim match {
    case "string" => if ((n \ "@name").text == "regex") {
      PropertyType.REGULAR_EXPRESSION
    } else if ((n \ "@name").text == "header") {
      PropertyType.TEXT
    } else {
      PropertyType.STRING
    }
    case "integer" => PropertyType.INTEGER
    case "boolean" => PropertyType.BOOLEAN
    case _ => PropertyType.STRING
  }

  private def nodeToPropertyDescription(node: Node, id: String): String = {
    val key = nodeToParameterKey(node)
    getMessage(id + "." + key + ".description")
  }

  private def nodeToDefaultValue(n: Node): String = (n \ "@default").text.trim

  private def xmlFromClassPath(s: String): Elem =  XML.load(fromClassPath(s))

  private def fromClassPath(s: String): InputStream = classOf[ScalastyleError].getResourceAsStream(s)
}
