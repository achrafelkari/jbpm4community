#!/bin/sh
#
# runs the jboss integration test suite

MAVEN_OPTS="-Xmx512M -Djboss.bind.address=$JBOSS_BINDADDR"
ANT_OPTS="-Djboss.version=$JBOSS_VERSION -Djbpm.parent.dir=$WORKSPACE \
-Djboss.distro.dir=$SOURCE_REPO/jboss -Djboss.bind.address=$JBOSS_BINDADDR \
-Dhsql.bind.address=$JBOSS_BINDADDR"

# just in case the previous run didnt complete ok, we stop jboss
ant -f modules/distro/src/main/files/install/build.xml reinstall.jboss
ant -f modules/distro/src/main/files/install/build.xml stop.jboss

mvn -U -Pdistro,integration clean install
ant -f qa/build.xml testsuite.jboss.setup

cd modules/test-cactus
mvn -Pruntest test
cd ../..

ant -f qa/build.xml testsuite.jboss.teardown
