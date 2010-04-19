#!/bin/sh
#
# runs the smoke test suite

mvn -U -Prun.spring.testsuite clean install
