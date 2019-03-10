Gradle multi-module project
===

This project consists of a root module and two submodules named module1 and module2.

t uses [gradle-scoverage](https://github.com/scoverage/gradle-scoverage), [scapegoat](https://github.com/sksamuel/scapegoat#gradle) and [sonarqube-gradle](https://plugins.gradle.org/plugin/org.sonarqube) plugins and includes examples of how to configure and execute SonarQube analysis.

To run the analysis execute the following command setting the `sonar.host.url` property to point to your SonarQube instance with installed sonar-scala plugin.

```bash
gradle --no-daemon -Dsonar.host.url=http://localhost clean test reportScoverage sonarqube
```

To see the result of an example analysis of this project, please go to [https://sonar.sonar-scala.com](https://sonar.sonar-scala.com/dashboard?id=example-gradle-multi-module).

For more configuration options please refer to SonarQube Gradle Scanner [documentation](https://docs.sonarqube.org/display/SCAN/Analyzing+with+SonarQube+Scanner+for+Gradle).
