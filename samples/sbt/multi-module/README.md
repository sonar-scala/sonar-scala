# Multi-module SBT sample project for Sonar Scoverage plugin #

Run scoverage to generate coverage reports:

    $ sbt clean scoverage:test

And then run Sonar runner to upload data from reports to the Sonar server:

    $ sonar-runner

## Requirements ##

- Installed Sonar Scoverage plugin
- Installed SBT
- Installed Sonar runner