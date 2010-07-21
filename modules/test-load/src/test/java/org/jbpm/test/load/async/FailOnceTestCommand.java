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

import org.jbpm.api.cmd.Command;
import org.jbpm.api.cmd.Environment;
import org.jbpm.api.history.HistoryComment;
import org.jbpm.internal.log.Log;
import org.jbpm.pvm.internal.history.model.HistoryCommentImpl;
import org.jbpm.pvm.internal.job.CommandMessage;
import org.jbpm.pvm.internal.session.DbSession;

/**
 * @author Tom Baeyens
 */
public class FailOnceTestCommand implements Command<Object> {

  private static final long serialVersionUID = 1L;
  private static final Log log = Log.getLog(FailOnceTestCommand.class.getName());
  
  int messageId;
  
  public FailOnceTestCommand() {
  }

  public static CommandMessage createMessage(int messageId) {
    FailOnceTestCommand command = new FailOnceTestCommand();
    command.messageId = messageId;
    return new CommandMessage(command);
  }

  public Object execute(Environment environment) throws Exception {
    DbSession dbSession = environment.get(DbSession.class);

    // this message execution should be rolled back
    HistoryComment comment = new HistoryCommentImpl(Integer.toString(messageId));
    dbSession.save(comment);

    if (!FailOnceMessageTest.failOnceMessageIds.contains(messageId)) {
      // registering the failed message in a non-transactional resource
      // so the messageId will still be added even after the transaction has rolled back
      log.debug("adding failonce message "+messageId);
      FailOnceMessageTest.failOnceMessageIds.add(messageId);
      
      throw new RuntimeException("failing once"); 
    }
    
    try {
      Thread.sleep(700);
    } catch (Exception e) {
      log.error(e.toString());
    }

    log.debug("message "+messageId+" now succeeds");

    return null;
  }
  
}
