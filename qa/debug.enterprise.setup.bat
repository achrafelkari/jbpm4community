cmd /C mvn -U -Pdistro,integration clean install
cmd /C ant -f qa/build.xml -Djboss.version=5.1.0.GA testsuite.enterprise.setup.for.debug
start "JBOSS DEBUG" /DC:\Software\jbpm-4.4-SNAPSHOT\jboss-5.1.0.GA\bin run.bat
cd modules\test-enterprise\test-enterprise-suite

@echo off
echo 
echo after jboss booted and you connected your eclipse to it in a debug session execute following command
echo mvn -Pruntest test
