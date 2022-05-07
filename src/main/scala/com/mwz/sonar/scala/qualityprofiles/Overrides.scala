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
package qualityprofiles

import org.sonar.api.batch.rule.Severity

/**
 * Defines overrides for the quality profiles:
 * @param blacklist A set of rule ids to be excluded.
 * @param severities A mapping from rule ids to new severities.
 * @param params A mapping from rule ids to key-value pairs with new parameter values.
 */
final case class Overrides(
  blacklist: Set[String],
  severities: Map[String, Severity],
  params: Map[String, Map[String, String]]
)
