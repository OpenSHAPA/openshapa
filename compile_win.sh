#!/usr/bin/env bash
# Used windows 7 Professional -- service pack 1
# Setup java (jdk 1.8.144)
# Download jdk for windows x64 and install into default location C:/Program Files
#   http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html
# Set the environment variable in System Variables
# JAVA_HOME to C:\Program Files\Java\jdk1.8.0_144
#
# Setup maven (apache maven 3.5.0)
# Download the zip and extract it to C:/Program Files
#   https://maven.apache.org/download.cgi
# Add C:/Program Files/apache-maven-3.5.0/bin to the path to enable the command mvn
#
curl http://www.datavyu.org/docs/user-guide.pdf > packaged_docs/user-guide.pdf
# Add the -X flag to get debug information
# Removed jar:jar goal because package already includes that goal and it was run TWICE!
# See: https://stackoverflow.com/questions/40964500/maven-jar-plugin-3-0-2-error-you-have-to-use-a-classifier-to-attach-supplementa
mvn -Prelease,win-package -Dmaven.test.skip=true clean package launch4j:launch4j assembly:assembly
# For debugging purposes etc. you may want to setup up a maven configuration in Intellij
# Follow these steps:
# Next to the run button click on the drop-down menu item 'Edit configuration'
# In the dialog select on the left-hand side '+' and select 'maven'
# Fill out working directory: C:/Users/Florian/integration/datavyu (yours is different)
# Command line: -Dmaven.test.skip=true clean compile jfx:run (for now we exclude tests)