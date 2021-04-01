/*
 * Copyright (C) 2018-2021  All sonar-scala contributors
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
package scalastyle

import org.scalastyle.FileSpec
import org.scalastyle.Message
import org.scalastyle.ScalastyleConfiguration
import org.scalastyle.{ScalastyleChecker => Checker}
import org.sonar.api.scanner.ScannerSide

trait ScalastyleCheckerAPI {
  private[scalastyle] def checkFiles(
    checker: Checker[FileSpec],
    configuration: ScalastyleConfiguration,
    files: Seq[FileSpec]
  ): List[Message[FileSpec]]
}

/**
 * A wrapper around ScalastyleChecker so we can inject it into the sensor.
 */
@ScannerSide
final class ScalastyleChecker extends ScalastyleCheckerAPI {
  private[scalastyle] def checkFiles(
    checker: Checker[FileSpec],
    configuration: ScalastyleConfiguration,
    files: Seq[FileSpec]
  ): List[Message[FileSpec]] =
    checker.checkFiles(configuration, files)
}
