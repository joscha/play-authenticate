#!/bin/sh
JAVA_HOME=$(/usr/libexec/java_home -v 1.8)
sbt --info -Drt.path="$JAVA_HOME/../Classes/classes.jar" release
