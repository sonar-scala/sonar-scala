import com.mwz.sonar.scala.metadata.scalastyle._
import com.mwz.sonar.scala.metadata.scapegoat._
import de.heikoseeberger.sbtheader.License
import java.time.Year
import org.sonar.updatecenter.common.PluginManifest
import sbt._
import sbt.librarymanagement.Resolver
import sbtrelease.ReleasePlugin.autoImport.ReleaseTransformations._
import sbtrelease.Version.Bump.Minor

enablePlugins(AutomateHeaderPlugin)

name := "sonar-scala"
organization := "com.github.mwz"
homepage := Some(url("https://github.com/mwz/sonar-scala"))
description := "Enables analysis of Scala projects with SonarQube."

// Licence
organizationName := "All sonar-scala contributors"
startYear := Some(2018)
licenses := Seq("LGPL-3.0" -> url("https://www.gnu.org/licenses/lgpl-3.0.en.html"))
headerLicense := Some(
  License.LGPLv3(
    s"${startYear.value.get}-${Year.now}",
    organizationName.value
  )
)
excludeFilter.in(headerResources) := "*.scala"

// Compile options
scalaVersion := "2.13.1"
scalacOptions ++= Seq(
  "-language:reflectiveCalls",
  "-Ymacro-annotations",
  "-Yrangepos",
  "-Ywarn-unused"
)
scalacOptions -= "-Xfatal-warnings"
javacOptions := Seq("-Xlint:deprecation")
cancelable in Global := true
scalafmtOnCompile in ThisBuild :=
  sys.env
    .get("DISABLE_SCALAFMT")
    .forall(_.toLowerCase == "false")
scapegoatVersion in ThisBuild := "1.4.4"
scapegoatReports := Seq("xml")
coverageOutputXML := true
coverageOutputHTML := false
coverageOutputCobertura := false

// Add Scalastyle and Scapegoat inspections generators.
sourceGenerators in Compile ++= Seq(
  ScapegoatInspectionsGenerator.generatorTask.taskValue,
  ScalastyleInspectionsGenerator.generatorTask.taskValue
)

// Lib dependencies
val sonarVersion = "8.3.0.34182"
val circe = "0.13.0"
val http4s = "0.21.4"
libraryDependencies ++= List(
  "com.beachape"             %% "enumeratum"           % "1.6.1",
  "com.beachape"             %% "enumeratum-cats"      % "1.6.1",
  "com.beachape"             %% "enumeratum-circe"     % "1.6.1",
  "com.beautiful-scala"      %% "scalastyle"           % "1.4.0",
  "io.circe"                 %% "circe-core"           % circe,
  "io.circe"                 %% "circe-generic-extras" % "0.13.0",
  "io.circe"                 %% "circe-generic"        % circe,
  "org.http4s"               %% "http4s-blaze-client"  % http4s,
  "org.http4s"               %% "http4s-circe"         % http4s,
  "org.scala-lang.modules"   %% "scala-xml"            % "1.3.0",
  "org.scalariform"          %% "scalariform"          % "0.2.10",
  "org.slf4j"                 % "slf4j-api"            % "1.7.30"     % Provided,
  "org.sonarsource.sonarqube" % "sonar-plugin-api"     % sonarVersion % Provided,
  "org.typelevel"            %% "cats-core"            % "2.2.0",
  "org.typelevel"            %% "cats-effect"          % "2.1.3",
  "org.typelevel"            %% "mouse"                % "0.25",
  // TEST
  "com.beachape"               %% "enumeratum-scalacheck"     % "1.6.1"      % Test,
  "com.github.alexarchambault" %% "scalacheck-shapeless_1.14" % "1.2.5"      % Test,
  "org.http4s"                 %% "http4s-blaze-server"       % http4s       % Test,
  "org.http4s"                 %% "http4s-dsl"                % http4s       % Test,
  "org.mockito"                %% "mockito-scala"             % "1.14.4"     % Test,
  "org.scalacheck"             %% "scalacheck"                % "1.14.3"     % Test,
  "org.scalatest"              %% "scalatest"                 % "3.1.2"      % Test,
  "org.scalatestplus"          %% "mockito-1-10"              % "3.1.0.0"    % Test,
  "org.scalatestplus"          %% "scalacheck-1-14"           % "3.1.2.0"    % Test,
  "com.softwaremill.diffx"     %% "diffx-scalatest"           % "0.3.28"     % Test,
  "org.sonarsource.sonarqube"   % "sonar-plugin-api-impl"     % sonarVersion % Test
)

// Project resolvers
resolvers ++= List(
  Resolver.sonatypeRepo("snapshots"),
  Resolver.sonatypeRepo("releases"),
  "Artima Maven Repository" at "https://repo.artima.com/releases"
)

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

// scalafix
scalafixDependencies in ThisBuild += "com.nequissimus" %% "sort-imports" % "0.5.4"
addCommandAlias("fix", "all compile:scalafix test:scalafix; fixImports")
addCommandAlias("fixImports", "compile:scalafix SortImports; test:scalafix SortImports")
addCommandAlias("fixCheck", "compile:scalafix --check; test:scalafix --check; fixCheckImports")
addCommandAlias("fixCheckImports", "compile:scalafix --check SortImports; test:scalafix --check SortImports")

// plugins
addCompilerPlugin(scalafixSemanticdb)
addCompilerPlugin("com.olegpy"    %% "better-monadic-for" % "0.3.1")
addCompilerPlugin("org.typelevel" %% "kind-projector"     % "0.10.3")
