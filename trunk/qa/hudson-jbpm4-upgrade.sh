#!/bin/sh
#
# runs the upgrade tests

MAVEN_OPTS="-Xmx512M"
ANT_OPTS="-Dold.jbpm.version=$OLD_JBPM_VERSION -Ddatabase=$DATABASE -Djbpm.parent.dir=$WORKSPACE"

cd qa/upgrade
mvn $ANT_OPTS dependency:copy
cd ../..

mvn -U -Pdistro,integration clean install
ant -f qa/build.xml testsuite.upgrade.setup

cd modules/test-upgrade
mvn $ANT_OPTS clean test
cd ../..

ant -f qa/build.xml testsuite.upgrade.teardown
