import org.sonar.updatecenter.common.PluginManifest
import sbt._
import sbtrelease.ReleasePlugin.autoImport.ReleaseTransformations._
import sbtrelease.Version.Bump.Minor

name := "sonar-scala"
organization := "com.github.mwz"
homepage := Some(url("https://github.com/mwz/sonar-scala"))
licenses := Seq("LGPL-3.0" -> url("https://opensource.org/licenses/lgpl-3.0.html"))
description := "Enables analysis of Scala projects with SonarQube."

// Compile options
scalaVersion := "2.12.7"
scalacOptions := Seq(
  "-unchecked",
  "-deprecation",
  "-encoding",
  "utf8",
  "-feature",
  "-language:reflectiveCalls"
)
javacOptions := Seq("-Xlint:deprecation")
cancelable in Global := true
scalafmtOnCompile in ThisBuild := true
scalafmtVersion in ThisBuild := "1.4.0"
scapegoatVersion in ThisBuild := "1.3.8"
scapegoatReports := Seq("xml")
scalacOptions in Scapegoat += "-P:scapegoat:overrideLevels:TraversableHead=Warning:OptionGet=Warning"
coverageOutputXML := true
coverageOutputHTML := false
coverageOutputCobertura := false

// Add Scalastyle and Scapegoat inspections generators.
sourceGenerators in Compile ++= Seq(
  ScapegoatInspectionsGenerator.generatorTask.taskValue,
  ScalastyleInspectionsGenerator.generatorTask.taskValue
)

// Lib dependencies
val sonarVersion = "7.3"
libraryDependencies ++= List(
  "org.sonarsource.sonarqube" % "sonar-plugin-api" % sonarVersion % Provided,
  "org.slf4j" % "slf4j-api" % "1.7.25" % Provided,
  "org.typelevel" %% "cats-core" % "1.4.0",
  "org.scalariform" %% "scalariform" % "0.2.6",
  "org.scalastyle" %% "scalastyle" % "1.0.0",
  "org.scala-lang.modules" %% "scala-xml" % "1.1.1",
  "org.scalatest" %% "scalatest" % "3.0.5" % Test,
  "org.mockito" % "mockito-core" % "2.23.0" % Test
)

// Adding a resolver to the Artima maven repo, so sbt can download the Artima SuperSafe Scala compiler
resolvers += "Artima Maven Repository" at "http://repo.artima.com/releases"

// Manifest attributes
packageOptions in (Compile, packageBin) += Package.ManifestAttributes(
  PluginManifest.KEY -> "scala",
  PluginManifest.NAME -> "Scala",
  PluginManifest.DESCRIPTION -> description.value,
  PluginManifest.HOMEPAGE -> "https://github.com/mwz/sonar-scala",
  PluginManifest.SOURCES_URL -> "https://github.com/mwz/sonar-scala",
  PluginManifest.ISSUE_TRACKER_URL -> "https://github.com/mwz/sonar-scala/issues",
  PluginManifest.ORGANIZATION -> "Michael Wizner",
  PluginManifest.ORGANIZATION_URL -> "https://github.com/mwz",
  PluginManifest.DEVELOPERS -> "Michael Wizner, Luis Miguel Mejía Suárez",
  PluginManifest.VERSION -> version.value,
  PluginManifest.DISPLAY_VERSION -> version.value,
  PluginManifest.SONAR_VERSION -> sonarVersion,
  PluginManifest.LICENSE -> "GNU LGPL 3",
  PluginManifest.SONARLINT_SUPPORTED -> "false",
  PluginManifest.MAIN_CLASS -> "com.mwz.sonar.scala.ScalaPlugin",
  PluginManifest.USE_CHILD_FIRST_CLASSLOADER -> "false"
)

// Assembly
test in assembly := {}
assemblyJarName in assembly := s"${name.value}-${version.value}.jar"
assemblyMergeStrategy in assembly := {
  case "log4j.properties" => MergeStrategy.first
  case "reference.conf"   => MergeStrategy.concat
  case "application.conf" => MergeStrategy.concat
  case PathList("META-INF", xs @ _*) =>
    xs match {
      case ("MANIFEST.MF" :: Nil) => MergeStrategy.discard
      case _                      => MergeStrategy.first
    }
  case _ => MergeStrategy.first
}
artifact in (Compile, assembly) := {
  val art = (artifact in (Compile, assembly)).value
  art.withClassifier(Some("assembly"))
}
addArtifact(artifact in (Compile, assembly), assembly)

// Bintray
bintrayRepository := "maven"
bintrayPackage := "sonar-scala"
bintrayVcsUrl := Some("git@github.com:mwz/sonar-scala.git")
bintrayReleaseOnPublish := true
publishMavenStyle := true
publishArtifact in Test := false
pomIncludeRepository := (_ => false)

// Release
releaseVersionBump := Minor
releaseTagComment := s"Releasing ${(version in ThisBuild).value}. [ci skip]"
releaseCommitMessage := s"Setting version to ${(version in ThisBuild).value}. [ci skip]"
releaseProcess := Seq[ReleaseStep](
  checkSnapshotDependencies,
  inquireVersions,
  setReleaseVersion,
  releaseStepTask(sonarScan),
  commitReleaseVersion,
  tagRelease,
  publishArtifacts,
  setNextVersion,
  commitNextVersion,
  pushChanges
)

// Test
parallelExecution in Test := false
logBuffered in Test := false

// ScalaTest reporter config:
// -o - standard output,
// D - show all durations,
// T - show reminder of failed and cancelled tests with short stack traces,
// F - show full stack traces.
testOptions in Test += Tests.Argument(TestFrameworks.ScalaTest, "-oDTF")
