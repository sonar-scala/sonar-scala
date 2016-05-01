package com.buransky.plugins.scoverage

import com.buransky.plugins.scoverage.language.Scala
import org.sonar.api.resources.Languages
import org.sonar.api.{Extension, ExtensionProvider, ServerExtension}

import scala.collection.JavaConversions._
import scala.collection.mutable.ListBuffer

class ScoverageExtensionProvider(languages: Languages) extends ExtensionProvider with ServerExtension {
  override def provide(): java.util.List[Class[_ <: Extension]] = {
    val result = ListBuffer[Class[_ <: Extension]]()

    if (languages.get(Scala.key) == null) {
      // Fix issue with multiple Scala plugins:
      // https://github.com/RadoBuransky/sonar-scoverage-plugin/issues/31
      result += classOf[Scala]
    }

    result
  }
}
