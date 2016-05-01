# Multi-module SBT sample project for Sonar Scoverage plugin #

 1. Create quality profile for Scala language and set it to be used by default.

 2. Run scoverage to generate coverage reports:

    $ sbt clean coverage test

 3. And then run Sonar runner to upload data from reports to the Sonar server:

    $ sonar-runner

## Requirements ##

- Installed Sonar Scoverage plugin
- Installed SBT
- Installed Sonar runner