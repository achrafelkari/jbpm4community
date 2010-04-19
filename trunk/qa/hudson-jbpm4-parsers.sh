#!/bin/sh
#
# runs the jpdl parser backwards compatibility tests

mvn -U -Djpdlparser=$JPDL_VERSION clean install
