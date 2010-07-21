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
package org.jbpm.test.load.async;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.hibernate.Session;

import org.jbpm.api.cmd.Environment;
import org.jbpm.api.cmd.VoidCommand;
import org.jbpm.api.history.HistoryComment;
import org.jbpm.internal.log.Log;
import org.jbpm.pvm.internal.history.model.HistoryCommentImpl;
import org.jbpm.pvm.internal.job.CommandMessage;
import org.jbpm.pvm.internal.session.MessageSession;
import org.jbpm.pvm.internal.util.CollectionUtil;
import org.jbpm.test.JbpmTestCase;

/**
 * @author Tom Baeyens
 */
public class FailOnceMessageTest extends JbpmTestCase {

  private static final Log log = Log.getLog(FailOnceMessageTest.class.getName());

  static final long nbrOfTestMessages = 50;
  static final List<Integer> failOnceMessageIds = Collections.synchronizedList(new ArrayList<Integer>());

  public void testFailOnceMessages() {
    insertFailOnceTestMessages();
    waitTillNoMoreMessages();

    for (int i = 0; i < nbrOfTestMessages; i++) {
      assertTrue("message " + i + " is not failed once: " + failOnceMessageIds, failOnceMessageIds.contains(i));
    }
    assertEquals(nbrOfTestMessages, failOnceMessageIds.size());

    log.debug("==== all messages processed, now checking if all messages have arrived exactly once ====");

    processEngine.execute(new VoidCommand() {

      private static final long serialVersionUID = 1L;

      @Override
      protected void executeVoid(Environment environment) throws Exception {
        Session session = environment.get(Session.class);
        List<?> comments = session.createCriteria(HistoryCommentImpl.class).list();

        for (HistoryComment comment : CollectionUtil.checkList(comments, HistoryComment.class)) {
          log.debug("retrieved message: " + comment.getMessage());
          Integer messageId = new Integer(comment.getMessage());
          assertTrue("message " + messageId + " committed twice", failOnceMessageIds.remove(messageId));
          // make sure the db stays clean
          session.delete(comment);
        }

        assertTrue("not all messages made a successful commit: " + failOnceMessageIds, failOnceMessageIds.isEmpty());
      }
    });
  }

  private static class InsertFailOnceTestMsgCmd extends VoidCommand {

    final int messageId;
    private static final long serialVersionUID = 1L;

    InsertFailOnceTestMsgCmd(int messageId) {
      this.messageId = messageId;
    }
    public void executeVoid(Environment environment) throws Exception {
      CommandMessage commandMessage = FailOnceTestCommand.createMessage(messageId);
      environment.get(MessageSession.class).send(commandMessage);
    }
  }

  void insertFailOnceTestMessages() {
    for (int i = 0; i < nbrOfTestMessages; i++) {
      processEngine.execute(new InsertFailOnceTestMsgCmd(i));
    }
  }
}
