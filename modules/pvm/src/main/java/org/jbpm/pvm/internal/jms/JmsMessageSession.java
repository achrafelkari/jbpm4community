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
package org.jbpm.pvm.internal.jms;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Session;

import org.jbpm.api.JbpmException;
import org.jbpm.api.job.Message;
import org.jbpm.pvm.internal.session.DbSession;
import org.jbpm.pvm.internal.session.MessageSession;

/** message service implementation based on JMS.
 * @author Tom Baeyens
 */
public class JmsMessageSession implements MessageSession {

  private static final long serialVersionUID = 1L;
  
  /* injected */
  DbSession dbSession;

  /* injected */
  Session jmsSession;

  /* injected */
  Destination jmsDestination;

  // Connection connection;
  
  // If you use an XA connection factory in JBoss, the parameters will be ignored.  It will always take part in the global JTA transaction.
  // If you use a non XQ connection factory, the first parameter specifies wether you want to have all message productions and 
  // consumptions as part of one transaction (TRUE) or wether you want all productions and consumptions to be instantanious (FALSE)
  // Of course, we never want messages to be received before the current jbpm transaction commits so we just set it to true.
  // Session session = connection.createSession(true, Session.AUTO_ACKNOWLEDGE);
  

  public void send(Message message) {
    try {
      
      dbSession.save(message);
      
      javax.jms.Message jmsMessage = jmsSession.createMessage();
      MessageProducer messageProducer = jmsSession.createProducer(jmsDestination);
      try {
        messageProducer.send(jmsMessage);
      } catch (Exception e) {
        throw new JbpmException("couldn't send jms message: "+e.getMessage(), e);
      } finally {
        messageProducer.close();
      }

      jmsMessage.setStringProperty("jobId", message.getId());

      /*
      if (jobImpl.getToken()!=null) {
        message.setLongProperty("tokenId", jobImpl.getToken().getId());
      }
      if (jobImpl.getProcessInstance()!=null) {
        message.setLongProperty("processInstanceId", jobImpl.getProcessInstance().getId());
      }
      if (jobImpl.getTaskInstance()!=null) {
        message.setLongProperty("taskInstanceId", jobImpl.getTaskInstance().getId());
      }
      */
      
    } catch (JMSException e) {
      throw new JbpmException("couldn't send jms message", e);
    }
  }
  
  public void close() {
  }
}
