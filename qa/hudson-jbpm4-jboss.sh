#!/bin/sh
#
# runs the jboss integration test suite

export MAVEN_OPTS="-Dbind.address=$JBOSS_BINDADDR"

export ANT_OPTS="-Djbpm.parent.dir=$WORKSPACE -Dbind.address=$JBOSS_BINDADDR \
       -Djboss.distro.dir=$SOURCE_REPO/jboss -Djboss.version=$JBOSS_VERSION"

# build distribution
mvn -U -Pdistro,integration clean install
# set up
ant -f qa/build.xml testsuite.jboss.setup
# run test suite
mvn -f modules/test-cactus/pom.xml -Pruntest -Dmaven.test.failure.ignore=true test
# tear down
ant -f qa/build.xml testsuite.jboss.teardown
