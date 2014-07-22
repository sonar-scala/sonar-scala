package com.ncredinburgh.sonar.scalastyle

import java.io.File
import java.nio.charset.StandardCharsets

import org.mockito.Mockito._
import org.scalastyle.{ConfigurationChecker, ErrorLevel, ScalastyleConfiguration}
import org.scalatest.mock.MockitoSugar
import org.scalatest.{FlatSpec, Matchers, PrivateMethodTester}
import org.sonar.api.profiles.RulesProfile
import org.sonar.api.rules.{Rule, RulePriority}

import scala.collection.JavaConversions._

class ScalastyleRunnerSpec extends FlatSpec with Matchers with MockitoSugar with PrivateMethodTester {

  trait Fixture {
    val checker1 = ConfigurationChecker("org.scalastyle.scalariform.MultipleStringLiteralsChecker", ErrorLevel, true, Map(), None, None)
    val checker2 = ConfigurationChecker("org.scalastyle.file.HeaderMatchesChecker", ErrorLevel, true, Map("header" -> "// Expected Header Comment"), None, None)
    val configuration = ScalastyleConfiguration("sonar", true, List(checker1, checker2))
    val testeeSpy = spy(new ScalastyleRunner(mock[RulesProfile]))
    doReturn(configuration).when(testeeSpy).config
    val charset = StandardCharsets.UTF_8.name
  }


  "a scalastyle runner" should "report StyleError messages if there are rule violations" in new Fixture {
    val files = List(new File("src/test/resources/ScalaFile1.scala"))
    
    val messages = testeeSpy.run(charset, files)

    messages.length shouldEqual 5
    messages(0).toString shouldEqual "StartWork()"
    messages(1).toString shouldEqual "StartFile(/Users/emrehantuzun/Desktop/sonar-scalastyle/src/test/resources/ScalaFile1.scala)"
    messages(2).toString shouldEqual "StyleError key=header.matches args=List() lineNumber=Some(1) column=None customMessage=None"
    messages(3).toString shouldEqual "EndFile(/Users/emrehantuzun/Desktop/sonar-scalastyle/src/test/resources/ScalaFile1.scala)"
    messages(4).toString shouldEqual "EndWork()"
  }

  it should "not report StyleError messages if there are no violations" in new Fixture {
    val files = List(new File("src/test/resources/ScalaFile2.scala"))
    
    val messages = testeeSpy.run(charset, files)

    messages.length shouldEqual 4
    messages(0).toString shouldEqual "StartWork()"
    messages(1).toString shouldEqual "StartFile(/Users/emrehantuzun/Desktop/sonar-scalastyle/src/test/resources/ScalaFile2.scala)"
    messages(2).toString shouldEqual "EndFile(/Users/emrehantuzun/Desktop/sonar-scalastyle/src/test/resources/ScalaFile2.scala)"
    messages(3).toString shouldEqual "EndWork()"
  }

  it should "be able to run several violations" in new Fixture {
    val files = List(new File("src/test/resources/ScalaFile1.scala"), new File("src/test/resources/ScalaFile2.scala"))
    
    val messages = testeeSpy.run(charset, files)
    
    messages.length shouldEqual 7
    messages(0).toString shouldEqual "StartWork()"
    messages(1).toString shouldEqual "StartFile(/Users/emrehantuzun/Desktop/sonar-scalastyle/src/test/resources/ScalaFile1.scala)"
    messages(2).toString shouldEqual "StyleError key=header.matches args=List() lineNumber=Some(1) column=None customMessage=None"
    messages(3).toString shouldEqual "EndFile(/Users/emrehantuzun/Desktop/sonar-scalastyle/src/test/resources/ScalaFile1.scala)"
    messages(4).toString shouldEqual "StartFile(/Users/emrehantuzun/Desktop/sonar-scalastyle/src/test/resources/ScalaFile2.scala)"
    messages(5).toString shouldEqual "EndFile(/Users/emrehantuzun/Desktop/sonar-scalastyle/src/test/resources/ScalaFile2.scala)"
    messages(6).toString shouldEqual "EndWork()"
  }

  it should "convert rules to checker" in {
    val ruleToChecker = PrivateMethod[ConfigurationChecker]('ruleToChecker)
    val profile = RulesProfile.create(Constants.ProfileName, Constants.ScalaKey)
    val testee = new ScalastyleRunner(profile)
    val key = "multiple.string.literals"
    val className = "org.scalastyle.scalariform.MultipleStringLiteralsChecker"
    val rule = Rule.create
    rule.setRepositoryKey(Constants.RepositoryKey)
      .setKey(className)
      .setName(ScalastyleResources.shortDescription(key))
      .setDescription(ScalastyleResources.longDescription(key))
      .setConfigKey(key)
      .setSeverity(RulePriority.MAJOR)
    rule.createParameter
      .setKey("allowed")
      .setDescription("")
      .setType("integer")
      .setDefaultValue("1")
    rule.createParameter
      .setKey("ignoreRegex")
      .setDescription("")
      .setType("integer")
      .setDefaultValue("^&quot;&quot;$")
    val activeRule = profile.activateRule(rule, rule.getSeverity)
    activeRule.setParameter("allowed", "1")
    activeRule.setParameter("ignoreRegex", "^&quot;&quot;$")
    
    val checker = testee invokePrivate ruleToChecker(activeRule)
    val expectedChecker = ConfigurationChecker(className, ErrorLevel, true, Map("allowed" -> "1", "ignoreRegex" -> "^&quot;&quot;$"), None, None)

    checker shouldEqual expectedChecker
  }
}
