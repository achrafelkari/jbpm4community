#!/bin/sh

set MAVEN_OPTS=-Xms1024M -Xmx1024M
mvn -U -Pdistro clean install
ant -f qa/build.xml manual.testrun.setup
