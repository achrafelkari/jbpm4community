#!/bin/sh
#
# runs the jboss integration test suite

MAVEN_OPTS="-Xmx512M -Djboss.bind.address=$JBOSS_BINDADDR"
ANT_OPTS="-Djboss.version=$JBOSS_VERSION -Djbpm.parent.dir=$WORKSPACE -Djboss.distro.dir=$SOURCE_REPO/jboss -Djboss.bind.address=$JBOSS_BINDADDR"

mvn -U -Pdistro,enterprise clean install
ant -f qa/build.xml testsuite.enterprise.setup

cd modules/test-enterprise/test-enterprise-suite
mvn -Pruntest test
cd ../../..

ant -f qa/build.xml testsuite.enterprise.teardown
