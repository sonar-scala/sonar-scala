/*
 * Sonar Scala Stlye Plugin
 * Copyright (C) 2011 - 2014 All contributors
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package com.ncredinburgh.sonar.scalastyle

import java.io.File
import java.nio.charset.StandardCharsets

import org.mockito.Matchers._
import org.mockito.Mockito._
import org.scalastyle._
import org.scalastyle.scalariform.ForBraceChecker
import org.scalatest._
import org.scalatest.mock.MockitoSugar
import org.sonar.api.batch.SensorContext
import org.sonar.api.component.ResourcePerspectives
import org.sonar.api.issue.Issue
import org.sonar.api.issue.Issuable
import org.sonar.api.resources.Project
import org.sonar.api.rules.{Rule, RuleFinder, RuleQuery}
import org.sonar.api.scan.filesystem.{FileQuery, ModuleFileSystem}
import org.sonar.core.issue.DefaultIssueBuilder

import scala.collection.JavaConversions._


/**
 * Created by keir on 13/06/14.
 */
class ScalaStyleSensorSpec extends FlatSpec with Matchers with MockitoSugar {

  trait Fixture {

    val fs = mock[ModuleFileSystem]
    val project = mock[Project]
    val runner = mock[ScalaStyleRunner]
    val perspective = mock[ResourcePerspectives]
    val issuable = mock[Issuable]
    val issueBuilder = new DefaultIssueBuilder().componentKey("foo");
    val rf = mock[RuleFinder]
    val aRule = Rule.create("repo", "key")

    val testee = new ScalaStyleSensor(perspective,runner,fs,rf)
    val context: SensorContext = mock[SensorContext]

    when(runner.run(anyString, anyListOf(classOf[File]))).thenReturn(List())

    when(fs.files(any[FileQuery])).thenReturn(List())
    when(fs.sourceCharset()).thenReturn(StandardCharsets.UTF_8)

    when(perspective.as(any(), any(classOf[org.sonar.api.resources.Resource]))).thenReturn(issuable)

    when(issuable.newIssueBuilder()).thenReturn(issueBuilder);

    when(rf.find(any[RuleQuery])).thenReturn(aRule)

  }


  "A Scalastyle Sensor" should "execute when a project is using Scala" in new Fixture {
    when(project.getLanguageKey()).thenReturn(Constants.SCALA_KEY)
    assert( testee.shouldExecuteOnProject(project) )
  }

  it should "not execute when a project is not using Scala" in new Fixture {
    when(project.getLanguageKey()).thenReturn("not scala")
    assert( !testee.shouldExecuteOnProject(project) )
  }

  it should "analyse all scala source files in project" in new Fixture {
    val files = List(new File("foo"), new File("bar"))
    when(fs.files(any[FileQuery])).thenReturn(files)

    testee.analyse(project, context);

    verify(runner).run(StandardCharsets.UTF_8.name(), files)
  }

  it should "report scalastyle errors as SonarQube issues" in new Fixture {
    val anError = new StyleError[FileSpec](new RealFileSpec("foo", None),classOf[ForBraceChecker], "foo", WarningLevel, List(), None)
    when(runner.run(anyString, anyListOf(classOf[File]))).thenReturn(List(anError))
    testee.analyse(project, context);

    verify(issuable).addIssue(any[Issue]);
  }

}
