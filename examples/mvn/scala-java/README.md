Maven single-module Scala and Java mixed project
===

This project consists of a single, root module with Scala and Java sources.

It uses [scoverage-maven-plugin](https://github.com/scoverage/scoverage-maven-plugin) and [sonar-maven-plugin](https://docs.sonarqube.org/display/SCAN/Analyzing+with+SonarQube+Scanner+for+Maven) and includes examples of how to configure and execute SonarQube analysis.

To run the analysis execute the following command setting the `sonar.host.url` property to point to your SonarQube instance with installed sonar-scala plugin.

```bash
mvn -Dsonar.host.url=http://localhost clean test scoverage:report jacoco:report sonar:sonar
```

For more configuration options please refer to SonarQube Maven Scanner [documentation](https://docs.sonarqube.org/display/SCAN/Analyzing+with+SonarQube+Scanner+for+Maven).
