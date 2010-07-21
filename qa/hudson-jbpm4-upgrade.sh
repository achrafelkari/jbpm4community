#!/bin/sh
#
# runs the upgrade tests

export MAVEN_OPTS="-Dold.jbpm.version=$OLD_JBPM_VERSION -Ddatabase=$DATABASE"

JBPM_VERSION=`mvn -Dexpression=project.version help:evaluate | grep '^4\.'`
export ANT_OPTS="-Djbpm.parent.dir=$WORKSPACE -Djbpm.version=$JBPM_VERSION $MAVEN_OPTS"

# build distribution
mvn -q -f qa/upgrade/pom.xml dependency:copy
mvn -q -U -Pdistro,integration clean install
# set up
ant -f qa/build.xml testsuite.upgrade.setup
# run test suite
mvn -f modules/test-upgrade/pom.xml clean test
# tear down
ant -f qa/build.xml testsuite.upgrade.teardown
