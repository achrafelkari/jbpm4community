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

import java.util.List;

import org.jbpm.api.JbpmException;
import org.jbpm.api.cmd.Environment;
import org.jbpm.api.cmd.VoidCommand;
import org.jbpm.api.job.Job;
import org.jbpm.pvm.internal.job.CommandMessage;
import org.jbpm.pvm.internal.session.MessageSession;

/**
 * @author Tom Baeyens
 */
public class FailingMessageTest extends JobExecutorTestCase {

  
  public void testFailedMessageProcessing() {
    jobExecutor.start();
    try {
      commandService.execute(new VoidCommand() {
        private static final long serialVersionUID = 1L;

        @Override
        protected void executeVoid(Environment environment) throws Exception {
          MessageSession messageSession = environment.get(MessageSession.class);
          CommandMessage commandMessage = FailingTestCommand.createMessage();
          messageSession.send(commandMessage);
        }
      });

      waitTillNoMoreMessages();

    } finally {
      jobExecutor.stop(true);
    }

    commandService.execute(new VoidCommand() {
      private static final long serialVersionUID = 1L;

      @Override
      public void executeVoid(Environment environment) throws Exception {
        List<Job> deadJobs = null;
        throw new JbpmException("todo get the jobs with exception");

//        Session session = environment.get(Session.class);
//        List commands = session.createQuery("from " + HistoryCommentImpl.class.getName()).list();
//        assertTrue("command insertion should have been rolled back", commands.isEmpty());
      }
    });
  }
}
