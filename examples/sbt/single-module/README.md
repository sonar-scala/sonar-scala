SBT single-module project
===

This project consists of a single, root module.

It uses [sbt-scoverage](https://github.com/scoverage/sbt-scoverage), [sbt-scapegoat](https://github.com/sksamuel/sbt-scapegoat) and [sonar-scanner](https://docs.sonarqube.org/display/SCAN/Analyzing+with+SonarQube+Scanner) and includes examples of how to configure and execute SonarQube analysis.

To run the analysis execute the following command setting the `sonar.host.url` property to point to your SonarQube instance with installed sonar-scala plugin.

```bash
sbt clean coverage test coverageReport scapegoat
```

To see the result of an example analysis of this project, please go to [https://sonar.sonar-scala.com](https://sonar.sonar-scala.com/dashboard?id=example-sbt-single-module).

For more configuration options please refer to SonarQube Scanner [documentation](https://docs.sonarqube.org/display/SCAN/Analyzing+with+SonarQube+Scanner).

To trigger sonar-scanner analysis from within sbt, one can use the [sbt-sonar](https://github.com/mwz/sbt-sonar) plugin, see the examples [here](https://github.com/mwz/sbt-sonar/tree/master/src/sbt-test/sbt-sonar).
