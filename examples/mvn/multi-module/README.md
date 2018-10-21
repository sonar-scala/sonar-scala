Maven single-module project
===

This project consists two modules named module1 and module2.

It uses [scoverage-maven-plugin](https://github.com/scoverage/scoverage-maven-plugin) and [sonar-maven-plugin](https://docs.sonarqube.org/display/SCAN/Analyzing+with+SonarQube+Scanner+for+Maven) and includes examples of how to configure and execute SonarQube analysis.

To run the analysis execute the following command setting the `sonar.host.url` property to point to your SonarQube instance with installed sonar-scala plugin.

```bash
mvn -Dsonar.host.url=http://localhost scoverage:report sonar:sonar
```

For more configuration options please refer to SonarQube Maven Scanner [documentation](https://docs.sonarqube.org/display/SCAN/Analyzing+with+SonarQube+Scanner+for+Maven).
