package com.ncredinburgh.sonar.scalastyle.testUtils

import java.util

import com.ncredinburgh.sonar.scalastyle.{Constants, ScalastyleResources}
import org.sonar.api.rule.RuleKey
import org.sonar.api.rules.{RulePriority, Rule, RuleQuery, RuleFinder}
import scala.collection.JavaConversions._

object TestRuleFinder extends RuleFinder {

  override def findByKey(repositoryKey: String, key: String): Rule = findAll(RuleQuery.create()).find(r => r.getRepositoryKey == repositoryKey && r.getKey == key).orNull

  override def findByKey(key: RuleKey): Rule = findAll(RuleQuery.create()).find(r => r.getRepositoryKey == key.repository() && r.getKey == key.rule()).orNull

  override def findById(ruleId: Int): Rule = findAll(RuleQuery.create()).find(r => r.getId == ruleId).orNull

  override def findAll(query: RuleQuery): util.Collection[Rule] = {
    ScalastyleResources.allDefinedRules map {
      case r =>
        val rule = Rule.create()
        val key = r.id
        rule.setRepositoryKey(Constants.RepositoryKey)
        rule.setLanguage(Constants.ScalaKey)
        rule.setKey(r.clazz)
        rule.setName(ScalastyleResources.label(key))
        rule.setDescription(r.description)
        rule.setConfigKey(key)
        // currently all rules comes with "warning" default level so we can treat with major severity
        rule.setSeverity(RulePriority.MAJOR)


        val params = r.params map {
          case param =>
            rule
              .createParameter
              .setDefaultValue(param.defaultVal)
              .setType(param.`type`.`type`())
              .setKey(param.name)
              .setDescription(param.desc)
        }
        rule

    }
  }

  override def find(query: RuleQuery): Rule = ???
}
