# sonar-scala
Sonarqube plugin for scala analysis

# Set-up
Intended for sonarqube 5.4

Download the latest relase into your sonar extentions/downloads folder.
Restart sonarqube either using the update center or manually.

# Build from source
```mvn package```

# Test
```
mvn test
sonar-runner -D sonar.projectKey=Sagacify:sonar-scala
```

# Info
This plugin is not an evolution from the legacy sonar-scala-plugin of which versions can be found laying around such as [1and1/sonar-scala](https://github.com/1and1/sonar-scala).
The previous plugin used the scala compiler to create its metrics which had the disadvantage of requiring a specific plugin per scala version.
Instead, we are using the [scala-ide/scalariform](https://github.com/scala-ide/scalariform) library to parse the source code in a version independent way.

# TODO
* Add property to sepcify scala version (currently defaults to 2.11.8)
* Integrate scalastyle
* Integrate coverage metrics
* Integrate scalawarts
...

# Credits
Many existing projects have been used as inspiration.
Here is a list of the main ones.

[1and1/sonar-scala](https://github.com/1and1/sonar-scala)

[SonarSource/sonar-java](https://github.com/SonarSource/sonar-java)

[SonarSource/sonar-examples](https://github.com/SonarSource/sonar-examples)

[NCR-CoDE/sonar-scalastyle](https://github.com/NCR-CoDE/sonar-scalastyle)

# Integration
Sonar-scala integrates the latest code from the [Sonar Scalastyle Plugin](https://github.com/NCR-CoDE/sonar-scalastyle) directly. All thise files must keep their original license. Also their history was pulled along with them. Any further change upstream should be incorporated using cherry-picks or merges.
