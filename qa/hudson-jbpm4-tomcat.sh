#!/bin/sh
#
# runs the jboss integration test suite

MAVEN_OPTS="-Xms1024M -Xmx1024M"

ANT_PROPERTIES="-Djbpm.parent.dir=$WORKSPACE -Djboss.distro.dir=$SOURCE_REPO/jboss -Dtomcat.distro.dir=tomcat.downloads" 

mvn -U -Pdistro,integration clean install
ant -f qa/build.xml $ANT_PROPERTIES reinstall.jbpm
ant -f qa/build.xml $ANT_PROPERTIES testsuite.tomcat.setup

cd modules/test-cactus
mvn -Pruntest test
cd ../..

ant -f qa/build.xml $ANT_PROPERTIES testsuite.tomcat.teardown
