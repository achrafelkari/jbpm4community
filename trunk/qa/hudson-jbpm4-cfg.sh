#!/bin/sh
#
# runs the configuration test suite

# build distribution
mvn -U -Pdistro clean install
# run test suite
mvn -f modules/test-cfg/pom.xml -Dmaven.test.failure.ignore=true \
    clean test
