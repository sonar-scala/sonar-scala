sonar-scala
===
[![circleci-badge][]][circleci]
[![coverage-badge][]][coverage]
[![bintray-badge][]][bintray]
[![bintray-badge-lts][]][bintray-lts]
[![bintray-stats-badge][]][bintray-stats]
[![gitter-badge][]][gitter]

[circleci-badge]: https://img.shields.io/circleci/project/github/mwz/sonar-scala/master.svg?label=Build
[circleci]: https://circleci.com/gh/mwz/sonar-scala
[coverage-badge]: https://sonar.sonar-scala.com/api/badges/measure?key=sonar-scala&metric=coverage
[coverage]: https://sonar.sonar-scala.com/component_measures?id=sonar-scala&metric=coverage
[bintray-badge]: https://img.shields.io/badge/Download-{{version}}-blue.svg
[bintray]: https://bintray.com/mwz/maven/sonar-scala/{{version}}/link
[bintray-badge-lts]: https://img.shields.io/badge/Download-{{ltsVersion}}_(LTS)-blue.svg
[bintray-lts]: https://bintray.com/mwz/maven/sonar-scala/{{ltsVersion}}/link
[bintray-stats-badge]: https://img.shields.io/badge/dynamic/json.svg?uri=https://bintray.com/statistics/packageStatistics?pkgPath=/mwz/maven/sonar-scala&query=$.totalDownloads&label=Downloads+(last+30+days)&colorB=green
[bintray-stats]: https://bintray.com/mwz/maven/sonar-scala#statistics
[gitter-badge]: https://img.shields.io/gitter/room/sonar-scala/sonar-scala.svg?colorB=46BC99&label=Chat
[gitter]: https://gitter.im/sonar-scala/sonar-scala


**SonarQube plugin for static code analysis of Scala projects.**

Intended for [SonarQube 6.7 LTS](https://www.sonarqube.org/sonarqube-6-7-lts), [SonarQube 7.4](https://www.sonarqube.org/sonarqube-7-4) and Scala 2.11/2.12.
Running analysis from a Windows machine is currently not supported, please use Linux or other Unix-like operating systems.

This plugin is not an evolution from the legacy sonar-scala-plugin of which versions can be found laying around such as [1and1/sonar-scala](https://github.com/1and1/sonar-scala).
The previous plugin used the Scala compiler to create its metrics which had the disadvantage of requiring a specific plugin per each major version of Scala.
Instead, this plugin uses the [scala-ide/scalariform](https://github.com/scala-ide/scalariform) library to parse the source code in a version independent way.


# Supported Metrics
This plugin currently supports the following SonarQube [metrics](https://docs.sonarqube.org/display/SONAR/Metric+Definitions):

**Size**
- Number of classes (and objects) - [`classes`](https://docs.sonarqube.org/display/SONAR/Metric+Definitions#MetricDefinitions-Size)
- Number of lines containing either comments or commented-out code - [`comment_lines`](https://docs.sonarqube.org/display/SONAR/Metric+Definitions#MetricDefinitions-Size)
- Number of files - [`files`](https://docs.sonarqube.org/display/SONAR/Metric+Definitions#MetricDefinitions-Size)
- Lines of code - [`ncloc`](https://docs.sonarqube.org/display/SONAR/Metric+Definitions#MetricDefinitions-Size)
- Number of functions - [`functions`](https://docs.sonarqube.org/display/SONAR/Metric+Definitions#MetricDefinitions-Size)

**Tests**
- Number of lines of code which could be covered by unit tests - [`lines_to_cover`](https://docs.sonarqube.org/display/SONAR/Metric+Definitions#MetricDefinitions-Tests)
- Number of lines of code which are not covered by unit tests - [`uncovered_lines`](https://docs.sonarqube.org/display/SONAR/Metric+Definitions#MetricDefinitions-Tests)
- Percentage of line coverage - [`line_coverage`](https://docs.sonarqube.org/display/SONAR/Metric+Definitions#MetricDefinitions-Tests)
- Number of unit tests - [`tests`](https://docs.sonarqube.org/display/SONAR/Metric+Definitions#MetricDefinitions-Tests)
- Number of skipped unit tests - [`skipped_tests`](https://docs.sonarqube.org/display/SONAR/Metric+Definitions#MetricDefinitions-Tests)
- Number of unit test errors - [`test_errors`](https://docs.sonarqube.org/display/SONAR/Metric+Definitions#MetricDefinitions-Tests)
- Number of unit test failures - [`test_failures`](https://docs.sonarqube.org/display/SONAR/Metric+Definitions#MetricDefinitions-Tests)
- Execution duration of unit tests - [`test_execution_time`](https://docs.sonarqube.org/display/SONAR/Metric+Definitions#MetricDefinitions-Tests)

In addition to the above, the plugin reports the following custom metrics, which are extracted from the Scoverage report:
 - Number of all statements - [`total_statements`](https://github.com/mwz/sonar-scala/blob/3973e6a8b3857c06de7b6d996702eeb3e543e5e0/src/main/scala/com/mwz/sonar/scala/scoverage/ScoverageMetrics.scala#L64)
 - Number of statements covered by tests - [`covered_statements`](https://github.com/mwz/sonar-scala/blob/3973e6a8b3857c06de7b6d996702eeb3e543e5e0/src/main/scala/com/mwz/sonar/scala/scoverage/ScoverageMetrics.scala#L74)
 - Percentage of statement coverage - [`scoverage`](https://github.com/mwz/sonar-scala/blob/3973e6a8b3857c06de7b6d996702eeb3e543e5e0/src/main/scala/com/mwz/sonar/scala/scoverage/ScoverageMetrics.scala#L84)
 - Percentage of branch coverage - [`branch_scoverage`](https://github.com/mwz/sonar-scala/blob/3973e6a8b3857c06de7b6d996702eeb3e543e5e0/src/main/scala/com/mwz/sonar/scala/scoverage/ScoverageMetrics.scala#L96)


# Quality Rules and Profiles
This plugin integrates 69 quality checks from [Scalastyle](http://www.scalastyle.org/rules-1.0.0.html) and 117 from [Scapegoat](https://github.com/sksamuel/scapegoat/tree/v1.3.8). 40 of the Scalastyle rules are defined without parameters, and the remaining 29 are rule templates, which allow you to set up custom rules by specifying the parameters yourself. Most of those are already activated for you with default values recommended by Scalastyle. Scapegoat rules don't contain any templates.

Sonar-scala creates two rules repositories and four built-in quality profiles: `Scalastyle`, `Scapegoat` and a combination of those two `Scalastyle+Scapegoat` and `Recommended by sonar-scala`. The rules in the quality profiles are almost all activated, except for those which are broken in the upstream projects and had to be blacklisted.

To make changes to any of the rules, you can clone the quality profile of your choice and then you should be able to activate or deactivate rules, change rule severity and create more custom rules from the existing templates. Alternatively, you can create your quality profile and activate rules from any of the repositories using SonarQube rule filtering and bulk-change features available on the Rules page.

For more information about Scalastyle and Scapegoat rules, please consult the upstream documentation:
- Scalastyle - http://www.scalastyle.org/rules-1.0.0.html
- Scapegoat - https://github.com/sksamuel/scapegoat/tree/v1.3.8


# Set-up
Download the latest [release](https://github.com/mwz/sonar-scala/releases) jar into your SonarQube plugins folder `/opt/sonarqube/extensions/plugins` and restart SonarQube either manually or using the update center.

For an out-of-the-box setup, you can use my docker-compose recipe or a docker image with SonarQube which contains bundled sonar-scala plugin. Please see [mwz/sonar-scala-docker](https://github.com/mwz/sonar-scala-docker) for more details.

For automating the analysis of your Scala projects, check out my sbt plugin [mwz/sbt-sonar](https://github.com/mwz/sbt-sonar).

Also, see the [examples](https://github.com/mwz/sonar-scala/tree/master/examples) directory, which includes sample projects for sbt, Gradle and Maven along with basic instructions on how to execute SonarQube analysis for each of those projects.


# Sonar-scanner properties
The plugin exposes the following properties which can be passed to sonar-scanner when running an analysis:
- **sonar.sources** - Scala source directory relative to the root of your project (usually `src/main/scala`)
- **sonar.tests** (optional) - Scala tests directory relative to the root of your project (usually `src/test/scala`)
- **sonar.junit.reportPaths** (optional) - relative path to the directory containing unit test execution reports in Surefire XML format (defaults to `target/test-reports`); those reports get generated automatically by sbt and Gradle (Gradle outputs those in `build/test-results/test`), for Maven see the [examples](examples/mvn) on how to configure the [Surefire](https://maven.apache.org/surefire/maven-surefire-plugin/test-mojo.html) plugin
- **sonar.scala.version** (optional) - defines the version of Scala used in your project (requires the `{major}.{minor}` versions and the patch version is ignored, defaults to `2.12`)
- **sonar.scala.scoverage.reportPath** (optional) - relative path to the scoverage report (defaults to `target/scala-${sonar.scala.version}/scoverage-report/scoverage.xml`)
- **sonar.scala.scalastyle.disable** (optional) - disables the Scalastyle sensor from being executed on your sources (defaults to `false`)
- **sonar.scala.scapegoat.reportPath** (optional) - relative path to the scapegoat report (defaults to `target/scala-${sonar.scala.version}/scapegoat-report/scapegoat.xml`)
- **sonar.scala.scapegoat.disable** (optional) - disables the Scapegoat sensor from being executed on your sources (defaults to `false`)

See an example usage:
```bash
sonar-scanner -Dsonar.projectName=test \
              -Dsonar.projectKey=test \
              -Dsonar.sources=src/main/scala \
              -Dsonar.tests=src/test/scala \
              -Dsonar.sourceEncoding=UTF-8 \
              -Dsonar.scala.version=2.12 \
              -Dsonar.scoverage.reportPath=target/scala-2.12/scoverage-report/scoverage.xml \
              -Dsonar.scapegoat.reportPath=target/scala-2.12/scapegoat-report/scapegoat.xml
```
or simply `sbt sonarScan` if you use the [sbt-sonar](https://github.com/mwz/sbt-sonar) sbt plugin.

# Compatibility with SonarQube
SonarQube | sonar-scala
----------|----------
7.4       | [7.3.1](https://github.com/mwz/sonar-scala/releases/tag/v7.3.1)
7.3       | [7.0](https://github.com/mwz/sonar-scala/releases/tag/v7.0.0)
6.7 LTS   | 6.x (*latest [{{ltsVersion}}](https://github.com/mwz/sonar-scala/releases/tag/v{{ltsVersion}})*)


# Development
To build the project from sources, run the `assembly` task in sbt shell and the jar assembled with all of the dependencies required by this plugin should be created in the `target/scala-2.12` directory. 

To debug the plugin, export the following environment variable before running `sonar-scanner` for your project:
```bash
export SONAR_SCANNER_OPTS="-agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=8000"
```
Once you've done that, `sonar-scanner` should display the following message `Listening for transport dt_socket at address: 8000`. You can now attach your IDE to the process on port `8000`, set breakpoints and debug the code.


# Credits
This project is a continuation of the sonar-scala plugin, which was initially developed by [Sagacify](https://github.com/Sagacify/sonar-scala).

Many other projects have been used as an inspiration, here is a list of the main ones:

- [RadoBuransky/sonar-scoverage-plugin](https://github.com/RadoBuransky/sonar-scoverage-plugin)
- [arthepsy/sonar-scala-extra](https://github.com/arthepsy/sonar-scala-extra)
- [1and1/sonar-scala](https://github.com/1and1/sonar-scala)
- [SonarSource/sonar-java](https://github.com/SonarSource/sonar-java)
- [SonarSource/sonar-examples](https://github.com/SonarSource/sonar-examples)


# Changelog
For a list of changes and releases, please see [CHANGELOG](CHANGELOG.md).


# License
The project is licensed under the GNU LGPL v3. See the [LICENSE](LICENSE) file for more details.
