#!/bin/sh
#
# reinstalls jbpm

echo 'make sure you have jbpm.parent.dir specified in your \${user.home}/.jbpm4/build.properties'
mvn -U -Pdistro clean install
ant -f qa/build.xml reinstall.jbpm

echo
