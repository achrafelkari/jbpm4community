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
import org.jbpm.api.cmd.Command;
import org.jbpm.api.cmd.Environment;
import org.jbpm.api.cmd.VoidCommand;
import org.jbpm.api.history.HistoryComment;
import org.jbpm.internal.log.Log;
import org.jbpm.pvm.internal.history.model.HistoryDetailImpl;
import org.jbpm.pvm.internal.job.CommandMessage;
import org.jbpm.pvm.internal.session.MessageSession;
import org.jbpm.pvm.internal.util.CollectionUtil;

/**
 * @author Tom Baeyens
 */
public class FailOnceMessageTest extends JobExecutorTestCase {

  private static final Log log = Log.getLog(FailOnceMessageTest.class.getName());
    
  static long nbrOfTestMessages = 50;
  
  static List<Integer> failOnceMessageIds = Collections.synchronizedList(new ArrayList<Integer>());
  
  public void testFailOnceMessages() {
    failOnceMessageIds.clear();

    jobExecutor.start();
    try {
      insertFailOnceTestMessages();
      waitTillNoMoreMessages();

    } finally {
      log.debug("stopping job executor");
      jobExecutor.stop(true);
    }

    for (int i = 0; i < nbrOfTestMessages; i++) {
      assertTrue("message " + i + " is not failed once: " + failOnceMessageIds, failOnceMessageIds.contains(i));
    }
    assertEquals(nbrOfTestMessages, failOnceMessageIds.size());
    
    log.debug("==== all messages processed, now checking if all messages have arrived exactly once ====");

    commandService.execute(new VoidCommand() {
      private static final long serialVersionUID = 1L;

      @Override
      protected void executeVoid(Environment environment) throws Exception {
        Session session = environment.get(Session.class);
        List<?> comments = session.createCriteria(HistoryDetailImpl.class).list();
        
        for (HistoryComment comment : CollectionUtil.checkList(comments, HistoryComment.class)) {
          log.debug("retrieved message: "+comment.getMessage());
          Integer messageId = new Integer(comment.getMessage());
          assertTrue("message " + messageId + " committed twice", failOnceMessageIds.remove(messageId));
        }

        assertTrue("not all messages made a successful commit: " + failOnceMessageIds, failOnceMessageIds.isEmpty());
      }
    });
  }

  public static class InsertFailOnceTestMsgCmd implements Command<Object> {
    private static final long serialVersionUID = 1L;
    int i;
    public InsertFailOnceTestMsgCmd(int i) {
      this.i = i;
    }
    public Object execute(Environment environment) throws Exception {
      MessageSession messageSession = environment.get(MessageSession.class);
      CommandMessage commandMessage = FailOnceTestCommand.createMessage(i);
      messageSession.send(commandMessage);
      return null;
    }
  }

  void insertFailOnceTestMessages() {
    for (int i = 0; i < nbrOfTestMessages; i++) {
      commandService.execute(new InsertFailOnceTestMsgCmd(i));
    }
  }

}
