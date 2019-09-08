scalaVersion := "2.12.8"
libraryDependencies ++= Seq(
  "org.sonarsource.update-center" % "sonar-update-center-common" % "1.22.0.644",
  // Scapegoat & scalastyle inspections generator dependencies
  "com.sksamuel.scapegoat" %% "scalac-scapegoat-plugin" % "1.3.10",
  "org.scalastyle"         %% "scalastyle"              % "1.0.0",
  "org.scalameta"          %% "scalameta"               % "4.2.3",
  "org.scalatest"          %% "scalatest"               % "3.0.8" % Test
)

// Adding a resolver to the Artima maven repo, so sbt can download the Artima SuperSafe sbt plugin
resolvers += "Artima Maven Repository" at "https://repo.artima.com/releases"

logBuffered in Test := false
testOptions in Test += Tests.Argument(TestFrameworks.ScalaTest, "-oDTF")
