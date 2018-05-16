Gradle single-module project
===

This project consists of a single, root module.

It uses gradle-scoverage and sonarqube-gradle plugins and includes examples of how to configure and execute SonarQube analysis.

To run the analysis execute the following command setting the `sonar.host.url` property to point to your SonarQube instance with installed sonar-scala plugin.

```bash
gradle -Dsonar.host.url=http://localhost clean reportScoverage sonarqube
```

For more configuration options please refer to SonarQube Gradle Scanner [documentation](https://docs.sonarqube.org/display/SCAN/Analyzing+with+SonarQube+Scanner+for+Gradle).
