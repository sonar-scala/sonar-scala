organization := Common.organization

name := Common.baseName + "-module2"

version := Common.version

scalaVersion := "2.10.3"

libraryDependencies ++= Seq(
  "org.scalatest" %%  "scalatest" % "2.0" % "test"
)

ScoverageSbtPlugin.instrumentSettings