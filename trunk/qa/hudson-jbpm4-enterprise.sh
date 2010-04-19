#!/bin/sh
#
# runs the jboss integration test suite

MAVEN_OPTS="-Xms1024M -Xmx1024M"
ANT_PROPERTIES="-Djboss.version=$JBOSS_VERSION -Djbpm.parent.dir=$WORKSPACE -Djboss.distro.dir=$SOURCE_REPO/jboss"
echo ANT_PROPERTIES=${ANT_PROPERTIES}

mvn -U -Pdistro,enterprise clean install
ant -f qa/build.xml $ANT_PROPERTIES testsuite.enterprise.setup

cd modules/test-enterprise/test-enterprise-suite
mvn -Pruntest test
cd ../../..

ant -f qa/build.xml $ANT_PROPERTIES testsuite.enterprise.teardown
