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
package org.jbpm.enterprise.internal.ejb;

import java.io.Serializable;

import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.MessageDrivenBean;
import javax.ejb.MessageDrivenContext;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import javax.jms.Session;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NameNotFoundException;
import javax.naming.NamingException;

import org.jbpm.api.cmd.Command;
import org.jbpm.api.job.Job;
import org.jbpm.internal.log.Log;
import org.jbpm.pvm.internal.cmd.ExecuteJobCmd;

/**
 * Message driven command receiver.
 * 
 * <h3>Configuration</h3>
 * 
 * <p>
 * The command receiver listens for messages on the destination established
 * during deployment. Upon delivery, this bean extracts a
 * {@linkplain Command command} from the message according to the format
 * described in the {@link #onMessage(Message) onMessage} method.
 * 
 * <p>
 * If the message does not match the expected format, it is forwarded to the
 * message destination referenced by <code>jms/DeadLetterQueue</code>. No
 * further processing is done on the message.
 * </p>
 * 
 * <p>
 * After extraction, the receiver dispatches the command to the
 * {@linkplain LocalCommandExecutor local command executor} specified by the EJB
 * reference <code>ejb/LocalCommandExecutor</code>.
 * </p>
 * 
 * <p>
 * In case the incoming message includes a <code>replyTo</code> destination,
 * the result of the command execution is wrapped into a message and sent there.
 * The resource manager connection factory reference
 * <code>jms/CommandConnectionFactory</code> specifies the factory used to
 * obtain JMS connections.
 * </p>
 * 
 * @author Jim Rigsbee
 * @author Tom Baeyens
 * @author Alejandro Guizar
 */
public class CommandReceiverMDB implements MessageDrivenBean, MessageListener {

  private MessageDrivenContext messageDrivenContext;
  private LocalCommandExecutor commandExecutor;
  private Connection jmsConnection;
  private Destination deadLetterQueue;

  private static final long serialVersionUID = 1L;

  private static final Log log = Log.getLog(CommandReceiverMDB.class.getName());

  /**
   * Processes a command message.
   * 
   * <h3>Message format</h3>
   * 
   * <p>
   * The incoming messages must conform to one of the following formats.
   * </p>
   * 
   * <ul>
   * <li><strong>Command object</strong>. The body of the message is a Java
   * {@linkplain ObjectMessage object} that implements the {@link Command}
   * interface. The header properties of the message, if any, are ignored.</li>
   * <li><strong>Job reference</strong>. The message contains a header
   * property called <code>jobId</code> of type <code>long</code> which
   * references a saved {@linkplain Job job} that is outstanding for execution.
   * The body of the message, if any, is ignored.</li>
   * </ul>
   */
  public void onMessage(Message message) {
    try {
      // extract command from message
      Command<?> command = extractCommand(message);
      if (command == null) {
        discard(message);
        return;
      }
      // execute command via local command executor bean
      Object result = commandExecutor.execute(command);
      // send a response back if a "reply to" destination is set
      Destination replyTo = message.getJMSReplyTo();
      if (replyTo != null && (result instanceof Serializable || result == null)) {
        sendResult((Serializable) result, replyTo, message.getJMSMessageID());
      }
    }
    catch (JMSException e) {
      throw new EJBException("could not process message " + message, e);
    }
  }

  private static Command<?> extractCommand(Message message) throws JMSException {
    Command<?> command = null;
    // messages can contain serialized commands
    if (message instanceof ObjectMessage) {
      ObjectMessage objectMessage = (ObjectMessage) message;
      Serializable body = objectMessage.getObject();
      log.debug("received object message with body " + body);
      if (body instanceof Command) {
        command = (Command<?>) body;
      }
      else {
        log.info("WARNING: ignoring object message, body is not a command: " + message);
      }
    }
    // messages can also contain a jobId property
    else if (message.getObjectProperty("jobId") != null) {
      String jobId = message.getStringProperty("jobId");
      log.debug("received message with job id " + jobId);
      // create a command with the given jobId
      command = new ExecuteJobCmd(jobId);
    }
    else {
      log.info("WARNING: ignoring message, unknown format: " + message);
    }
    return command;
  }

  private void discard(Message message) throws JMSException {
    // if a dead letter queue is configured, send the message there
    if (deadLetterQueue != null) {
      Session jmsSession = createSession();
      try {
        jmsSession.createProducer(deadLetterQueue).send(message);
      }
      finally {
        jmsSession.close();
      }
    }
    else {
      // reject the message
      messageDrivenContext.setRollbackOnly();
    }
  }

  private Session createSession() throws JMSException {
    /*
     * if the connection supports xa, the session will be transacted, else the
     * session will auto acknowledge; in either case no explicit transaction
     * control must be performed - see ejb 2.1 section 17.3.5
     */
    return jmsConnection.createSession(false, Session.AUTO_ACKNOWLEDGE);
  }

  private void sendResult(Serializable result, Destination destination, String correlationId)
      throws JMSException {
    log.debug("sending result " + result + " to " + destination);
    Session jmsSession = createSession();
    try {
      Message resultMessage = jmsSession.createObjectMessage(result);
      resultMessage.setJMSCorrelationID(correlationId);
      jmsSession.createProducer(destination).send(resultMessage);
    }
    finally {
      jmsSession.close();
    }
  }

  public void setMessageDrivenContext(MessageDrivenContext messageDrivenContext) {
    this.messageDrivenContext = messageDrivenContext;
  }

  public void ejbCreate() {
    try {
      Context context = new InitialContext();
      LocalCommandExecutorHome commandExecutorHome = (LocalCommandExecutorHome) context.lookup("java:comp/env/ejb/LocalCommandExecutor");
      ConnectionFactory jmsConnectionFactory = (ConnectionFactory) context.lookup("java:comp/env/jms/JbpmConnectionFactory");
      try {
        deadLetterQueue = (Destination) context.lookup("java:comp/env/jms/DeadLetterQueue");
      }
      catch (NameNotFoundException e) {
        log.info("dead letter queue not specified, messages with unknown formats will be rejected");
      }
      context.close();

      commandExecutor = commandExecutorHome.create();
      jmsConnection = jmsConnectionFactory.createConnection();
    }
    catch (NamingException e) {
      throw new EJBException("error retrieving managed objects", e);
    }
    catch (CreateException e) {
      throw new EJBException("error creating command executor", e);
    }
    catch (JMSException e) {
      throw new EJBException("error creating jms connection", e);
    }
  }

  public void ejbRemove() {
    if (jmsConnection != null) {
      try {
        jmsConnection.close();
      }
      catch (JMSException e) {
        log.debug("could not close jms connection", e);
      }
      jmsConnection = null;
    }
    commandExecutor = null;
    messageDrivenContext = null;
  }
}
