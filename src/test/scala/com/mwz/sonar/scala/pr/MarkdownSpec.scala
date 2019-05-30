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

package com.mwz.sonar.scala.pr

import org.http4s.Uri
import org.scalatest.{FlatSpec, Matchers}
import org.sonar.api.batch.fs.InputFile
import org.sonar.api.batch.fs.internal.TestInputFileBuilder
import org.sonar.api.batch.rule.Severity
import org.sonar.api.rule.RuleKey

class MarkdownSpec extends FlatSpec with Matchers {
  it should "create a markdown comment" in {
    val uri: Uri = Uri.uri("https://test.com")
    val file: InputFile = TestInputFileBuilder
      .create("", "src/main/scala/Other.scala")
      .setLanguage("scala")
      .setLines(10)
      .setType(InputFile.Type.MAIN)
      .build()
    val issue: Issue = Issue(
      RuleKey.of("repo", "rule"),
      file,
      10,
      Severity.MINOR,
      "message"
    )

    val expected: Markdown =
      Markdown(
        "MINOR: message " +
        "([more](https://test.com/coding_rules?open=repo%3Arule&rule_key=repo%3Arule))"
      )

    Markdown.inline(uri, issue) shouldBe expected
  }
}
