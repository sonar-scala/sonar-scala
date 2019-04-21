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

import org.http4s.Uri

final case class Markdown(text: String) extends AnyVal

object Markdown {

  /**
   * Generate an inline comment.
   * The format is: "SEVERITY: TEXT ([more](link to the rule))"
   */
  def inline(baseUrl: Uri, issue: Issue): Markdown = {
    val ruleUri: Uri = baseUrl
      .withPath("coding_rules")
      .withQueryParam("open", issue.key.toString)
    // TODO: Add severity image.
    Markdown(s"${issue.severity.name}: ${issue.message} ([more]($ruleUri))")
  }
}
