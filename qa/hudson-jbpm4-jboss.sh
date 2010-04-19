#!/bin/sh
#
# runs the jboss integration test suite

MAVEN_OPTS="-Xms1024M -Xmx1024M"
ANT_PROPERTIES="-Djboss.version=$JBOSS_VERSION -Djbpm.parent.dir=$WORKSPACE -Djboss.distro.dir=$SOURCE_REPO/jboss"
echo ANT_PROPERTIES=${ANT_PROPERTIES}

echo just in case the previous run didnt complete ok, we stop jboss
ant -f modules/distro/src/main/files/install/build.xml $ANT_PROPERTIES reinstall.jboss
ant -f modules/distro/src/main/files/install/build.xml $ANT_PROPERTIES stop.jboss

mvn -U -Pdistro,integration clean install
ant -f qa/build.xml $ANT_PROPERTIES testsuite.jboss.setup

cd modules/test-cactus
mvn -Pruntest test
cd ../..

ant -f qa/build.xml $ANT_PROPERTIES testsuite.jboss.teardown
