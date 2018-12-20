/*
 * Sonar Scala Plugin
 * Copyright (C) 2018 All contributors
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
package com.mwz.sonar.scala
package util

import java.nio.file.{Path, Paths}
import java.util.Optional

import org.sonar.api.batch.fs.{FileSystem, InputFile}
import org.sonar.api.batch.measure.Metric
import org.sonar.api.batch.sensor.SensorContext
import org.sonar.api.config.Configuration

import scala.language.implicitConversions

/**
 *  Scala.Option <-> Java.Optional conversions.
 *  @note Taken from https://gist.github.com/julienroubieu/fbb7e1467ab44203a09f.
 */
object JavaOptionals {
  implicit def toRichOption[T >: Null](opt: Option[T]): RichOption[T] = new RichOption[T](opt)
  implicit def toRichOptional[T](optional: Optional[T]): RichOptional[T] = new RichOptional[T](optional)
}

/** Transform this Option to an equivalent Java Optional */
class RichOption[T >: Null](opt: Option[T]) {
  def toOptional: Optional[T] = Optional.ofNullable(opt.orNull)
}

/** Transform this Optional to an equivalent Scala Option */
class RichOptional[T](opt: Optional[T]) {
  def toOption: Option[T] = if (opt.isPresent) Some(opt.get()) else None
}

/**
 * Various Path utilities.
 */
object PathUtils {
  import JavaOptionals._

  /**
   * Get a list of paths from the config for a given key.
   * Fall back to the default value.
   */
  def fromConfig(config: Configuration, key: String, default: String): List[Path] =
    config
      .get(key)
      .toOption
      .filter(_.nonEmpty)
      .getOrElse(default)
      .split(',') // scalastyle:ignore org.scalastyle.scalariform.NamedArgumentChecker
      .map(p => Paths.get(p.trim))
      .toList

  /** Current working directory. */
  def cwd: Path = Paths.get(".").toAbsolutePath.normalize

  /**
   * Construct a relative path between the base path and the suffix if the suffix is absolute,
   * otherwise resolve the relative suffix path against the 'next' path.
   */
  def relativize(base: Path, next: Path, fullOrSuffix: Path): Path =
    if (fullOrSuffix.isAbsolute) base.relativize(fullOrSuffix)
    else next.resolve(fullOrSuffix)

  /**
   * Strips out prefix from the path if it starts with the prefix.
   */
  def stripOutPrefix(prefix: Path, path: Path): Path =
    if (path.startsWith(prefix)) prefix.relativize(path)
    else path

  /**
   * Returns the module base path relative to the current working directory
   * */
  def getModuleBaseDirectory(fs: FileSystem): Path = {
    val moduleAbsolutePath = Paths.get(fs.baseDir().getAbsolutePath).normalize
    val currentWorkdirAbsolutePath = PathUtils.cwd
    currentWorkdirAbsolutePath.relativize(moduleAbsolutePath)
  }
}

object MetricUtils {

  /**
   * Save a new measure for the given metric.
   */
  def save[T <: java.io.Serializable](
    context: SensorContext,
    file: InputFile,
    metric: Metric[T],
    value: T
  ): Unit = {
    context
      .newMeasure[T]
      .on(file)
      .forMetric(metric)
      .withValue(value)
      .save()
  }
}
