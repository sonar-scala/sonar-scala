# sonar-scala
[![CircleCI](https://img.shields.io/circleci/project/github/mwz/sonar-scala/master.svg)](https://circleci.com/gh/mwz/sonar-scala)
[![Download](https://api.bintray.com/packages/mwz/maven/sonar-scala/images/download.svg)](https://bintray.com/mwz/maven/sonar-scala/_latestVersion)

SonarQube plugin for static code analysis of Scala projects.


# Set-up
Intended for [SonarQube 6.7.1 LTS](https://www.sonarqube.org/downloads) and Scala 2.11/2.12.

Download the latest [release](https://github.com/mwz/sbt-sonar/releases) into your sonar extensions/downloads folder and restart SonarQube either using the update center or manually.


# Quality Rules
The rules in Scalastyle are almost all deactivated. They must be activated and either make Scala rules inherit Scalastyle rules or change the project's rules.

For more information about either Scalastyle rules or Scoverage results please consult their upstream documentation first.

- [NCR-CoDE/sonar-scalastyle](https://github.com/NCR-CoDE/sonar-scalastyle)
- [RadoBuransky/sonar-scoverage-plugin](https://github.com/RadoBuransky/sonar-scoverage-plugin)


# Info
This plugin is not an evolution from the legacy sonar-scala-plugin of which versions can be found laying around such as [1and1/sonar-scala](https://github.com/1and1/sonar-scala).
The previous plugin used the scala compiler to create its metrics which had the disadvantage of requiring a specific plugin per scala version.
Instead, this plugin uses the [scala-ide/scalariform](https://github.com/scala-ide/scalariform) library to parse the source code in a version independent way.


# Build from source
```sbtshell
sbt assembly
```


# Credits
The project has been originally developed by [Sagacify](https://github.com/Sagacify/sonar-scala) and integrates code from two other SonarQube plugins [Sonar Scalastyle Plugin](https://github.com/NCR-CoDE/sonar-scalastyle) and [Sonar Scoverage Plugin](https://github.com/RadoBuransky/sonar-scoverage-plugin).

Many other projects have been used as an inspiration, here is a list of the main ones:

- [1and1/sonar-scala](https://github.com/1and1/sonar-scala)
- [SonarSource/sonar-java](https://github.com/SonarSource/sonar-java)
- [SonarSource/sonar-examples](https://github.com/SonarSource/sonar-examples)


# Integration
For ease of use, Sonar Scala directly integrates the latest code from the [Sonar Scalastyle Plugin](https://github.com/NCR-CoDE/sonar-scalastyle) and [Sonar Scoverage Plugin](https://github.com/RadoBuransky/sonar-scoverage-plugin). This is possible as all three projects are released under the GNU LGPL v3 license. Nevertheless, all merged files are to keep their original copyright, classpath, and commit history. Any further change upstream should be incorporated using cherry-picks or merges.


# License
The project is licensed under the GNU LGPL v3. See the LICENSE file for more details.
