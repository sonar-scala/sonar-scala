scalaVersion := "2.12.10"
libraryDependencies ++= Seq(
  "org.sonarsource.update-center" % "sonar-update-center-common" % "1.24.0.735",
  // Scapegoat & scalastyle inspections generator dependencies
  "com.sksamuel.scapegoat" % s"scalac-scapegoat-plugin_${scalaVersion.value}" % "1.4.1",
  "com.beautiful-scala"    %% "scalastyle"                                    % "1.3.0+14-c70c73ba-SNAPSHOT",
  "org.scalameta"          %% "scalameta"                                     % "4.3.0",
  "org.scalatest"          %% "scalatest"                                     % "3.1.1" % Test
)

// Adding a resolver to the Artima maven repo, so sbt can download the Artima SuperSafe sbt plugin
resolvers ++= Seq(
  Resolver.sonatypeRepo("snapshots"),
  "Artima Maven Repository" at "https://repo.artima.com/releases"
)

logBuffered in Test := false
testOptions in Test += Tests.Argument(TestFrameworks.ScalaTest, "-oDTF")
