organization := "com.buransky"

scalaVersion := "2.11.6"

lazy val root = project.in(file(".")).aggregate(module1, module2)

lazy val module1 = project.in(file("module1"))

lazy val module2 = project.in(file("module2"))
