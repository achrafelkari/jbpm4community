set MAVEN_OPTS=-Xms1024M -Xmx1024M
set ANT_PROPERTIES=-Dold.jbpm.version=%1 -Ddatabase=%2

cd qa\upgrade
cmd /C mvn %ANT_PROPERTIES% dependency:copy
cd ..\..

cmd /C mvn -U -Pdistro,integration clean install
cmd /C ant -f qa/build.xml %ANT_PROPERTIES% testsuite.upgrade.setup

cd modules\test-upgrade
cmd /C mvn %ANT_PROPERTIES% clean test
cd ..\..

cmd /C ant -f qa/build.xml %ANT_PROPERTIES% testsuite.upgrade.teardown
