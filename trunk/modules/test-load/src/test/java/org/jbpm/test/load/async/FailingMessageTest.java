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

import org.hibernate.Session;

import org.jbpm.api.cmd.Environment;
import org.jbpm.api.cmd.VoidCommand;
import org.jbpm.pvm.internal.history.model.HistoryCommentImpl;
import org.jbpm.pvm.internal.job.CommandMessage;
import org.jbpm.pvm.internal.job.JobImpl;
import org.jbpm.pvm.internal.session.MessageSession;
import org.jbpm.test.JbpmTestCase;

/**
 * @author Tom Baeyens
 */
public class FailingMessageTest extends JbpmTestCase {

  public void testFailedMessageProcessing() {
    processEngine.execute(new VoidCommand() {

      private static final long serialVersionUID = 1L;

      @Override
      protected void executeVoid(Environment environment) throws Exception {
        MessageSession messageSession = environment.get(MessageSession.class);
        CommandMessage commandMessage = FailingTestCommand.createMessage();
        messageSession.send(commandMessage);
      }
    });

    waitTillNoMoreMessages();

    processEngine.execute(new VoidCommand() {

      private static final long serialVersionUID = 1L;

      @Override
      public void executeVoid(Environment environment) throws Exception {
        Session session = environment.get(Session.class);

        List<?> deadJobs = session.createCriteria(JobImpl.class).list();
        assertEquals(1, deadJobs.size());
        session.delete(deadJobs.get(0));

        List<?> comments = session.createCriteria(HistoryCommentImpl.class).list();
        assertEquals("command insertion should have been rolled back", 0, comments.size());
      }
    });
  }
}
