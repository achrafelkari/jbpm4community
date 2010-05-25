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

import junit.framework.TestCase;
import javax.transaction.Status;
import javax.transaction.Synchronization;
import org.springframework.transaction.support.TransactionSynchronization;

/**
 * JBPM-2863.
 *
 * @author Huisheng Xu
 */
public class SpringToStandardSynchronizationTest extends TestCase {
    public void testCommited() {
        MockSync sync = new MockSync();
        SpringToStandardSynchronization stss = new SpringToStandardSynchronization(sync);
        stss.afterCompletion(TransactionSynchronization.STATUS_COMMITTED);
        assertEquals(Status.STATUS_COMMITTED, sync.getStatus());
    }

    public void testRollback() {
        MockSync sync = new MockSync();
        SpringToStandardSynchronization stss = new SpringToStandardSynchronization(sync);
        stss.afterCompletion(TransactionSynchronization.STATUS_ROLLED_BACK);
        assertEquals(Status.STATUS_ROLLEDBACK, sync.getStatus());
    }

    public void testUnknowStatus() {
        Synchronization sync = new MockSync();
        SpringToStandardSynchronization stss = new SpringToStandardSynchronization(sync);
        try {
           stss.afterCompletion(TransactionSynchronization.STATUS_UNKNOWN);
           fail();
        } catch(TransactionException ex) {
            assertTrue(true);
        }
    }

    static class MockSync implements Synchronization {
        private int status;

        public int getStatus() {
            return status;
        }

        public void afterCompletion(int status) {
            this.status = status;
        }

        public void beforeCompletion() {
        }
    }
}
