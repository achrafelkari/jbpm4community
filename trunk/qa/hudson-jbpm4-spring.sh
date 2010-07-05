#!/bin/sh
#
# runs the spring test suite

mvn -U -Prun.spring.testsuite -Dmaven.test.failure.ignore=true clean install
