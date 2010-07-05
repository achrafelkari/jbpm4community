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

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import org.hibernate.Session;

import org.jbpm.api.Execution;
import org.jbpm.api.cmd.Environment;
import org.jbpm.api.cmd.VoidCommand;
import org.jbpm.pvm.internal.job.CommandMessage;
import org.jbpm.pvm.internal.model.ExecutionImpl;
import org.jbpm.pvm.internal.wire.descriptor.ObjectDescriptor;
import org.jbpm.pvm.internal.wire.descriptor.StringDescriptor;

/**
 * @author Tom Baeyens
 */
public class ExclusiveTestCommand extends VoidCommand {

  private static final long serialVersionUID = 1L;
  static Random random = new Random();
  
  String executionId;
  
  public ExclusiveTestCommand() {
  }

  public static CommandMessage createMessage(Execution execution) {
    CommandMessage commandMessage = new CommandMessage();
    commandMessage.setExecution((ExecutionImpl) execution);
    commandMessage.setExclusive(true);
    
    ObjectDescriptor commandDescriptor = new ObjectDescriptor(ExclusiveTestCommand.class);
    commandDescriptor.addInjection("executionId", new StringDescriptor(execution.getId()));
    commandMessage.setCommandDescriptor(commandDescriptor);
    return commandMessage;
  }

  protected void executeVoid(Environment environment) throws Exception {
    Long threadId = Thread.currentThread().getId();
    
    Session session = environment.get(Session.class);
    ExecutionImpl execution = (ExecutionImpl) session.get(ExecutionImpl.class, executionId);
    
    String executionKey = execution.getKey();

    // exclusiveMessageIds maps execution keys to a set of thread ids.
    // the idea is that for each execution, all the exclusive jobs will 
    // be executed by 1 thread sequentially.  
    
    // in the end, each set should contain exactly 1 element
    
    Set<Long> groupMessages = ExclusiveMessagesTest.exclusiveThreadIds.get(executionKey);
    if (groupMessages==null) {
      groupMessages = new HashSet<Long>();
      ExclusiveMessagesTest.exclusiveThreadIds.put(executionKey, groupMessages);
    }
    groupMessages.add(threadId);
    
    /*
    // let's assume that an average jobImpl takes between 0 and 150 millis to complete.
    int workTime = random.nextInt(150);
    log.debug("executing exclusive message for "+execution+".  this is going to take "+workTime+"ms");
    try {
      Thread.sleep(workTime);
    } catch (RuntimeException e) {
      log.debug("sleeping was interrupted");
    }
    */
  }

}
