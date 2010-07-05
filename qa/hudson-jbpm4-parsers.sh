#!/bin/sh
#
# runs the jpdl parser backwards compatibility tests

mvn -U -Djpdlparser=$JPDL_VERSION -Dmaven.test.failure.ignore=true clean install
