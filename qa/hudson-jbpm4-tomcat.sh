#!/bin/sh
#
# runs the tomcat integration test suite

export MAVEN_OPTS="-Dbind.address=$TOMCAT_BINDADDR"

export ANT_OPTS="-Djbpm.parent.dir=$WORKSPACE -Dbind.address=$TOMCAT_BINDADDR \
       -Dtomcat.distro.dir=tomcat.downloads"

# build distribution
mvn -U -Pdistro,integration clean install
# set up
ant -f qa/build.xml testsuite.tomcat.setup
# run test suite
mvn -f modules/test-cactus/pom.xml -Pruntest -Dmaven.test.failure.ignore=true test
# tear down
ant -f qa/build.xml testsuite.tomcat.teardown
