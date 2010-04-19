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
import javax.jms.Session;

import org.jbpm.test.JbpmCustomCfgTestCase;
import org.jbpm.test.JmsTopicListener;


/**
 * @author Tom Baeyens
 */
public class JmsTopicTest extends JbpmCustomCfgTestCase {

  String deploymentId;
  JmsTopicListener jmsTopicListener;
  
  protected void setUp() throws Exception {
    super.setUp();
    
    deploymentId = repositoryService.createDeployment()
        .addResourceFromClasspath("org/jbpm/test/jms/topicprocess.jpdl.xml")
        .deploy();
    
    jmsCreateTopic("jms/ConnectionFactory", "topic/ProductTopic");
    jmsTopicListener = jmsStartTopicListener("jms/ConnectionFactory", "topic/ProductTopic", true, Session.AUTO_ACKNOWLEDGE);
  }

  protected void tearDown() throws Exception {
    jmsTopicListener.stop();
    jmsRemoveTopic("jms/ConnectionFactory", "topic/ProductTopic");

    repositoryService.deleteDeploymentCascade(deploymentId);
    
    super.tearDown();
  }

  public void testTestTopicMessage() throws Exception {
    executionService.startProcessInstanceByKey("JmsTopic");
    
    MapMessage mapMessage = (MapMessage) jmsTopicListener.getNextMessage(1000);
    assertEquals("shampoo", mapMessage.getString("product"));
  }

}
