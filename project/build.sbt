libraryDependencies += "org.sonarsource.update-center" % "sonar-update-center-common" % "1.21.0.561"

// Adding a resolver to the Artima maven repo, so sbt can download the Artima SuperSafe sbt plugin
resolvers += "Artima Maven Repository" at "http://repo.artima.com/releases"
