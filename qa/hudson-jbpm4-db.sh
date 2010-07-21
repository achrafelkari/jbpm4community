#!/bin/sh
#
# runs the database test suite

export MAVEN_OPTS="-Ddatabase=$DATABASE"

JBPM_VERSION=`mvn -Dexpression=project.version help:evaluate | grep '^4\.'`
export ANT_OPTS="-Djbpm.parent.dir=$WORKSPACE -Djbpm.version=$JBPM_VERSION $MAVEN_OPTS"

# build distribution
mvn -q -U -Pdistro clean install
# set up
ant -f qa/build.xml testsuite.db.setup
# run test suite
mvn -Dmaven.test.failure.ignore=true test
# tear down
ant -f qa/build.xml testsuite.db.teardown
