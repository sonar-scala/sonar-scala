scalaVersion := "2.12.8"
libraryDependencies ++= Seq(
  "org.sonarsource.update-center" % "sonar-update-center-common" % "1.21.0.561",
  // Scapegoat & scalastyle inspections generator dependencies
  "com.sksamuel.scapegoat" %% "scalac-scapegoat-plugin" % "1.3.8",
  "org.scalastyle"         %% "scalastyle"              % "1.0.0",
  "org.scalameta"          %% "scalameta"               % "4.1.6",
  "org.scalatest"          %% "scalatest"               % "3.0.7" % Test
)

// Adding a resolver to the Artima maven repo, so sbt can download the Artima SuperSafe sbt plugin
resolvers += "Artima Maven Repository" at "http://repo.artima.com/releases"

logBuffered in Test := false
testOptions in Test += Tests.Argument(TestFrameworks.ScalaTest, "-oDTF")
