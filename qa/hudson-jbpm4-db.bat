@echo off

@echo database: %1

cmd /C mvn -U -Pdistro clean install
cmd /C ant -f qa/build.xml -Ddatabase=%1 testsuite.db.setup
cmd /C mvn -Ddatabase=%1 test
cmd /C ant -f qa/build.xml -Ddatabase=%1 testsuite.db.teardown
