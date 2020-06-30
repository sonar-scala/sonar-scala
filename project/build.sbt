scalaVersion := "2.12.10"
libraryDependencies ++= Seq(
  "org.sonarsource.update-center" % "sonar-update-center-common" % "1.25.0.830",
  // Scapegoat & scalastyle inspections generator dependencies
  "com.sksamuel.scapegoat" % s"scalac-scapegoat-plugin_${scalaVersion.value}" % "1.4.4",
  "com.beautiful-scala"   %% "scalastyle"                                     % "1.4.0",
  "org.scalameta"         %% "scalameta"                                      % "4.3.18",
  "org.scalatest"         %% "scalatest"                                      % "3.1.2" % Test
)

// Adding a resolver to the Artima maven repo, so sbt can download the Artima SuperSafe sbt plugin
resolvers ++= Seq(
  Resolver.sonatypeRepo("snapshots"),
  "Artima Maven Repository" at "https://repo.artima.com/releases"
)

logBuffered in Test := false
testOptions in Test += Tests.Argument(TestFrameworks.ScalaTest, "-oDTF")
