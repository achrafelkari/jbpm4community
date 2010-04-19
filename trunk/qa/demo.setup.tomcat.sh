#!/bin/sh

set MAVEN_OPTS=-Xms1024M -Xmx1024M
mvn -U -Pdistro clean install
ant -f qa/build.xml reinstall.jbpm
ant -f modules/distro/src/main/files/install/build.xml demo.setup.tomcat
