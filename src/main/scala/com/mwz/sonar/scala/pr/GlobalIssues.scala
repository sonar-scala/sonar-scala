/*
 * Copyright (C) 2018-2022  All sonar-scala contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU General Lesser Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.mwz.sonar.scala
package pr

import java.util.concurrent.ConcurrentHashMap

import scala.jdk.CollectionConverters._

import org.sonar.api.batch.InstantiationStrategy
import org.sonar.api.batch.ScannerSide
import org.sonar.api.batch.fs.InputFile
import org.sonar.api.batch.rule.Severity
import org.sonar.api.rule.RuleKey

final case class Issue(
  key: RuleKey,
  file: InputFile,
  line: Int,
  severity: Severity,
  message: String
)

/**
 * Global collection of issues reported during project analysis.
 * Instantiated only once for a batch run.
 */
// TODO: Both @ScannerSide and @InstantiationStrategy are deprecated, we should switch
//  to the org.sonar.api.scanner.ScannerSide in the future.
@ScannerSide
@InstantiationStrategy(InstantiationStrategy.PER_BATCH)
final class GlobalIssues {
  private[this] val issues: ConcurrentHashMap[InputFile, List[Issue]] =
    new ConcurrentHashMap()

  def add(issue: Issue): Unit =
    issues.merge(issue.file, List(issue), (current, value) => value ::: current)

  def allIssues: Map[InputFile, List[Issue]] =
    issues.asScala.toMap
}
