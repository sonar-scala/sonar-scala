/*
 * Copyright (C) 2018-2019  All sonar-scala contributors
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

import org.sonar.api.batch.fs.InputFile
import org.sonar.api.batch.{InstantiationStrategy, ScannerSide}

import scala.collection.mutable

/**
 * Global collection of issues reported during project analysis.
 * Instantiated only once for a batch run.
 */
// TODO: Both @ScannerSide and @InstantiationStrategy are deprecated, we should switch
//  to the org.sonar.api.scanner.ScannerSide in the future.
@ScannerSide
@InstantiationStrategy(InstantiationStrategy.PER_BATCH)
final class GlobalIssues {
  // As far as I know this won't be accessed concurrently.
  private[this] val issues: mutable.HashMap[InputFile, List[Issue]] =
    mutable.HashMap.empty

  def add(issue: Issue): Unit =
    issues += (issue.file -> (issue :: issues.getOrElse(issue.file, List.empty)))

  def allIssues: Map[InputFile, List[Issue]] =
    Map(issues.toList: _*)
}
