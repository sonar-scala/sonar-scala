/*
 * Sonar Scala Style Plugin
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

import java.util.Properties
import org.scalastyle.ScalastyleError
import org.sonar.api.PropertyType
import scala.xml.Node

/**
 * Provides access to the various .property and XML files that scala style provides
 * to describe its checkers.
 */
object ScalaStyleResources {

  private val defaultConfig = xmlFromClassPath("/default_config.xml")
  private val definitions = xmlFromClassPath("/scalastyle_definition.xml")
  private val documentation = xmlFromClassPath("/scalastyle_documentation.xml")
  private val properties = new Properties()

  properties.load(fromClassPath("/scalastyle_messages.properties"))

  // scala style does not provide descriptions for some checkers so add our own
  properties.load(this.getClass.getResourceAsStream("/scalastyle_override_messages.properties"))

  def allDefinedRules: Seq[RepositoryRule] = for {
    checker <- definitions \\ "scalastyle-definition" \ "checker"
    id = (checker \ "@id").text.trim
    clazz = (checker \ "@class").text.trim
    params = (checker \ "parameters" \ "parameter").map(n => Param(nodeToParameterKey(n), nodeToPropertyType(n), "", ""))
  } yield RepositoryRule(clazz, id, longDescription(id), params.toList)


  def longDescription(key: String) : String  = {
    val doc = descriptionFromDocumentation(key)
    if (doc.isEmpty) {
      shortDescription(key)
    } else {
      doc
    }
  }

  def shortDescription(key: String) : String = {
    getMessage(key + ".description")
  }

  def parameterDefault(clazz: String, paramKey: String): String = {
    val strings = for {
      check <- defaultConfig \\ "scalastyle" \ "check"  if (check \ "@class").text == clazz
      p <- check \ "parameters" \ "parameter" if (p \ "@name").text == paramKey
    } yield {
      p.text.trim
    }

    // actually only ever expect 1 match
    strings.mkString(",")
  }

  private def descriptionFromDocumentation(key: String): String = {
    val strings = for {
      check <- documentation \\ "scalastyle-documentation" \ "check" if (check \ "@id").text == key
      node <- check \ "justification"
    } yield {
      node.text.trim
    }

    strings.mkString("\n")
  }


  private def getMessage(key: String) = properties.getProperty(key)

  private def nodeToParameterKey(n: Node) = (n \ "@name").text.trim

  private def nodeToPropertyType(n: Node) = (n \ "@type").text match {
    case "string" => if ((n \ "@name").text == "regex") {
      PropertyType.REGULAR_EXPRESSION
    } else {
      PropertyType.STRING
    }
    case "integer" => PropertyType.INTEGER
    case "boolean" => PropertyType.BOOLEAN
    case _ => PropertyType.STRING
  }

  private def xmlFromClassPath(s : String) =  scala.xml.XML.load(fromClassPath(s))

  private def fromClassPath(s : String) = classOf[ScalastyleError].getResourceAsStream(s)

}
