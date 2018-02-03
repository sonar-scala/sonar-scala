import sbt._

name := "sonar-scala"
organization := "com.github.mwz"
homepage := Some(url("https://github.com/mwz/sonar-scala"))
licenses := Seq("LGPL-3.0" -> url("https://opensource.org/licenses/lgpl-3.0.html"))

scalaVersion := "2.12.4"
scalacOptions := Seq(
  "-unchecked",
  "-deprecation",
  "-encoding",
  "utf8",
  "-feature",
  "-language:reflectiveCalls"
)
javacOptions := Seq("-Xlint:deprecation")

val sonarVersion = "6.7.1"

libraryDependencies ++= List(
  "org.sonarsource.sonarqube" % "sonar-core" % sonarVersion,
  "org.sonarsource.sonarqube" % "sonar-plugin-api" % sonarVersion,
  "org.scalariform" %% "scalariform" % "0.2.6",
  "org.slf4j" % "slf4j-api" % "1.7.25",
  "commons-io" % "commons-io" % "2.6",
  "org.scalastyle" %% "scalastyle" % "1.0.0",
  "com.google.guava" % "guava" % "18.0",
  "org.scalatest" %% "scalatest" % "3.0.4" % Test,
  "org.mockito" % "mockito-core" % "2.12.0" % Test
)

parallelExecution in Test := false
logBuffered in Test := false
testOptions in Test += Tests.Argument(TestFrameworks.ScalaTest, "-oDF")
