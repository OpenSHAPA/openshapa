#!/usr/bin/env bash
# Used a macOS Serria 10.12.16
#
# Setup java (here SDK 1.8.144)
# http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html (download dmg and install)
# In my case it got installed into
# Add export JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk1.8.0_144.jdk/Contents/Home to ~/.bash_profile
#
# Setup maven (here apache maven 3.5)
# https://maven.apache.org/download.cgi (download zip and extract to /opt)
# Add export PATH=/opt/apache-maven-3.5.0/bin:$PATH to ~/.bash_profile
#
curl http://www.datavyu.org/docs/user-guide.pdf > packaged_docs/user-guide.pdf
# The arguments 'package' and 'appbundle:bundle' are there to create an application bundle
# mvn clean package appbundle:bundle -Dmaven.test.skip=true jfx:native

# This line builds using the JFX packager
mvn clean -Dmaven.test.skip=true jfx:native
