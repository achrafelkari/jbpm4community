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

import java.util.Random;

import org.hibernate.Session;
import org.jbpm.api.cmd.Command;
import org.jbpm.api.cmd.Environment;
import org.jbpm.pvm.internal.history.model.HistoryCommentImpl;
import org.jbpm.pvm.internal.history.model.HistoryDetailImpl;
import org.jbpm.pvm.internal.job.CommandMessage;
import org.jbpm.pvm.internal.wire.descriptor.IntegerDescriptor;
import org.jbpm.pvm.internal.wire.descriptor.ObjectDescriptor;

/**
 * @author Tom Baeyens
 */
public class NormalMessageCommand implements Command<Void>  {
  
  private static final long serialVersionUID = 1L;
  static Random random = new Random();
  
  int messageId;
  
  public NormalMessageCommand() {
  }
  
  public NormalMessageCommand(int messageId) {
    this.messageId = messageId;
  }

  public static CommandMessage createMessage(int messageId) {
    CommandMessage commandMessage = new CommandMessage();
    ObjectDescriptor commandDescriptor = new ObjectDescriptor(NormalMessageCommand.class);
    commandDescriptor.addInjection("messageId", new IntegerDescriptor(messageId));
    commandMessage.setCommandDescriptor(commandDescriptor);
    return commandMessage;
  }

  public Void execute(Environment environment) throws Exception {
    HistoryDetailImpl comment = new HistoryCommentImpl(Integer.toString(messageId));
    Session session = environment.get(Session.class);
    session.save(comment);
    return null;
  }
}
