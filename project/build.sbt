scalaVersion := "2.12.5"
libraryDependencies += "org.sonarsource.update-center" % "sonar-update-center-common" % "1.21.0.561"

// Adding a resolver to the Artima maven repo, so sbt can download the Artima SuperSafe sbt plugin
resolvers += "Artima Maven Repository" at "http://repo.artima.com/releases"

// Adding the scapegoat inspections generator task dependencies
libraryDependencies ++= Seq(
  "com.sksamuel.scapegoat" %% "scalac-scapegoat-plugin" % "1.3.4",
  "io.github.lukehutch" % "fast-classpath-scanner" % "2.18.2"
)
