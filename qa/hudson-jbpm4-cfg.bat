@echo off

mvn clean install
cd modules\test-cfg
mvn clean test
cd ..\..

echo 
