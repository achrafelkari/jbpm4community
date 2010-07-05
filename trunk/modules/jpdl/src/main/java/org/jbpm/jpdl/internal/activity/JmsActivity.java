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
package org.jbpm.jpdl.internal.activity;

import java.io.Serializable;
import java.util.List;

import javax.jms.Destination;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.QueueSender;
import javax.jms.QueueSession;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.Topic;
import javax.jms.TopicConnection;
import javax.jms.TopicConnectionFactory;
import javax.jms.TopicPublisher;
import javax.jms.TopicSession;
import javax.jms.XAConnectionFactory;
import javax.jms.XAQueueConnection;
import javax.jms.XAQueueConnectionFactory;
import javax.jms.XAQueueSession;
import javax.jms.XATopicConnection;
import javax.jms.XATopicConnectionFactory;
import javax.jms.XATopicSession;
import javax.naming.InitialContext;

import org.jbpm.api.JbpmException;
import org.jbpm.api.model.OpenExecution;
import org.jbpm.internal.log.Log;
import org.jbpm.pvm.internal.el.Expression;
import org.jbpm.pvm.internal.model.ExecutionImpl;
import org.jbpm.pvm.internal.wire.Descriptor;
import org.jbpm.pvm.internal.wire.WireContext;
import org.jbpm.pvm.internal.wire.descriptor.MapDescriptor;

/**
 * @author Koen Aers
 * @author Tom Baeyens
 */
public class JmsActivity extends JpdlAutomaticActivity {

  private static final Log log = Log.getLog(JmsActivity.class.getName());

  private static final long serialVersionUID = 1L;

  protected String type = null;
  protected Expression textExpression = null;
  protected Expression objectExpression = null;
  protected MapDescriptor mapDescriptor = null;
  protected String connectionFactoryName = null;
  protected String destinationName = null;
  protected boolean transacted = true;
  protected int acknowledgeMode = Session.AUTO_ACKNOWLEDGE;

  public void perform(OpenExecution execution) {
    try {
      InitialContext initialContext = new InitialContext();

      Destination destination = (Destination) initialContext.lookup(destinationName);
      Object connectionFactory = initialContext.lookup(connectionFactoryName);

      if (connectionFactory instanceof XAConnectionFactory) {
        log.debug("connection factory '"+connectionFactoryName+"' is a XAConnectionFactory: using xa jms apis");
        if (destination instanceof Queue) {
          log.debug("destination '"+destinationName+"' is a Queue: using xa queue jms apis");
          XAQueueConnectionFactory xaQueueConnectionFactory = (XAQueueConnectionFactory) connectionFactory;
          sendToQueueXA((Queue) destination, xaQueueConnectionFactory, (ExecutionImpl) execution);
  
        } else if (destination instanceof Topic) {
          log.debug("destination '"+destinationName+"' is a Topic: using xa topic jms apis");
          XATopicConnectionFactory xaTopicConnectionFactory = (XATopicConnectionFactory) connectionFactory;
          sendToTopicXA((Topic) destination, xaTopicConnectionFactory, (ExecutionImpl) execution);
          
        } else {
          throw new JbpmException("invalid destination type for '"+destinationName+"': "+destination.getClass().getName());
        }

      } else { // non-XA
        log.debug("connection factory '"+connectionFactoryName+"' is a ConnectionFactory: using non-xa jms apis");
        if (destination instanceof Queue) {
          log.debug("destination '"+destinationName+"' is a Queue: using non-xa queue jms apis");
          QueueConnectionFactory queueConnectionFactory = (QueueConnectionFactory) connectionFactory;
          sendToQueue((Queue) destination, queueConnectionFactory, (ExecutionImpl) execution);
    
        } else if (destination instanceof Topic) {
          log.debug("destination '"+destinationName+"' is a Topic: using non-xa topic jms apis");
          TopicConnectionFactory topicConnectionFactory = (TopicConnectionFactory) connectionFactory;
          sendToTopic((Topic) destination, topicConnectionFactory, (ExecutionImpl) execution);
          
        } else {
          throw new JbpmException("invalid destination type for '"+destinationName+"': "+destination.getClass().getName());
        }
      }

    } catch (RuntimeException e) {
      log.error("couldn't send jms message: "+e.getMessage(), e);
      throw e;

    } catch (Exception e) {
      log.error("couldn't send jms message: "+e.getMessage(), e);
      throw new JbpmException("couldn't send jms message to queue"+e.getMessage(), e);
    }
  }

  protected void sendToQueueXA(Queue queue, XAQueueConnectionFactory xaQueueConnectionFactory, ExecutionImpl execution) throws Exception {
    XAQueueConnection xaQueueConnection = null;
    XAQueueSession xaQueueSession = null;
    MessageProducer messageProducer = null;

    try {
      xaQueueConnection = xaQueueConnectionFactory.createXAQueueConnection();
      xaQueueSession = xaQueueConnection.createXAQueueSession();
      messageProducer = xaQueueSession.createProducer(queue);
      Message message = createMessage(xaQueueSession, execution);
      messageProducer.send(message);

    } finally {
      try {
        messageProducer.close();
      } catch (Exception e) {
        e.printStackTrace();
      }
      try {
        xaQueueSession.close();
      } catch (Exception e) {
        e.printStackTrace();
      }
      try {
        xaQueueConnection.close();
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }

  protected void sendToTopicXA(Topic topic, XATopicConnectionFactory xaTopicConnectionFactory, ExecutionImpl execution) throws Exception {
    XATopicConnection xaTopicConnection = null;
    XATopicSession xaTopicSession = null;
    MessageProducer messageProducer = null;
    
    try {
      xaTopicConnection = xaTopicConnectionFactory.createXATopicConnection();
      xaTopicSession = xaTopicConnection.createXATopicSession();
      messageProducer = xaTopicSession.createProducer(topic);
      Message message = createMessage(xaTopicSession, execution);
      messageProducer.send(message);

    } finally {
      try {
        messageProducer.close();
      } catch (Exception e) {
        e.printStackTrace();
      }
      try {
        xaTopicSession.close();
      } catch (Exception e) {
        e.printStackTrace();
      }
      try {
        xaTopicConnection.close();
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }

  protected void sendToQueue(Queue queue, QueueConnectionFactory queueConnectionFactory, ExecutionImpl execution) throws Exception {
    QueueConnection queueConnection = null;
    QueueSession queueSession = null;
    QueueSender queueSender = null;

    try {
      queueConnection = queueConnectionFactory.createQueueConnection();
      queueSession = queueConnection.createQueueSession(transacted, acknowledgeMode);
      queueSender = queueSession.createSender(queue);
      Message message = createMessage(queueSession, execution);
      queueSender.send(message);
      if (transacted) {
        queueSession.commit();
      }

    } finally {
      try {
        queueSender.close();
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
  }

  protected void sendToTopic(Topic topic, TopicConnectionFactory topicConnectionFactory, ExecutionImpl execution) throws Exception {
    TopicConnection topicConnection = null;
    TopicSession topicSession = null;
    TopicPublisher topicPublisher = null;
    
    try {
      topicConnection = topicConnectionFactory.createTopicConnection();
      topicSession = topicConnection.createTopicSession(transacted, acknowledgeMode);
      topicPublisher = topicSession.createPublisher(topic);
      Message message = createMessage(topicSession, execution);
      topicPublisher.send(message);
      if (transacted) {
        topicSession.commit();
      }

    } finally {
      try {
        topicPublisher.close();
      } catch (Exception e) {
        e.printStackTrace();
      }
      try {
        topicSession.close();
      } catch (Exception e) {
        e.printStackTrace();
      }
      try {
        topicConnection.close();
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }

  protected Message createMessage(Session session, ExecutionImpl execution) throws Exception {
    if ("text".equals(type)) {
      return createTextMessage(session, execution);
    } else if ("object".equals(type)) {
      return createObjectMessage(session, execution);
    } else if ("map".equals(type)) {
      return createMapMessage(session, execution);
    }
    throw new JbpmException("no type configured in jms activity");
  }

  private MapMessage createMapMessage(Session session, ExecutionImpl execution) throws Exception {
    MapMessage result = session.createMapMessage();
    if (mapDescriptor != null) {
      List<Descriptor> keyDescriptors = mapDescriptor.getKeyDescriptors();
      List<Descriptor> valueDescriptors = mapDescriptor.getValueDescriptors();
      WireContext wireContext = new WireContext();
      wireContext.setScopeInstance(execution);
      for (int i = 0; i < keyDescriptors.size(); i++) {
        String key = (String) wireContext.create(keyDescriptors.get(i), false);
        Object value = wireContext.create(valueDescriptors.get(i), false);
        result.setObject(key, value);
      }
    }
    return result;
  }

  private TextMessage createTextMessage(Session session, ExecutionImpl execution) throws Exception {
    Object value = textExpression.evaluate(execution);
    if (value!=null) {
      return session.createTextMessage(value.toString());
    }
    throw new JbpmException("null value for expression '"+textExpression+"' in jms activity"); 
  }

  private ObjectMessage createObjectMessage(Session session, ExecutionImpl execution) throws Exception {
    Object object = objectExpression.evaluate(execution);
    if ( (object!=null)
         && !(object instanceof Serializable)
       ) {
      throw new JbpmException("can't send jms message: creation of object message expression '"+objectExpression+"' must be done with serializable: "+object);
    }
    return session.createObjectMessage((Serializable) object);
  }

  public void setType(String type) {
    this.type = type;
  }
  public void setTextExpression(Expression textExpression) {
    this.textExpression = textExpression;
  }
  public void setObjectExpression(Expression objectExpression) {
    this.objectExpression = objectExpression;
  }
  public void setMapDescriptor(MapDescriptor mapDescriptor) {
    this.mapDescriptor = mapDescriptor;
  }
  public void setConnectionFactoryName(String connectionFactoryName) {
    this.connectionFactoryName = connectionFactoryName;
  }
  public void setDestinationName(String destinationName) {
    this.destinationName = destinationName;
  }
  public boolean isTransacted() {
    return transacted;
  }
  public void setTransacted(boolean transacted) {
    this.transacted = transacted;
  }
  public int getAcknowledgeMode() {
    return acknowledgeMode;
  }
  public void setAcknowledgeMode(int acknowledgeMode) {
    this.acknowledgeMode = acknowledgeMode;
  }
}
