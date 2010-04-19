@echo off
rem runs the jboss integration test suite

set MAVEN_OPTS=-Xms1024m -Xmx1024m
set ANT_PROPERTIES="-Djboss.version=5.1.0.GA"
echo ANT_PROPERTIES=%ANT_PROPERTIES%

cmd /C mvn -U -Pdistro,enterprise clean install
cmd /C ant -f qa/build.xml %ANT_PROPERTIES% testsuite.enterprise.setup

cd modules\test-enterprise\test-enterprise-suite
cmd /C  mvn -Pruntest test
cd ..\..\..

cmd /C ant -f qa/build.xml %ANT_PROPERTIES% testsuite.enterprise.teardown

echo 
