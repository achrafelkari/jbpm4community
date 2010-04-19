#!/bin/sh
#
# runs the configuration test suite

mvn clean install
cd modules/test-cfg
mvn clean test
cd ../..
