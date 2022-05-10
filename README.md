<h1 align="left"> <img src="./img/sonar-scala.svg" height="80px"> sonar-scala</h1>

![](https://img.shields.io/github/workflow/status/sonar-scala/sonar-scala/Release/master)
[![sonatype-badge]][sonatype] [![bintray-badge-lts]][bintray-lts]
[![bintray-badge-lts-79]][bintray-lts-79]
[![bintray-badge-lts-67]][bintray-lts-67] [![gitter-badge]][gitter]

[sonatype]:
  https://s01.oss.sonatype.org/content/repositories/releases/com/sonar-scala/sonar-scala_2.13/9.0.0/sonar-scala_2.13-9.0.0-assembly.jar
[sonatype-badge]: https://img.shields.io/badge/Download-9.0.0-blue.svg
[bintray-badge-lts]:
  https://img.shields.io/badge/Download-8.9.0_(for_SonarQube_8.9_LTS)-blue.svg
[bintray-badge-lts-79]:
  https://img.shields.io/badge/Download-7.9.0_(for_SonarQube_7.9_LTS)-blue.svg
[bintray-badge-lts-67]:
  https://img.shields.io/badge/Download-6.8.0_(for_SonarQube_6.7_LTS)-blue.svg
[bintray-lts]: https://bintray.com/mwz/maven/sonar-scala/8.9.0/link
[bintray-lts-79]:
  https://bintray.com/mwz/maven/sonar-scala/7.9.0/link
[bintray-lts-67]:
  https://bintray.com/mwz/maven/sonar-scala/6.8.0/link
[gitter]: https://gitter.im/sonar-scala/sonar-scala
[gitter-badge]:
  https://img.shields.io/gitter/room/sonar-scala/sonar-scala.svg?colorB=46BC99&label=Chat

**A free and open-source SonarQube plugin for static code analysis of Scala
projects.**

sonar-scala is an independent SonarQube plugin, driven by and developed with
:heart: by the
[community](https://github.com/mwz/sonar-scala/graphs/contributors).

Intended for [SonarQube 9.4](https://www.sonarqube.org/sonarqube-9-4),
[SonarQube 8.9 LTS](https://www.sonarqube.org/sonarqube-8-7),
[SonarQube 7.9 LTS](https://www.sonarqube.org/sonarqube-7-9-lts),
[SonarQube 6.7 LTS](https://www.sonarqube.org/sonarqube-6-7-lts) and Scala
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
