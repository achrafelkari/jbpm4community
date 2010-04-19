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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.Topic;
import javax.jms.XATopicConnection;
import javax.jms.XATopicConnectionFactory;
import javax.jms.XATopicSession;
import javax.naming.InitialContext;

import junit.framework.TestCase;

import org.jbpm.internal.log.Log;


/**
 * @author Tom Baeyens
 */
public class JmsXATopicListener implements JmsTopicListener {

  private static Log log = Log.getLog(JmsXATopicListener.class.getName());

  String topicJndiName = null;
  String connectionFactoryJndiName = null;
  Topic topic = null;
  XATopicConnectionFactory topicConnectionFactory = null;
  XATopicConnection topicConnection = null;
  XATopicSession topicSession = null;
  MessageConsumer messageConsumer = null;
  
  List<Message> messages = null;

  JmsXATopicListener(String connectionFactoryJndiName, String topicJndiName) {
    this.connectionFactoryJndiName = connectionFactoryJndiName;
    this.topicJndiName = topicJndiName;
    start();
  }

  void start() {
    try {
      messages = Collections.synchronizedList(new ArrayList<Message>());

      InitialContext context = new InitialContext();
      topicConnectionFactory = (XATopicConnectionFactory) context.lookup(connectionFactoryJndiName);
      topic = (Topic) context.lookup(topicJndiName);
      topicConnection = topicConnectionFactory.createXATopicConnection();
      topicSession = topicConnection.createXATopicSession();
      messageConsumer = topicSession.createConsumer(topic);
      messageConsumer.setMessageListener(new Listener());

      topicConnection.start();

    } catch (Exception e) {
      stop();
      throw new RuntimeException("couldn't subscribe message listener to topic '"+topicJndiName+"': "+e.getMessage(), e);
    }
  }

  private class Listener implements MessageListener {
    public void onMessage(Message message) {
      messages.add(message);
    }
  }

  public void stop() {
    if (topicConnection!=null) {
      try {
        topicConnection.stop();
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    if (messageConsumer!=null) {
      try {
        messageConsumer.close();
      } catch (Exception e) {
        e.printStackTrace();
      }
      messageConsumer = null;
    }
    if (topicSession!=null) {
      try {
        topicSession.close();
      } catch (Exception e) {
        e.printStackTrace();
      }
      topicSession = null;
    }
    if (topicConnection!=null) {
      try {
        topicConnection.close();
      } catch (Exception e) {
        e.printStackTrace();
      }
      topicConnection = null;
    }
  }

  public Message getNextMessage(long timeout) {
    long start = System.currentTimeMillis();
    while (true) {
      if (!messages.isEmpty()) {
        return messages.remove(0);
      }
      if (System.currentTimeMillis()-start > timeout) {
        TestCase.fail("no message for topic "+topicJndiName);
      }
      try {
        Thread.sleep(200);
      } catch (InterruptedException e) {
        log.info("waiting for next message got interrupted");
      }
    }
  }
}
