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
 */package org.jbpm.pvm.internal.jms;

import java.util.Enumeration;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.ObjectMessage;
import javax.jms.Queue;
import javax.jms.Topic;

import org.jbpm.internal.log.Log;

/** message printing and destination name extraction.
 * @author Tom Baeyens, Jim Rigsbee
 */
public class JmsMessageUtil {

  private static final Log log = Log.getLog(JmsMessageUtil.class.getName());

  public static String dump(Message message) {
  	StringBuilder dump = new StringBuilder();

    dump.append("JMS MessageImpl Dump\n").append("MessageImpl type is " + message.getClass().getName() + "\n");

    try {
      if (message instanceof ObjectMessage) {
        dump.append("MessageImpl object type is " + ((ObjectMessage) message).getObject().getClass().getName() + "\n");
      }
      dump.append("Reply to " + getDestinationName(message.getJMSReplyTo()) + "\n");
      Enumeration<?> e = message.getPropertyNames();
      while (e.hasMoreElements()) {
        String propertyName = (String) e.nextElement();
        Object property = message.getObjectProperty(propertyName);
        dump.append("Property " + propertyName + " value " + property.toString() + "\n");
      }
    } catch (JMSException j) {
      log.error("JMS exception while dumping message", j);
    }

    return dump.toString();
  }

  public static String getDestinationName(Destination d) {
    try {
      if (d instanceof Queue)
        return ((Queue) d).getQueueName();
      else if (d instanceof Topic)
        return ((Topic) d).getTopicName();
    } catch (JMSException j) {
      log.error("JMS exception while getting destination name", j);
    };

    return null;
  }
}
