#!/bin/sh
#
# runs the upgrade tests

MAVEN_OPTS="-Xms1024M -Xmx1024M"
ANT_PROPERTIES="-Dold.jbpm.version=$OLD_JBPM_VERSION -Ddatabase=$DATABASE -Djbpm.parent.dir=$WORKSPACE"
echo ANT_PROPERTIES=${ANT_PROPERTIES}

cd qa/upgrade
mvn $ANT_PROPERTIES dependency:copy
cd ../..

mvn -U -Pdistro,integration clean install
ant -f qa/build.xml $ANT_PROPERTIES testsuite.upgrade.setup

cd modules/test-upgrade
mvn $ANT_PROPERTIES clean test
cd ../..

ant -f qa/build.xml $ANT_PROPERTIES testsuite.upgrade.teardown
