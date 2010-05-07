#!/bin/sh
#
# runs the jboss integration test suite

MAVEN_OPTS="-Xmx512M -Dtomcat.bind.address=$TOMCAT_BINDADDR"
ANT_OPTS="-Djbpm.parent.dir=$WORKSPACE -Djboss.distro.dir=$SOURCE_REPO/jboss \
-Dtomcat.distro.dir=tomcat.downloads -Dtomcat.bind.address=$TOMCAT_BINDADDR"

mvn -U -Pdistro,integration clean install
ant -f qa/build.xml reinstall.jbpm
ant -f qa/build.xml testsuite.tomcat.setup

cd modules/test-cactus
mvn -Pruntest test
cd ../..

ant -f qa/build.xml testsuite.tomcat.teardown
