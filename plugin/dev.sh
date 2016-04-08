#!/bin/bash

SONAR_HOME=~/bin/sonarqube-5.4
PLUGIN_VERSION=5.1.3

mvn install

PLUGIN_FILE="./target/sonar-scoverage-plugin-$PLUGIN_VERSION.jar"
if [ ! -f $PLUGIN_FILE ]; then
    echo "Plugin jar not found! [$PLUGIN_FILE]"
    exit 1
fi

$SONAR_HOME/bin/linux-x86-64/sonar.sh stop

rm $SONAR_HOME/extensions/plugins/sonar-scoverage-plugin-*
cp $PLUGIN_FILE $SONAR_HOME/extensions/plugins/sonar-scoverage-plugin-$PLUGIN_VERSION.jar

$SONAR_HOME/bin/linux-x86-64/sonar.sh start
