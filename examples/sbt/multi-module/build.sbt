lazy val baseSettings = Seq(
  scalaVersion := "2.12.8",
  libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.12" % "test",
  scapegoatVersion in ThisBuild := "1.3.9"
)

lazy val module1 = (project in file("module1"))
  .settings(baseSettings)
  .settings(name := "module2")

lazy val module2 = (project in file("module2"))
  .settings(baseSettings)
  .settings(name := "module1")

lazy val multiModule = (project in file("."))
  .aggregate(module1, module2)
  .settings(name := "sbt-multi-module")
  .settings(baseSettings)
