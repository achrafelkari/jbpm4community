#!/bin/sh
#
# runs the jboss integration test suite

export MAVEN_OPTS="-Dbind.address=$JBOSS_BINDADDR"

JBPM_VERSION=`mvn -Dexpression=project.version help:evaluate | grep '^4\.'`
export ANT_OPTS="-Djbpm.parent.dir=$WORKSPACE -Djbpm.version=$JBPM_VERSION \
       -Djboss.distro.dir=$SOURCE_REPO/jboss -Djboss.version=$JBOSS_VERSION $MAVEN_OPTS"

# build distribution
mvn -q -U -Pdistro,enterprise clean install
# set up
ant -f qa/build.xml testsuite.enterprise.setup
# run test suite
mvn -f modules/test-enterprise/test-enterprise-suite/pom.xml -Pruntest \
    -Dmaven.test.failure.ignore=true test
# tear down
ant -f qa/build.xml testsuite.enterprise.teardown
