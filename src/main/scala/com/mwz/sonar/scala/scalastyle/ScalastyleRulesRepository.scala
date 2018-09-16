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

import cats.instances.string._
import cats.syntax.option._
import org.scalastyle._
import org.sonar.api.batch.rule.Severity
import org.sonar.api.rule.RuleStatus
import org.sonar.api.rules.RuleType
import org.sonar.api.server.rule.RulesDefinition.{NewParam, NewRepository, NewRule}
import org.sonar.api.server.rule.{RuleParamType, RulesDefinition}

/**
 * Defines a repository for the Scalastyle inspections.
 */
final class ScalastyleRulesRepository extends RulesDefinition {
  import ScalastyleRulesRepository._

  override def define(context: RulesDefinition.Context): Unit = {
    // Create an empty repository.
    val repository = context
      .createRepository(RepositoryKey, Scala.LanguageKey)
      .setName(RepositoryName)

    // Register each Scalastyle inspection as a repository rule.
    ScalastyleInspections.AllInspections.foreach { inspection =>
      createRule(repository, inspection, template = inspection.params.nonEmpty)

      // For each template create a rule with default parameter values.
      // (except for the rules listed in the SkipTemplateInstances set)
      if (inspection.params.nonEmpty && !SkipTemplateInstances.contains(inspection.id))
        createRule(repository, inspection, template = false)
    }

    // Save the repository.
    repository.done()
  }
}

private[scalastyle] object ScalastyleRulesRepository {
  private final case class Acc(indent: Boolean, isEmpty: Boolean, text: String)

  final val RepositoryKey = "sonar-scala-scalastyle"
  final val RepositoryName = "Scalastyle"
  final val RuleClassParam = "ruleClass"

  // Skip creating template instances for the following inspections:
  // header.matches - this rule wouldn't work with a default parameter value.
  // regex - no default regex provided.
  final val SkipTemplateInstances = Set("header.matches", "regex", "scaladoc")

  /**
   * Create a new rule from the given inspection.
   */
  def createRule(repository: NewRepository, inspection: ScalastyleInspection, template: Boolean): NewRule = {
    val key = if (template) s"${inspection.clazz}-template" else inspection.clazz
    val rule = repository.createRule(key)
    rule.setInternalKey(key)
    rule.setName(inspection.label)
    rule.setMarkdownDescription(formatDescription(inspection))
    rule.setActivatedByDefault(true) // scalastyle:ignore LiteralArguments
    rule.setStatus(RuleStatus.READY)
    rule.setSeverity(levelToSeverity(inspection.defaultLevel).name)
    rule.setType(RuleType.CODE_SMELL)

    // Create parameters.
    inspection.params.foreach(param => createParam(inspection.clazz, rule, param))

    // Create Scalastyle checker parameter, which refers to the class name.
    rule
      .createParam(RuleClassParam)
      .setType(RuleParamType.STRING)
      .setDescription("Scalastyle's rule (checker) class name.")
      .setDefaultValue(inspection.clazz)

    // Set the rule as a template.
    rule.setTemplate(template)
  }

  /**
   * Create the parameter for the given rule.
   */
  def createParam(inspectionId: String, rule: NewRule, param: Param): NewParam = {
    rule
      .createParam(param.name)
      .setType(parameterTypeToRuleParamType(inspectionId, param.name, param.typ))
      .setDescription(s"${param.label}: ${param.description}")
      .setDefaultValue(param.default)
  }

  /**
   * Convert Scalastyle inspection level to SonarQube rule severity.
   */
  def levelToSeverity(level: Level): Severity = level match {
    case InfoLevel    => Severity.INFO
    case WarningLevel => Severity.MINOR
    case ErrorLevel   => Severity.MAJOR
  }

  /**
   * Convert Scalastyle inspection parameter type to SonarQube rule parameter type.
   */
  def parameterTypeToRuleParamType(ruleClass: String, name: String, typ: ParameterType): RuleParamType =
    typ match {
      // RuleParamType.TEXT is used for header parameter of the HeaderMatchesChecker inspection.
      case StringType
          if ruleClass == "org.scalastyle.file.HeaderMatchesChecker" &&
          name == "header" =>
        RuleParamType.TEXT
      case StringType =>
        RuleParamType.STRING
      case IntegerType =>
        RuleParamType.INTEGER
      case BooleanType =>
        RuleParamType.BOOLEAN
    }

  /**
   * Create a full description for a Scalastyle inspection.
   */
  def formatDescription(inspection: ScalastyleInspection): String = {
    s"*${inspection.description}*" +
    inspection.justification.map(s => s"\n\n${format(s)}").orEmpty +
    inspection.extraDescription.map(s => s"\n\n${format(s)}").orEmpty
  }

  /**
   * Reformat the text from Scalastyle docs into a markdown format.
   */
  def format(s: String): String = {
    s.lines.foldLeft(Acc(indent = false, isEmpty = true, "")) {
      case (acc, l) =>
        // Remove all backslashes as they are unnecessary.
        val line = l.replace("\\", "")
        val trailingSpaces = line.takeWhile(_ == ' ').length
        // Trim the text and replace ` with `` for inline code blocks.
        val trimmed = line.trim.replace("`", "``")

        acc match {
          // Empty line.
          case _ if trimmed.length == 0 =>
            acc.copy(isEmpty = true)

          // Previous line indented.
          case Acc(true, _, text) =>
            if (trailingSpaces <= 2)
              Acc(indent = false, isEmpty = false, s"$text\n`` $trimmed")
            else
              Acc(indent = true, isEmpty = false, s"$text\n$line")

          // Previous line not indented.
          case Acc(false, isEmpty, text) =>
            if (trailingSpaces <= 2)
              if (isEmpty) {
                val space = if (text.isEmpty) "" else "\n"
                Acc(indent = false, isEmpty = false, s"$text$space$trimmed")
              } else
                Acc(indent = false, isEmpty = false, s"$text\n$trimmed")
            else
              Acc(indent = true, isEmpty = false, s"$text\n``\n$line")
        }
    } match {
      // Close code block.
      case Acc(true, _, text) => s"$text\n``"
      case acc                => acc.text
    }
  }
}
