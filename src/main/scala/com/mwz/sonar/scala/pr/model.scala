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

// TODO: Capture more custom errors.
@SuppressWarnings(Array("IncorrectlyNamedExceptions"))
sealed trait ReviewError extends Exception
case object NoFilesInPR extends ReviewError

sealed trait PrStatus extends Product with Serializable
case object Pending extends PrStatus
case object Success extends PrStatus
final case class Error(reviewStatus: ReviewStatus) extends PrStatus
final case class Failure(error: Throwable) extends PrStatus

final case class ReviewStatus(blocker: Int, critical: Int)
object ReviewStatus {
  def description(reviewStatus: ReviewStatus): String = {
    reviewStatus match {
      case ReviewStatus(blockers, critical) if blockers > 0 && critical > 0 =>
        s"$blockers blocker${form(blockers)} and $critical critical issue${form(critical)}"
      case ReviewStatus(blockers, _) if blockers > 0 =>
        s"$blockers blocker${form(blockers)}"
      case ReviewStatus(_, critical) if critical > 0 =>
        s"$critical critical issue${form(critical)}"
      case _ =>
        "no critical or blocker issues"
    }
  }

  private[pr] def form(i: Int): String =
    if (i > 1) "s" else ""
}

final case class Markdown(text: String) extends AnyVal
object Markdown {

  /**
   * Generate an inline comment.
   * The format is: "SEVERITY: TEXT ([more](link to the rule))"
   */
  def inline(baseUrl: Uri, issue: Issue): Markdown = {
    val ruleUri: Uri =
      (baseUrl / "coding_rules")
        .withQueryParam("open", issue.key.toString)
    // TODO: Add severity image.
    Markdown(s"${issue.severity.name}: ${issue.message} ([more]($ruleUri))")
  }
}
