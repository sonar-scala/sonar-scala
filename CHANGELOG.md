Changelog
===

## [6.2.0](https://github.com/mwz/sonar-scala/releases/tag/v6.2.0) - 28.04.2018
- Added support for multiple source locations configured by the `sonar.sources` property. As per SonarQube [documentation](https://docs.sonarqube.org/display/SONAR/Analysis+Parameters), the paths should be separated by a comma. Additionally, the paths can now be absolute, which allows sonar-scala to work with [SonarQube Maven plugin](https://docs.sonarqube.org/display/SCAN/Analyzing+with+SonarQube+Scanner+for+Maven). (#52 - @mwz)
- The Scovearge report path property `sonar.scoverage.reportPath` was deprecated and will be removed in the next major version. Please use `sonar.scala.scoverage.reportPath` instead. (#46 - @BalmungSan)

## [6.1.0](https://github.com/mwz/sonar-scala/releases/tag/v6.1.0) - 28.03.2018
- The Scoverage sensor was rewritten from scratch; introduced a new branch coverage metric. (#34 - @BalmungSan)
- Addressed coverage measure warnings reported by sonar-scanner during analysis. (#18 - @BalmungSan)
- Added a new property `sonar.scala.version` to specify Scala version. (#2 - @ElfoLiNk)

## [6.0.0](https://github.com/mwz/sonar-scala/releases/tag/v6.0.0) - 10.02.2018
- Support for SonarQube 6.7.1 LTS. (#5 - @mwz)
