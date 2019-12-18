#!/usr/bin/env bash
set -eu

export CWD=$(pwd)
export SONAR_SCANNER_DEFAULTS="-Dsonar.host.url=${SONARQUBE_URL} -Dsonar.login=${SONARQUBE_ACCESS_TOKEN}"
export GRADLE_VERSION=5.3.1

# SBT single-module
echo "Scanning SBT single-module project."
cd $CWD/sbt/single-module
sbt clean coverage test coverageReport scapegoat </dev/null
sonar-scanner ${SONAR_SCANNER_DEFAULTS}

# SBT multi-module
echo -e "\nScanning SBT multi-module project."
cd $CWD/sbt/multi-module
sbt clean coverage test coverageReport scapegoat </dev/null
sonar-scanner ${SONAR_SCANNER_DEFAULTS}

# Gradle single-module
echo -e "\nScanning Gradle single-module project."
cd $CWD/gradle/single-module
gradle wrapper --gradle-version $GRADLE_VERSION
./gradlew --no-daemon ${SONAR_SCANNER_DEFAULTS} clean test reportScoverage sonarqube

# Gradle multi-module
echo -e "\nScanning Gradle multi-module project."
cd $CWD/gradle/multi-module
gradle wrapper --gradle-version $GRADLE_VERSION
./gradlew --no-daemon ${SONAR_SCANNER_DEFAULTS} clean test reportScoverage sonarqube

# Maven single-module
echo -e "\nScanning Maven single-module project."
cd $CWD/mvn/single-module
mvn ${SONAR_SCANNER_DEFAULTS} -Dsurefire.useSystemClassLoader=false \
  clean test scoverage:report scala:compile sonar:sonar

# Maven multi-module
echo -e "\nScanning Maven multi-module project."
cd $CWD/mvn/multi-module
mvn ${SONAR_SCANNER_DEFAULTS} -Dsurefire.useSystemClassLoader=false \
  clean test scoverage:report scala:compile sonar:sonar

# Maven scala-java
echo -e "\nScanning Maven scala-java project."
cd $CWD/mvn/scala-java
mvn ${SONAR_SCANNER_DEFAULTS} -Dsurefire.useSystemClassLoader=false \
  clean test scoverage:report jacoco:report scala:compile sonar:sonar
