@echo off 

cmd /C mvn -U -Pdistro,enterprise clean install
cmd /C ant -f qa/build.xml -Djboss.version=5.1.0.GA testsuite.enterprise.setup.for.debug
