#!/bin/sh
#
# runs the smoke test suite

mvn -U -Dmaven.test.failure.ignore=true clean install
