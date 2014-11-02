#!/bin/sh
JAVA_HOME=$(/usr/libexec/java_home -v 1.6)
sbt --info -Drt.path="$JAVA_HOME/../Classes/classes.jar" release
