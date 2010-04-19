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
package org.jbpm.test;

import javax.jms.Queue;
import javax.naming.InitialContext;

import com.mockrunner.ejb.JNDIUtil;
import com.mockrunner.jms.JMSTestModule;
import com.mockrunner.mock.jms.JMSMockObjectFactory;
import com.mockrunner.mock.jms.MockQueueConnectionFactory;


/**
 * @author Tom Baeyens
 */
public class MockRunnerTest extends BaseJbpmTestCase {

  public void testMockRunner() throws Exception {
    JNDIUtil.initMockContextFactory();
    JMSMockObjectFactory jmsMockObjectFactory = new JMSMockObjectFactory();
    MockQueueConnectionFactory mockQueueConnectionFactory = jmsMockObjectFactory.getMockQueueConnectionFactory();
    JMSTestModule jmsTestModule = new JMSTestModule(jmsMockObjectFactory);
    
    
    InitialContext initialContext = new InitialContext();
    
    initialContext.bind("jms/ConnectionFactory", mockQueueConnectionFactory);
    
    assertSame(mockQueueConnectionFactory, initialContext.lookup("jms/ConnectionFactory"));

    Queue queue = jmsTestModule.getDestinationManager().createQueue("JmsActivityQueue");    

    initialContext.bind("queues/JmsActivityQueue", queue);

    assertSame(queue, initialContext.lookup("queues/JmsActivityQueue"));
  }
}
