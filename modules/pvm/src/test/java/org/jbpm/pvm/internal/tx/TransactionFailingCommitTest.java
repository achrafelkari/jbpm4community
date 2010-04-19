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
public class TransactionFailingCommitTest extends BaseJbpmTestCase {
  
  public static class CommitException extends RuntimeException {
    private static final long serialVersionUID = 1L;
    public CommitException(String msg) {
      super(msg);
    }
  }
  
  public static class FailingPrepareResource extends TestResource {
    public void prepare() {
      super.prepare();
      throw new CommitException("resource couldn't prepare");
    }
  }

  public void testMultipleResourcesFailingPrepare() {
    TestResource resourceOne = null;
    TestResource resourceTwo = null;
    TestResource resourceThree = null;
    
    try {
      EnvironmentImpl environment = TxTests.openEnvironment(
        "<jbpm-configuration>" +
        "  <process-engine/>"+
        "  <transaction-context>" +
        "    <transaction />" +
        "    <object name='resourceOne' class='"+TestResource.class.getName()+"'>" +
        "      <enlist />" +
        "    </object>" +
        "    <object name='resourceTwo' class='"+FailingPrepareResource.class.getName()+"'>" +
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
      fail("expected exception");
    } catch (CommitException e) {
      // OK
    }
    
    assertTrue(resourceOne.isPrepared);
    assertFalse(resourceOne.isCommitted);
    assertTrue(resourceOne.isRolledBack);
    
    assertTrue(resourceTwo.isPrepared);
    assertFalse(resourceTwo.isCommitted);
    assertTrue(resourceTwo.isRolledBack);
    
    assertFalse(resourceThree.isPrepared);
    assertFalse(resourceThree.isCommitted);
    assertTrue(resourceThree.isRolledBack);
  }

  public static class FailingCommitResource extends TestResource {
    public void commit() {
      super.commit();
      throw new CommitException("resource couldn't commit");
    }
  }

  public void testMultipleResourcesFailingCommit() {
    TestResource resourceOne = null;
    TestResource resourceTwo = null;
    TestResource resourceThree = null;
    
    try {
      EnvironmentImpl environment = TxTests.openEnvironment(
        "<jbpm-configuration>" +
        "  <process-engine/>"+
        "  <transaction-context>" +
        "    <transaction />" +
        "    <object name='resourceOne' class='"+TestResource.class.getName()+"'>" +
        "      <enlist />" +
        "    </object>" +
        "    <object name='resourceTwo' class='"+FailingCommitResource.class.getName()+"'>" +
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
      fail("expected exception");
    } catch (CommitException e) {
      // OK
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
}
