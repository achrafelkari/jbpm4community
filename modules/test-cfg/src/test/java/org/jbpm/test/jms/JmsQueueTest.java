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

import javax.jms.MapMessage;

import org.jbpm.test.JbpmCustomCfgTestCase;


/**
 * @author Tom Baeyens
 */
public class JmsQueueTest extends JbpmCustomCfgTestCase {

  String deploymentId;
  
  protected void setUp() throws Exception {
    super.setUp();
    
    deploymentId = repositoryService.createDeployment()
        .addResourceFromClasspath("org/jbpm/test/jms/queueprocess.jpdl.xml")
        .deploy();
    
    jmsCreateQueue("jms/ConnectionFactory", "queue/ProductQueue");
  }

  protected void tearDown() throws Exception {
    jmsRemoveQueue("jms/ConnectionFactory", "queue/ProductQueue");
    
    repositoryService.deleteDeploymentCascade(deploymentId);
    
    super.tearDown();
  }

  public void testQueueMessage() throws Exception {
    executionService.startProcessInstanceByKey("JmsQueue");
    
    MapMessage mapMessage = (MapMessage) jmsConsumeMessageFromQueue("jms/ConnectionFactory", "queue/ProductQueue");
    assertEquals("shampoo", mapMessage.getString("product"));
  }
}
