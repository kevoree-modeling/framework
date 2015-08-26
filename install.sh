#!/usr/bin/env bash

export JAVA_HOME=$(/usr/libexec/java_home)
export MAVEN_OPTS="-Xmx2000m -XX:MaxPermSize=256m"
mvn clean install