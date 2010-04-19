/**
 * Copyright (C) 2007  Bull S. A. S.
 * Bull, Rue Jean Jaures, B.P.68, 78340, Les Clayes-sous-Bois
 * This library is free software; you can redistribute it and/or modify it under the terms
 * of the GNU Lesser General Public License as published by the Free Software Foundation
 * version 2.1 of the License.
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth
 * Floor, Boston, MA  02110-1301, USA.
 **/
package org.jbpm.test.load;

import java.util.ArrayList;
import java.util.List;

import org.jbpm.cmd.Command;
import org.jbpm.cmd.CommandService;
import org.jbpm.env.Environment;
import org.jbpm.job.Message;
import org.jbpm.internal.log.Log;
import org.jbpm.pvm.internal.job.CommandMessage;
import org.jbpm.pvm.internal.jobexecutor.JobExecutor;
import org.jbpm.pvm.internal.wire.descriptor.ObjectDescriptor;
import org.jbpm.session.MessageSession;
import org.jbpm.session.PvmDbSession;
import org.jbpm.test.DbTestCase;


/**
 * This class contains job executor tests which requires to have a read commited isolation level. 
 * 
 * @author Guillaume Porcher
 *
 */
public class JobExecutorIsolationDbTest extends DbTestCase {
  
  static int jobExecutorTimeoutMillis = 500;
  static int checkInterval = 400;
  
  public static class SimpleTestCommand implements Command<Object> {
    private static final Log log = Log.getLog(SimpleTestCommand.class.getName());

    public Object execute(Environment environment) throws Exception {
      log.debug("command executed !");
      return null;
    }
  }
  
  public void testInsertMessage() {
    System.out.println("FIXME: JBPM-1769 fix db isolation test");
  }
  
  
  /*
   * Basic test that only shows a simple situation in which we need to have a read commited isolation level.
   */
  
  // FIX rename test method back to testInsertMessage 
  public void dontTestInsertMessage() throws InterruptedException {
    JobExecutorTest.processedMessageIds = new ArrayList<Integer>();
    JobExecutor jobExecutor = processEngine.get(JobExecutor.class);
    jobExecutor.setIdleInterval(jobExecutorTimeoutMillis);
    jobExecutor.start();
    try {
      processEngine.get(CommandService.class).execute(new Command<Object>() {
        public Object execute(Environment environment) throws Exception {
          MessageSession messageSession = environment.get(MessageSession.class);
          CommandMessage commandMessage = new CommandMessage(new ObjectDescriptor(SimpleTestCommand.class));
          messageSession.send(commandMessage);
          List<Message> messages = environment.get(PvmDbSession.class).findMessages(0, 10);
          assertNotNull(messages);
          assertEquals(1, messages.size());
          Thread.sleep(jobExecutorTimeoutMillis * 2);
          messages = environment.get(PvmDbSession.class).findMessages(0, 10);
          assertNotNull(messages);
          assertEquals("Job has been executed before the transaction is committed !!", 1, messages.size());
          return null;
        }
      });
      Thread.sleep(jobExecutorTimeoutMillis * 2);
    } finally {
      jobExecutor.stop(true);
    }
  }
}
