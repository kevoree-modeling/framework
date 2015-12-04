#!/usr/bin/env bash
export JAVA_HOME=$(/usr/libexec/java_home)
mvn -P release release:clean
mvn -P release release:prepare
mvn -P release release:perform