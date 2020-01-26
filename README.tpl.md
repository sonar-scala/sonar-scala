<h1 align="left"> <img src="./img/sonar-scala.svg" height="80px"> sonar-scala</h1>

[![circleci-badge]][circleci] [![coverage-badge]][coverage]
[![bintray-badge]][bintray] [![bintray-badge-lts]][bintray-lts]
[![bintray-badge-lts-67]][bintray-lts-67]
[![bintray-stats-badge]][bintray-stats] [![gitter-badge]][gitter]

[bintray]: https://bintray.com/mwz/maven/sonar-scala/{{version}}/link
[bintray-badge]: https://img.shields.io/badge/Download-{{version}}-blue.svg
[bintray-badge-lts]:
  https://img.shields.io/badge/Download-{{ltsVersion}}_(for_SonarQube_7.9_LTS)-blue.svg
[bintray-badge-lts-67]:
  https://img.shields.io/badge/Download-{{lts67Version}}_(for_SonarQube_6.7_LTS)-blue.svg
[bintray-lts]: https://bintray.com/mwz/maven/sonar-scala/{{ltsVersion}}/link
[bintray-lts-67]:
  https://bintray.com/mwz/maven/sonar-scala/{{lts67Version}}/link
[bintray-stats]: https://bintray.com/mwz/maven/sonar-scala#statistics
[bintray-stats-badge]:
  https://img.shields.io/badge/dynamic/json.svg?uri=https://bintray.com/statistics/packageStatistics?pkgPath=/mwz/maven/sonar-scala&query=$.totalDownloads&label=Downloads+(last+30+days)&colorB=green
[circleci]: https://circleci.com/gh/mwz/sonar-scala
[circleci-badge]:
  https://img.shields.io/circleci/project/github/mwz/sonar-scala/master.svg?label=Build
[coverage]:
  https://sonar.sonar-scala.com/component_measures?id=sonar-scala&metric=coverage
[coverage-badge]:
  https://sonar.sonar-scala.com/api/badges/measure?key=sonar-scala&metric=coverage
[gitter]: https://gitter.im/sonar-scala/sonar-scala
[gitter-badge]:
  https://img.shields.io/gitter/room/sonar-scala/sonar-scala.svg?colorB=46BC99&label=Chat

**A free and open-source SonarQube plugin for static code analysis of Scala
projects.**

sonar-scala is an independent SonarQube plugin, driven by and developed with
:heart: by the
[community](https://github.com/mwz/sonar-scala/graphs/contributors).

Intended for [SonarQube 6.7 LTS](https://www.sonarqube.org/sonarqube-6-7-lts),
[SonarQube 7.9 LTS](https://www.sonarqube.org/sonarqube-7-9-lts),
[SonarQube 8.1](https://www.sonarqube.org/sonarqube-8-1) and Scala
2.11/2.12/2.13.

sonar-scala is developed in Scala. It uses the
[scalariform](https://github.com/scala-ide/scalariform) library to parse the
source code and integrates with [Scoverage](http://scoverage.org) (code
coverage), [Scalastyle](http://www.scalastyle.org) and
[Scapegoat](https://github.com/sksamuel/scapegoat) (static code analysis). It
also provides
[pull request decoration](https://sonar-scala.com/docs/setup/pr-decoration)
functionality, which can review pull requests on Github and make comments on new
issues directly in the pull request instead of reporting them to SonarQube.

_Running analysis from a Windows machine is currently not supported - please use
Linux or other Unix-like operating system._

## Documentation

See the project website [sonar-scala.com](https://sonar-scala.com) for
documentation.

## Development

To build the project from sources, run the `assembly` task in sbt shell and the
jar assembled with all of the dependencies required by this plugin should be
created in the `target/scala-2.13` directory.

To debug the plugin, export the following environment variable before running
`sonar-scanner` for your project:

```bash
export SONAR_SCANNER_OPTS="-agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=8000"
```

Once you've done that, `sonar-scanner` should display the following message
`Listening for transport dt_socket at address: 8000`. You can now attach your
IDE to the process on port `8000`, set breakpoints and debug the code.

## Credits

This project is a continuation of the sonar-scala plugin, which was initially
developed by [Sagacify](https://github.com/Sagacify/sonar-scala).

Many other projects have been used as an inspiration, here is a list of the main
ones:

- [RadoBuransky/sonar-scoverage-plugin](https://github.com/RadoBuransky/sonar-scoverage-plugin)
- [arthepsy/sonar-scala-extra](https://github.com/arthepsy/sonar-scala-extra)
- [1and1/sonar-scala](https://github.com/1and1/sonar-scala)
- [SonarSource/sonar-java](https://github.com/SonarSource/sonar-java)
- [SonarSource/sonar-examples](https://github.com/SonarSource/sonar-examples)
- [SonarSource/sonar-github](https://docs.sonarqube.org/display/PLUG/GitHub+Plugin)

## Changelog

For a full list of changes and releases, please see
[Changelog](https://sonar-scala.com/docs/changelog).

## License

The project is licensed under the GNU LGPL v3. See the [LICENSE](LICENSE) file
for more details.
