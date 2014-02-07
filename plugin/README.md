# Sonar Scoverage Plugin source code #

Useful bash script for plugin development to stop Sonar server, build plugin, copy it to Sonar plugin
directory and start Sonar server again:

    <SONAR_INSTALL_DIR>/bin/linux-x86-64/sonar.sh stop

    mvn install
    cp ./target/sonar-scoverage-plugin-1.0-SNAPSHOT.jar <SONAR_INSTALL_DIR>/extensions/plugins/

    <SONAR_INSTALL_DIR>/bin/linux-x86-64/sonar.sh start