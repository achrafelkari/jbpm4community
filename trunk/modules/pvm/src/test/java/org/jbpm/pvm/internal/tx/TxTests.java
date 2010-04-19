/*
 * JBoss, Home of Professional Open Source
 * Copyright 2005, JBoss Inc., and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jbpm.pvm.internal.tx;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.jbpm.pvm.internal.cfg.ConfigurationImpl;
import org.jbpm.pvm.internal.env.EnvironmentFactory;
import org.jbpm.pvm.internal.env.EnvironmentImpl;

/**
 * @author Tom Baeyens
 */
public class TxTests {
  
  public static EnvironmentImpl openEnvironment(String xmlString) {
    EnvironmentFactory environmentFactory = (EnvironmentFactory) new ConfigurationImpl()
        .setXmlString(xmlString)
        .skipDbCheck()
        .buildProcessEngine();
    return environmentFactory.openEnvironment();
  }

  public static Test suite() {
    TestSuite suite = new TestSuite("org.jbpm.pvm.internal.tx");
    //$JUnit-BEGIN$
    suite.addTestSuite(TransactionResourcesCommitTest.class);
    suite.addTestSuite(TransactionResourcesSetRollbackOnlyTest.class);
    suite.addTestSuite(EnlistTest.class);
    suite.addTestSuite(TransactionFailingCommitTest.class);
    //$JUnit-END$
    return suite;
  }

}
