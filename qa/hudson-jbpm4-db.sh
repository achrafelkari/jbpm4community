#!/bin/sh
#
# runs the smoke test suite

mvn -U -Pdistro clean install
ant -f qa/build.xml -Ddatabase=$DATABASE -Djbpm.parent.dir=$WORKSPACE testsuite.db.setup
mvn -Ddatabase=$DATABASE test
ant -f qa/build.xml -Ddatabase=$DATABASE -Djbpm.parent.dir=$WORKSPACE testsuite.db.teardown
