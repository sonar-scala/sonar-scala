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
package scalastyle

import org.scalatest.{FlatSpec, Inspectors, LoneElement, Matchers}
import org.sonar.api.rule.RuleStatus
import org.sonar.api.rules.RuleType
import org.sonar.api.server.rule.RulesDefinition.{Context, Repository}

class ScalastyleRulesRepositoryTest extends FlatSpec with Matchers with Inspectors with LoneElement {

  trait Ctx {
    val context = new Context()
    new ScalastyleRulesRepository().define(context)
    val repository: Repository = context.repositories.loneElement
  }

  "ScalastyleRulesRepository" should "define rules repository" in new Ctx {
    context.repositories().size shouldBe 1
  }

  it should "correctly define repository properties" in new Ctx {
    repository.key shouldBe ScalastyleRulesRepository.RepositoryKey
    repository.name shouldBe ScalastyleRulesRepository.RepositoryName
    repository.language shouldBe Scala.LanguageKey
  }

  it should "include all Scalastyle inspections" in new Ctx {
    repository.rules.size shouldBe ScalastyleInspections.AllInspections.size
  }

  it should "have all rules with non-empty properties" in new Ctx {
    forEvery(repository.rules) { rule =>
      rule.key should not be empty
      rule.internalKey should not be empty
      rule.name should not be empty
      rule.markdownDescription should not be empty
      rule.severity should not be empty
    }
  }

  it should "have all rules' keys start with org.scalastyle" in new Ctx {
    forEvery(repository.rules) { rule =>
      rule.key should startWith("org.scalastyle")
    }
  }

  it should "have all rules activated by default" in new Ctx {
    forEvery(repository.rules) { rule =>
      rule.activatedByDefault shouldBe true
    }
  }

  it should "have all rules with READY status" in new Ctx {
    forEvery(repository.rules) { rule =>
      rule.status shouldBe RuleStatus.READY
    }
  }

  it should "have all rules marked as CODE_SMELL" in new Ctx {
    forEvery(repository.rules) { rule =>
      rule.`type` shouldBe RuleType.CODE_SMELL
    }
  }
}
