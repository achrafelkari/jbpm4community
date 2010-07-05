#!/bin/sh
#
# runs the jboss integration test suite

export MAVEN_OPTS="-Dbind.address=$JBOSS_BINDADDR"

export ANT_OPTS="-Djbpm.parent.dir=$WORKSPACE -Dbind.address=$JBOSS_BINDADDR \
       -Djboss.distro.dir=$SOURCE_REPO/jboss -Djboss.version=$JBOSS_VERSION"

# build distribution
mvn -U -Pdistro,enterprise clean install
# set up
ant -f qa/build.xml testsuite.enterprise.setup
# run test suite
mvn -f modules/test-enterprise/test-enterprise-suite/pom.xml -Pruntest \
    -Dmaven.test.failure.ignore=true test
# tear down
ant -f qa/build.xml testsuite.enterprise.teardown
