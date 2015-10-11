[![Build Status](https://travis-ci.org/NCR-CoDE/sonar-scalastyle.svg?branch=master)](https://travis-ci.org/NCR-CoDE/sonar-scalastyle)

# Sonar Scalastyle Plugin

Wraps up Scalastyle as a SonarQube plugin. Currently scalastyle provides 60 different checks. They  
are represented as 38 rules without parameters and 22 templates in SonarQube. Templates allow to instantiate
the same check multiple times with different values. For example different regular expression rules with different
severity levels.

http://www.scalastyle.org/

### Instructions

* Download and unzip SonarQube [4.5.5+](http://www.sonarqube.org/downloads/)
* Download and unzip [Sonar Runner](http://repo1.maven.org/maven2/org/codehaus/sonar/runner/sonar-runner-dist/2.4/sonar-runner-dist-2.4.zip).
* Download the Sonar Scalastyle Plugin
* Copy the plugin to `extensions/plugins` directory of SonarQube.
* Start the SonarQube server by running the `.sh/.bat` script under `sonarqube<version>/bin/<your os>` directory
* Create a sonar-project.properties file
* Run `sonar-runner` in your project's root directory.
* Check the report on http://localhost:9000/
* You can log in with default username and password 'admin'.

---

intended for

Sonar 4.5.5+
ScalaStyle 0.7.0