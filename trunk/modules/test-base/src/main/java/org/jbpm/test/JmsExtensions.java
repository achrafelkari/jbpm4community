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

import java.util.HashMap;
import java.util.Map;

import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.QueueSession;
import javax.jms.XAQueueConnection;
import javax.jms.XAQueueConnectionFactory;
import javax.jms.XAQueueSession;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import junit.framework.AssertionFailedError;

import org.jbpm.api.JbpmException;

import com.mockrunner.ejb.JNDIUtil;
import com.mockrunner.jms.JMSTestModule;
import com.mockrunner.mock.jms.JMSMockObjectFactory;
import com.mockrunner.mock.jms.MockQueue;
import com.mockrunner.mock.jms.MockQueueConnectionFactory;
import com.mockrunner.mock.jms.MockTopic;


/**
 * @author Tom Baeyens
 */
public abstract class JmsExtensions {
  
  static {
      try {
        new InitialContext().getEnvironment();
      } catch (NamingException e) {
        try {
          JNDIUtil.initMockContextFactory();
        } catch (Exception e2) {
          e.printStackTrace();
          throw new RuntimeException("coudn't initialize mock jndi: "+e2.getMessage(), e2);
        }
      }
  }
  
  static Map<String, JMSMockObjectFactory> jmsMockObjectFactories = new HashMap<String, JMSMockObjectFactory>();
  static Map<String, JMSTestModule> jmsTestModules = new HashMap<String, JMSTestModule>();
  
  public static void createQueue(String connectionFactoryJndiName, String queueJndiName) {
    MockQueue queue = getJmsTestModule(connectionFactoryJndiName)
        .getDestinationManager()
        .createQueue(queueJndiName);
    
    bindToJndi(queueJndiName, queue);
  }

  public static void removeQueue(String connectionFactoryJndiName, String queueJndiName) {
    getJmsTestModule(connectionFactoryJndiName)
        .getDestinationManager()
        .removeQueue(queueJndiName);

    unbindFromJndi(queueJndiName);
  }

  public static void createTopic(String connectionFactoryJndiName, String topicJndiName) {
    MockTopic topic = getJmsTestModule(connectionFactoryJndiName)
        .getDestinationManager()
        .createTopic(topicJndiName);

    bindToJndi(topicJndiName, topic);
  }

  public static void removeTopic(String connectionFactoryJndiName, String topicJndiName) {
    getJmsTestModule(connectionFactoryJndiName)
        .getDestinationManager()
        .removeTopic(topicJndiName);

    unbindFromJndi(topicJndiName);
  }

  protected static void bindToJndi(String jndiName, Object object) {
    try {
      new InitialContext().bind(jndiName, object);
    } catch (Exception e) {
      throw new JbpmException("couldn't bind object '"+object+"' to jndi name '"+jndiName+"': "+e.getMessage(), e);
    }
  }

  protected static void unbindFromJndi(String jndiName) {
    try {
      new InitialContext().unbind(jndiName);
    } catch (Exception e) {
      throw new JbpmException("couldn't unbind object from jndi name '"+jndiName+"': "+e.getMessage(), e);
    }
  }

  private static JMSTestModule getJmsTestModule(String connectionFactoryJndiName) {
    JMSTestModule jmsTestModule = jmsTestModules.get(connectionFactoryJndiName);
    if (jmsTestModule==null) {
      JMSMockObjectFactory jmsMockObjectFactory = getMockObjectFactory(connectionFactoryJndiName);
      jmsTestModule = new JMSTestModule(jmsMockObjectFactory);
      jmsTestModules.put(connectionFactoryJndiName, jmsTestModule);
    }
    return jmsTestModule;
  }

  private static JMSMockObjectFactory getMockObjectFactory(String connectionFactoryJndiName) {
    JMSMockObjectFactory jmsMockObjectFactory = jmsMockObjectFactories.get(connectionFactoryJndiName);
    if (jmsMockObjectFactory==null) {
      jmsMockObjectFactory = new JMSMockObjectFactory();
      jmsMockObjectFactories.put(connectionFactoryJndiName, jmsMockObjectFactory);
      MockQueueConnectionFactory mockQueueConnectionFactory = jmsMockObjectFactory.getMockQueueConnectionFactory();
      try {
        new InitialContext().bind(connectionFactoryJndiName, mockQueueConnectionFactory);
      } catch (Exception e) {
        throw new JbpmException("couldn't bind mock queue connection factory  '"+connectionFactoryJndiName+"': "+e.getMessage(), e);
      }
    }
    return jmsMockObjectFactory;
  }

  public static Object consumeMessageFromQueueXA(String connectionFactoryJndiName, String queueJndiName, long timeout) {
    try {
      InitialContext context = new InitialContext();
      Queue queue = (Queue)context.lookup(queueJndiName);
      XAQueueConnectionFactory xaQueueConnectionFactory = (XAQueueConnectionFactory) context.lookup(connectionFactoryJndiName);
      
      XAQueueConnection xaQueueConnection = null;
      XAQueueSession queueSession = null;
      MessageConsumer messageConsumer = null;
      try {
        xaQueueConnection = xaQueueConnectionFactory.createXAQueueConnection();
        xaQueueConnection.start();
        queueSession = xaQueueConnection.createXAQueueSession();
        messageConsumer = queueSession.createConsumer(queue);
        Message message = messageConsumer.receive(timeout);
        if (message==null) {
          throw new AssertionFailedError("no message on queue "+queueJndiName);
        }
        return message;
  
      } finally {
        try {
          messageConsumer.close();
        } catch (Exception e) {
          e.printStackTrace();
        }
        try {
          queueSession.close();
        } catch (Exception e) {
          e.printStackTrace();
        }
        try {
          xaQueueConnection.close();
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    } catch (RuntimeException e) {
      throw e;
    } catch (Exception e) {
      throw new JbpmException("couldn't receive message from queue: "+e.getMessage(), e);
    }
  }

  public static Message consumeMessageFromQueue(String connectionFactoryJndiName, String queueJndiName, long timeout, boolean transacted, int acknowledgeMode) {
    try {
      InitialContext context = new InitialContext();
      QueueConnectionFactory queueConnectionFactory = (QueueConnectionFactory) context.lookup(connectionFactoryJndiName);
      Queue queue = (Queue)context.lookup(queueJndiName);
      
      QueueConnection queueConnection = null;
      QueueSession queueSession = null;
      MessageConsumer messageConsumer = null;
      
      try {
        queueConnection = queueConnectionFactory.createQueueConnection();
        queueConnection.start();
        queueSession = queueConnection.createQueueSession(transacted, acknowledgeMode);
        messageConsumer = queueSession.createConsumer(queue);
        Message message = messageConsumer.receive(timeout);
        if (message==null) {
          throw new AssertionFailedError("no message on queue "+queueJndiName);
        }
        if (transacted) {
          queueSession.commit();
        }
        return message;
  
      } finally {
        try {
          messageConsumer.close();
        } catch (Exception e) {
          e.printStackTrace();
        }
        try {
          queueSession.close();
        } catch (Exception e) {
          e.printStackTrace();
        }
        try {
          queueConnection.close();
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    } catch (RuntimeException e) {
      throw e;
    } catch (Exception e) {
      throw new JbpmException("couldn't receive message from queue: "+e.getMessage(), e);
    }
  }

  public static void jmsAssertQueueEmptyXA(String connectionFactoryJndiName, String queueJndiName, long timeout) {
    try {
      consumeMessageFromQueueXA(connectionFactoryJndiName, queueJndiName, timeout);
    } catch (AssertionFailedError e) {
      if (e.getMessage().startsWith("no message on queue")) {
        return;
      }
    }
    throw new AssertionFailedError("message available on queue "+queueJndiName);
  }

  public static void jmsAssertQueueEmpty(String connectionFactoryJndiName, String queueJndiName, long timeout, boolean transacted, int acknowledgeMode) {
    try {
      consumeMessageFromQueue(connectionFactoryJndiName, queueJndiName, timeout, transacted, acknowledgeMode);
    } catch (AssertionFailedError e) {
      if (e.getMessage().startsWith("no message on queue")) {
        return;
      }
    }
    throw new AssertionFailedError("message available on queue "+queueJndiName);
  }
}
