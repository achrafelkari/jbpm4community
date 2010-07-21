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
package org.jbpm.test.load.messages;

import java.util.BitSet;
import java.util.List;

import org.hibernate.Session;

import org.jbpm.api.cmd.Command;
import org.jbpm.api.cmd.Environment;
import org.jbpm.api.history.HistoryComment;
import org.jbpm.internal.log.Log;
import org.jbpm.pvm.internal.cmd.CompositeCmd;
import org.jbpm.pvm.internal.cmd.SendMessageCmd;
import org.jbpm.pvm.internal.history.model.HistoryCommentImpl;
import org.jbpm.pvm.internal.job.CommandMessage;
import org.jbpm.pvm.internal.util.CollectionUtil;
import org.jbpm.test.load.LoadTestCase;

/**
 * @author Tom Baeyens
 * @author Guillaume Porcher
 */
public class MessageProcessingTest extends LoadTestCase {

  private static final Log log = Log.getLog(MessageProcessingTest.class.getName());

  // nbrOfTestMessages must be divisible by insertGroupSize
  static final long insertGroupSize = 100;
  static final long nbrOfTestMessages = insertGroupSize * 10;
  static int commentsAdded = 0;

  public static synchronized void commentAdded() {
    commentsAdded++;
  }

  protected void logColumnTitles() {
    logFileWriter.println("Used Memory\tProcessed Messages\tTime");
  }

  public void logStatus() {
    String measuredTime = getMeasuredTime();
    logFileWriter.println(getUsedMemory() + "\t" + commentsAdded + "\t" + measuredTime);
    logFileWriter.flush();
    log.info(commentsAdded + " msgs in " + measuredTime);
  }

  public void testMessageProcessing() throws Exception {
    int messageIndex = 0;
    // insert ${nbrOfTestMessages} messages...
    for (int i = 0; i < nbrOfTestMessages; i += insertGroupSize) {
      CompositeCmd compositeCmd = new CompositeCmd();
      // ...in groups of ${insertGroupSize}
      for (int j = 0; j < insertGroupSize; j++) {
        String messageText = Integer.toString(messageIndex++);
        CommandMessage commandMessage = new CommandMessage(new AddCommentCmd(messageText));
        SendMessageCmd sendMessageCmd = new SendMessageCmd(commandMessage);
        compositeCmd.addCommand(sendMessageCmd);
      }
      processEngine.execute(compositeCmd);
      log.info("added " + messageIndex + " messages");
    }

    startMeasuringTime();
    // wait till all messages are processed
    waitTillNoMoreMessages();
    stopMeasuringTime();

    log.info("processing " + nbrOfTestMessages + " messages took " + getMeasuredTime());

    BitSet processedMessageNumbers = processEngine.execute(new Command<BitSet>() {

      private static final long serialVersionUID = 1L;

      public BitSet execute(Environment environment) {
        BitSet processedMessageNumbers = new BitSet();
        Session session = environment.get(Session.class);
        List<?> comments = session.createCriteria(HistoryCommentImpl.class).list();
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

}
