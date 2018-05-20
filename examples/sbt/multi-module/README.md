SBT multi-module project
===

This project consists of a root module and two submodules named module1 and module2.

It uses [sbt-scoverage](https://github.com/scoverage/sbt-scoverage) and [sonar-scanner](https://docs.sonarqube.org/display/SCAN/Analyzing+with+SonarQube+Scanner) and includes examples of how to configure and execute SonarQube analysis.

To run the analysis execute the following command setting the `sonar.host.url` property to point to your SonarQube instance with installed sonar-scala plugin.

```bash
sbt clean coverage test coverageReport
sonar-scanner -Dsonar.host.url=http://localhost
```

For more configuration options please refer to SonarQube Scanner [documentation](https://docs.sonarqube.org/display/SCAN/Analyzing+with+SonarQube+Scanner+for+Gradle).

To trigger sonar-scanner analysis from within sbt, one can use the [sbt-sonar](https://github.com/mwz/sbt-sonar) plugin, see the examples [here](https://github.com/mwz/sbt-sonar/tree/master/src/sbt-test/sbt-sonar).
