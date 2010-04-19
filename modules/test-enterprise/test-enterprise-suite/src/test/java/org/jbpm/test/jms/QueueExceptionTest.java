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
package org.jbpm.test.jms;

import junit.framework.Test;

import org.apache.cactus.ServletTestSuite;
import org.jbpm.test.JbpmTestCase;


/**
 * @author Tom Baeyens
 */
public class QueueExceptionTest extends JbpmTestCase {

  public static Test suite() {
    ServletTestSuite servletTestSuite = new ServletTestSuite();
    servletTestSuite.addTestSuite(QueueTextMessageTest.class);
    return servletTestSuite;
  }
    
  protected void setUp() throws Exception {
    super.setUp();  
    registerDeployment(repositoryService.createDeployment()
        .addResourceFromClasspath("org/jbpm/test/jms/queue.text.process.jpdl.xml")
        .deploy());  
  }
  
  public void testQueueMessageException() throws Exception {
    try {
      executionService.startProcessInstanceByKey("JmsQueueException");
      fail("expected exception");
    } catch (Exception e) {
      // OK
    }

    jmsAssertQueueEmptyXA("java:JmsXA", "queue/jbpm-test-queue", 1000);
    
    assertEquals(0, executionService.createProcessInstanceQuery().list().size());
  }
}
