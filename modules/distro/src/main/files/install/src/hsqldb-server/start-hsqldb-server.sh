#!/bin/sh
[ -z "$JBOSS_BINDADDR" ] && JBOSS_BINDADDR=localhost
java -cp hsqldb.jar org.hsqldb.Server -address $JBOSS_BINDADDR -port 1701 -dbname.0 jbpmDatabase