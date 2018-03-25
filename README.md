sonar-scala
===
[![circleci-badge][]][circleci]
[![coverage-badge][]][coverage]
[![bintray-badge][]][bintray]
[![bintray-stats-badge][]][bintray-stats]
[![gitter-badge][]][gitter]
[![insightio-badge][]][insightio]

[circleci-badge]: https://img.shields.io/circleci/project/github/mwz/sonar-scala/master.svg?label=Build
[circleci]: https://circleci.com/gh/mwz/sonar-scala
[coverage-badge]: https://sonar.sonar-scala.com/api/badges/measure?key=sonar-scala&metric=coverage
[coverage]: https://sonar.sonar-scala.com/component_measures?id=sonar-scala&metric=coverage
[bintray-badge]: https://api.bintray.com/packages/mwz/maven/sonar-scala/images/download.svg
[bintray]: https://bintray.com/mwz/maven/sonar-scala/_latestVersion
[bintray-stats-badge]: https://img.shields.io/badge/dynamic/json.svg?uri=https://bintray.com/statistics/packageStatistics?pkgPath=/mwz/maven/sonar-scala&query=$.totalDownloads&label=Downloads+(last+30+days)&colorB=green
[bintray-stats]: https://bintray.com/mwz/maven/sonar-scala#statistics
[gitter-badge]: https://img.shields.io/gitter/room/sonar-scala/sonar-scala.svg?colorB=46BC99&label=Chat
[gitter]: https://gitter.im/sonar-scala/sonar-scala
[insightio-badge]: https://img.shields.io/badge/Insight.io-Ready-brightgreen.svg
[insightio]: https://insight.io/github.com/mwz/sonar-scala


**SonarQube plugin for static code analysis of Scala projects.**

Intended for [SonarQube 6.7+ LTS](https://www.sonarqube.org/downloads) and Scala 2.11/2.12.

This plugin is not an evolution from the legacy sonar-scala-plugin of which versions can be found laying around such as [1and1/sonar-scala](https://github.com/1and1/sonar-scala).
The previous plugin used the scala compiler to create its metrics which had the disadvantage of requiring a specific plugin per each major version of Scala.
Instead, this plugin uses the [scala-ide/scalariform](https://github.com/scala-ide/scalariform) library to parse the source code in a version independent way.


# Supported Metrics
This plugin currently supports the following SonarQube [metrics](https://docs.sonarqube.org/display/SONAR/Metric+Definitions):
- Number of classes (and objects) - [`classes`](https://docs.sonarqube.org/display/SONAR/Metric+Definitions#MetricDefinitions-Size)
- Number of lines containing either comments or commented-out code - [`comment_lines`](https://docs.sonarqube.org/display/SONAR/Metric+Definitions#MetricDefinitions-Size)
- Number of files - [`files`](https://docs.sonarqube.org/display/SONAR/Metric+Definitions#MetricDefinitions-Size)
- Lines of code - [`ncloc`](https://docs.sonarqube.org/display/SONAR/Metric+Definitions#MetricDefinitions-Size)
- Number of functions - [`functions`](https://docs.sonarqube.org/display/SONAR/Metric+Definitions#MetricDefinitions-Size)
- Number of lines of code which could be covered by unit tests - [`lines_to_cover`](https://docs.sonarqube.org/display/SONAR/Metric+Definitions#MetricDefinitions-Tests)
- 	Number of lines of code which are not covered by unit tests - [`uncovered_lines`](https://docs.sonarqube.org/display/SONAR/Metric+Definitions#MetricDefinitions-Tests)
- Percentage of line coverage - [`line_coverage`](https://docs.sonarqube.org/display/SONAR/Metric+Definitions#MetricDefinitions-Tests)

In addition to the above, the plugin reports the following custom coverage metrics:
 - Number of all statements - [`total_statements`](https://github.com/mwz/sonar-scala/blob/3973e6a8b3857c06de7b6d996702eeb3e543e5e0/src/main/scala/com/mwz/sonar/scala/scoverage/ScoverageMetrics.scala#L64)
 - Number of statements covered by tests - [`covered_statements`](https://github.com/mwz/sonar-scala/blob/3973e6a8b3857c06de7b6d996702eeb3e543e5e0/src/main/scala/com/mwz/sonar/scala/scoverage/ScoverageMetrics.scala#L74)
 - Percentage of statement coverage - [`scoverage`](https://github.com/mwz/sonar-scala/blob/3973e6a8b3857c06de7b6d996702eeb3e543e5e0/src/main/scala/com/mwz/sonar/scala/scoverage/ScoverageMetrics.scala#L84)
 - Percentage of branch coverage - [`branch_scoverage`](https://github.com/mwz/sonar-scala/blob/3973e6a8b3857c06de7b6d996702eeb3e543e5e0/src/main/scala/com/mwz/sonar/scala/scoverage/ScoverageMetrics.scala#L96)


# Quality Rules and Profiles
This plugin integrates 75 quality checks from [Scalastyle](http://www.scalastyle.org/rules-1.0.0.html). 46 of them are quality rules without parameters which work out of the box and the remaining 29 are rule templates that allow you to set up custom rules which can be configured by various parameters.

The rules in the Scalastyle quality profile, created by this plugin, are almost all deactivated. In order to use all of the rules, you should clone the quality profile and you should be able to activate more rules, change rule severity and create more custom rules from the existing templates.

For more information about either Scalastyle rules or Scoverage results please consult their upstream documentation first.

- [NCR-CoDE/sonar-scalastyle](https://github.com/NCR-CoDE/sonar-scalastyle)
- [RadoBuransky/sonar-scoverage-plugin](https://github.com/RadoBuransky/sonar-scoverage-plugin)


# Set-up
Download the latest [release](https://github.com/mwz/sbt-sonar/releases) jar into your SonarQube plugins folder `/opt/sonarqube/extensions/plugins` and restart SonarQube either using the update center or manually.

For an out-of-the-box setup, you can use my docker-compose recipe or a docker image with SonarQube LTS which contains bundled sonar-scala and [arthepsy/sonar-scala-extra](https://github.com/arthepsy/sonar-scala-extra) (Scapegoat) plugins. Please see [mwz/sonar-scala-docker](https://github.com/mwz/sonar-scala-docker) for more details.

For automating the analysis of your Scala projects, check out my sbt plugin [mwz/sbt-sonar](https://github.com/mwz/sbt-sonar).

# Sonar-scanner properties
The plugin exposes the following properties which can be passed to sonar-scanner when running an analysis:
- *sonar.sources* (optional) - Scala source directory relative to the root of your project (defaults to `src/main/scala`)
- *sonar.scala.version* (optional) - defines the version of Scala used in your project (defaults to `2.11.0`)
- *sonar.scoverage.reportPath* (optional) - relative path to the scoverage report (defaults to `target/scala-${sonar.scala.version}/scoverage-report/scoverage.xml`).

See an example usage:
```bash
sonar-scanner -Dsonar.projectName=test \
              -Dsonar.projectKey=test \
              -Dsonar.sources=src/main/scala \
              -Dsonar.sourceEncoding=UTF-8 \
              -Dsonar.scala.version=2.12.5 \
              -Dsonar.scoverage.reportPath=target/scala-2.12/scoverage-report/scoverage.xml
```

# Development
To build the project from sources, run the `assembly` task in sbt shell and the jar assembled with all of the dependencies required by this plugin should be created in the `target/scala-2.12` directory. 


# Credits
The project has been originally developed by [Sagacify](https://github.com/Sagacify/sonar-scala) and integrates code from two other SonarQube plugins [Sonar Scalastyle Plugin](https://github.com/NCR-CoDE/sonar-scalastyle) and [Sonar Scoverage Plugin](https://github.com/RadoBuransky/sonar-scoverage-plugin).

Many other projects have been used as an inspiration, here is a list of the main ones:

- [1and1/sonar-scala](https://github.com/1and1/sonar-scala)
- [SonarSource/sonar-java](https://github.com/SonarSource/sonar-java)
- [SonarSource/sonar-examples](https://github.com/SonarSource/sonar-examples)


# Integration
For ease of use, Sonar Scala directly integrates the latest code from the [Sonar Scalastyle Plugin](https://github.com/NCR-CoDE/sonar-scalastyle) and [Sonar Scoverage Plugin](https://github.com/RadoBuransky/sonar-scoverage-plugin). This is possible as all three projects are released under the GNU LGPL v3 license. Nevertheless, all merged files are to keep their original copyright, classpath, and commit history. Any further change upstream should be incorporated using cherry-picks or merges.


# Changelog
For a list of changes and releases, please see [CHANGELOG](CHANGELOG.md).


# License
The project is licensed under the GNU LGPL v3. See the [LICENSE](LICENSE) file for more details.
