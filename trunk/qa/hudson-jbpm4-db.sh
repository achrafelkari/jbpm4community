#!/bin/sh
#
# runs the database test suite

# build distribution
mvn -U -Pdistro clean install
# set up
ant -f qa/build.xml -Ddatabase=$DATABASE -Djbpm.parent.dir=$WORKSPACE testsuite.db.setup
# run test suite
mvn -Ddatabase=$DATABASE -Dmaven.test.failure.ignore=true test
# tear down
ant -f qa/build.xml -Ddatabase=$DATABASE -Djbpm.parent.dir=$WORKSPACE testsuite.db.teardown
