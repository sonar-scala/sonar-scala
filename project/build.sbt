scalaVersion := "2.12.6"
libraryDependencies ++= Seq(
  "org.sonarsource.update-center" % "sonar-update-center-common" % "1.21.0.561",
  // Scapegoat & scalastyle inspections generator dependencies
  "com.sksamuel.scapegoat" %% "scalac-scapegoat-plugin" % "1.3.7",
  "io.github.lukehutch" % "fast-classpath-scanner" % "3.1.13",
  "org.scalastyle" %% "scalastyle" % "1.0.0",
  "org.scalameta" %% "scalameta" % "4.0.0-M8",
  "org.scalatest" %% "scalatest" % "3.0.5" % Test
)

// Adding a resolver to the Artima maven repo, so sbt can download the Artima SuperSafe sbt plugin
resolvers += "Artima Maven Repository" at "http://repo.artima.com/releases"
