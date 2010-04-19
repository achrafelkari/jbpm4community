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

import org.jbpm.pvm.internal.env.EnvironmentImpl;
import org.jbpm.test.BaseJbpmTestCase;


/**
 * @author Tom Baeyens
 */
public class TransactionResourcesCommitTest extends BaseJbpmTestCase {
  
  public void testOneResourceCommit() {

    TestResource resourceOne = null;
    
    EnvironmentImpl environment = TxTests.openEnvironment(
      "<jbpm-configuration>" +
      "  <process-engine/>"+
      "  <transaction-context>" +
      "    <transaction />" +
      "    <object name='resourceOne' class='"+TestResource.class.getName()+"'>" +
      "      <enlist />" +
      "    </object>" +
      "  </transaction-context>" +
      "</jbpm-configuration>"
    );
    try {
      StandardTransaction standardTransaction = environment.get(StandardTransaction.class);
      standardTransaction.begin();

      resourceOne = (TestResource) environment.get("resourceOne");
      
      assertFalse(resourceOne.isPrepared);
      assertFalse(resourceOne.isCommitted);
      assertFalse(resourceOne.isRolledBack);
      
      standardTransaction.complete();

    } finally {
      environment.close();
    }
    
    assertTrue(resourceOne.isPrepared);
    assertTrue(resourceOne.isCommitted);
    assertFalse(resourceOne.isRolledBack);
  }

  public void testMultipleResourcesCommit() {
    TestResource resourceOne = null;
    TestResource resourceTwo = null;
    TestResource resourceThree = null;
    
    EnvironmentImpl environment = TxTests.openEnvironment(
      "<jbpm-configuration>" +
      "  <process-engine/>"+
      "  <transaction-context>" +
      "    <transaction />" +
      "    <object name='resourceOne' class='"+TestResource.class.getName()+"'>" +
      "      <enlist />" +
      "    </object>" +
      "    <object name='resourceTwo' class='"+TestResource.class.getName()+"'>" +
      "      <enlist />" +
      "    </object>" +
      "    <object name='resourceThree' class='"+TestResource.class.getName()+"'>" +
      "      <enlist />" +
      "    </object>" +
      "  </transaction-context>" +
      "</jbpm-configuration>"
    );
    try {
      StandardTransaction standardTransaction = environment.get(StandardTransaction.class);
      standardTransaction.begin();

      resourceOne = (TestResource) environment.get("resourceOne");
      assertFalse(resourceOne.isPrepared);
      assertFalse(resourceOne.isCommitted);
      assertFalse(resourceOne.isRolledBack);

      resourceTwo = (TestResource) environment.get("resourceTwo");
      assertFalse(resourceTwo.isPrepared);
      assertFalse(resourceTwo.isCommitted);
      assertFalse(resourceTwo.isRolledBack);
   
      resourceThree = (TestResource) environment.get("resourceThree");
      assertFalse(resourceThree.isPrepared);
      assertFalse(resourceThree.isCommitted);
      assertFalse(resourceThree.isRolledBack);

      standardTransaction.complete();

    } finally {
      environment.close();
    }
    
    assertTrue(resourceOne.isPrepared);
    assertTrue(resourceOne.isCommitted);
    assertFalse(resourceOne.isRolledBack);
    
    assertTrue(resourceTwo.isPrepared);
    assertTrue(resourceTwo.isCommitted);
    assertFalse(resourceTwo.isRolledBack);
    
    assertTrue(resourceThree.isPrepared);
    assertTrue(resourceThree.isCommitted);
    assertFalse(resourceThree.isRolledBack);
  }

  public void testFetchOneResourceOutOfManyCommit() {
    TestResource resourceTwo = null;
    
    EnvironmentImpl environment = TxTests.openEnvironment(
      "<jbpm-configuration>" +
      "  <process-engine/>"+
      "  <transaction-context>" +
      "    <transaction />" +
      "    <object name='resourceOne' class='"+TestResource.class.getName()+"'>" +
      "      <enlist />" +
      "    </object>" +
      "    <object name='resourceTwo' class='"+TestResource.class.getName()+"'>" +
      "      <enlist />" +
      "    </object>" +
      "    <object name='resourceThree' class='"+TestResource.class.getName()+"'>" +
      "      <enlist />" +
      "    </object>" +
      "  </transaction-context>" +
      "</jbpm-configuration>"
    );
    try {
      StandardTransaction standardTransaction = environment.get(StandardTransaction.class);
      standardTransaction.begin();

      resourceTwo = (TestResource) environment.get("resourceTwo");
      assertFalse(resourceTwo.isPrepared);
      assertFalse(resourceTwo.isCommitted);
      assertFalse(resourceTwo.isRolledBack);
      
      assertEquals(1, environment.get(StandardTransaction.class).resources.size());

      standardTransaction.complete();

    } finally {
      environment.close();
    }

    assertEquals(1, environment.get(StandardTransaction.class).resources.size());

    assertTrue(resourceTwo.isPrepared);
    assertTrue(resourceTwo.isCommitted);
    assertFalse(resourceTwo.isRolledBack);
  }
}
