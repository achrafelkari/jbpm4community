
set MAVEN_OPTS=-Xms1024M -Xmx1024M

cmd /C mvn -U -Pdistro,integration clean install
cmd /C ant -f qa/build.xml testsuite.jboss.setup

cd modules\test-cactus
cmd /C mvn -Pruntest test
cd ..\..

cmd /C ant -f qa/build.xml testsuite.jboss.teardown

echo 
