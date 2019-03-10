Maven multi-module project
===

This project consists of two modules named module1 and module2.

It uses [scoverage-maven-plugin](https://github.com/scoverage/scoverage-maven-plugin), [scapegoat](https://github.com/sksamuel/scapegoat#maven) and [sonar-maven-plugin](https://github.com/SonarSource/sonar-scanner-maven) and includes examples of how to configure and execute SonarQube analysis.

To run the analysis execute the following command setting the `sonar.host.url` property to point to your SonarQube instance with installed sonar-scala plugin.

```bash
mvn -Dsonar.host.url=http://localhost clean test scoverage:report scala:compile sonar:sonar
```

**Notes:**

+ Sometimes, the **Scapegoat** _report_ file (`scapegoat.xml`) gets cleaned after the execution of the tests.  
  Running the `scala:compile` target will trigger the scapegoat analysis once again. Thus, refreshing the _report_ file.  
  For that reason, it is recommended to alwasy excecute it just before running the sonar scanner.

+ Even if, on most projects the `${project.build.directory}` property equals `./target`.  
  It is recommended to use it instead, just to ensure both **Scapegoat** and **sonar-scala** use the same folder for writing / reading the _report_ file.  
  For multi-module projects _(like this one)_, the use of the `${project.build.directory}` property is **MANDATORY!**.
  Since that way, you ensure that each module writes and reads they own _report_ file.

To see the result of an example analysis of this project, please go to [https://sonar.sonar-scala.com](https://sonar.sonar-scala.com/dashboard?id=example-mvn-multi-module).

For more configuration options please refer to SonarQube Maven Scanner [documentation](https://docs.sonarqube.org/display/SCAN/Analyzing+with+SonarQube+Scanner+for+Maven).
