Tests in the enterprise module are integration tests. The maven surefire plugin
runs tests during the 'test' phase by default. This module is configured to
skip test execution during the 'test' phase, allowing the build to reach the
'package' phase and assemble the ejb jar.

Enterprise tests are associated to the 'integration-test' phase that occurs
after 'package' but before 'install'. In order to run properly, enterprise
tests require a running JBoss application server. The address to which the
server is bound must be specified in the Maven property 'jboss.bind.address',
otherwise the tests will be skipped.

mvn -Djboss.bind.address=127.0.0.1 test

Because several databases do not allow DDL in JTA transactions, the database
schema has to be created prior to executing the tests for the first time,
unless you've chosen Hsql as the target database when running the installer.
