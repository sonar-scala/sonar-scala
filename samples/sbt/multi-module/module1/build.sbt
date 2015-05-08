organization := Common.organization

name := Common.baseName + "-module1"

version := Common.version

scalaVersion := "2.11.6"

libraryDependencies ++= Seq(
  "org.scalatest" %%  "scalatest" % "2.2.4" % "test"
)