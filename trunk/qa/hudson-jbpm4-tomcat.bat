set MAVEN_OPTS=-Xms1024M -Xmx1024M

cmd /C mvn -U -Pdistro,integration clean install
cmd /C ant -f qa/build.xml reinstall.jbpm
cmd /C ant -f qa/build.xml testsuite.tomcat.setup

cd modules\test-cactus
cmd /C mvn -Pruntest package
cd ..\..

cmd /C ant -f qa/build.xml testsuite.tomcat.teardown

echo 
