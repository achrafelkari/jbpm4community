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

import java.util.BitSet;
import java.util.List;

import org.hibernate.Session;

import org.jbpm.api.cmd.Command;
import org.jbpm.api.cmd.Environment;
import org.jbpm.api.cmd.VoidCommand;
import org.jbpm.api.history.HistoryComment;
import org.jbpm.pvm.internal.history.model.HistoryDetailImpl;
import org.jbpm.pvm.internal.job.CommandMessage;
import org.jbpm.pvm.internal.session.MessageSession;
import org.jbpm.pvm.internal.util.CollectionUtil;
import org.jbpm.test.JbpmTestCase;

/**
 * @author Tom Baeyens
 */
public class NormalMessageTest extends JbpmTestCase {

  static long nbrOfTestMessages = 100;

  public void testNormalMessageProcessing() {
    // insert ${nbrOfTestMessages} messages...
    for (int i = 0; i < nbrOfTestMessages; i++) {
      processEngine.execute(new InsertNormalMessageCmd(i));
    }
    // wait till all messages are processed
    waitTillNoMoreMessages();

    BitSet processedMessageNumbers = processEngine.execute(new Command<BitSet>() {

      private static final long serialVersionUID = 1L;

      public BitSet execute(Environment environment) {
        BitSet processedMessageNumbers = new BitSet();
        Session session = environment.get(Session.class);
        List<?> comments = session.createCriteria(HistoryDetailImpl.class).list();
        for (HistoryComment comment : CollectionUtil.checkList(comments, HistoryComment.class)) {
          int processedMessageNumber = Integer.parseInt(comment.getMessage());
          processedMessageNumbers.set(processedMessageNumber);
          // make sure the db stays clean
          session.delete(comment);
        }
        return processedMessageNumbers;
      }
    });

    for (int i = 0; i < nbrOfTestMessages; i++) {
      assertTrue("message " + i + " is not processed: " + processedMessageNumbers, processedMessageNumbers.get(i));
    }
  }

  public static class InsertNormalMessageCmd extends VoidCommand {

    final int messageId;
    private static final long serialVersionUID = 1L;

    public InsertNormalMessageCmd(int messageId) {
      this.messageId = messageId;
    }
    protected void executeVoid(Environment environment) throws Exception {
      MessageSession messageSession = environment.get(MessageSession.class);
      CommandMessage commandMessage = NormalMessageCommand.createMessage(messageId);
      messageSession.send(commandMessage);
    }
  }

}
