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
import org.jbpm.pvm.internal.job.CommandMessage;
import org.jbpm.pvm.internal.wire.descriptor.IntegerDescriptor;
import org.jbpm.pvm.internal.wire.descriptor.ObjectDescriptor;


/**
 * @author Tom Baeyens
 * @author Guillaume Porcher
 * 
 * Simple command that will create an exception during execution. 
 * The exception will generate a stacktrace with variable length 
 * (controlled by the length parameter).
 * 
 * This class is to test the persistence of exception stacktrace in jobs.
 */
public class GenerateExceptionTestCommand implements Command<Object> {

  private static final long serialVersionUID = 1L;
  
  int length;
  
  public GenerateExceptionTestCommand() {
  }
  
  public static CommandMessage createMessage(int recursionInitialDepth) {
    CommandMessage commandMessage = new CommandMessage();
    ObjectDescriptor commandDescriptor = new ObjectDescriptor(GenerateExceptionTestCommand.class);
    commandDescriptor.addInjection("length", new IntegerDescriptor(recursionInitialDepth));
    commandMessage.setCommandDescriptor(commandDescriptor);
    return commandMessage;
  }

  public Object execute(Environment environment) throws Exception {
    StringBuilder message = new StringBuilder();
    while (message.length() < length) {
      message.append("This is a long test message. ");
    }
    throw new RuntimeException(message.toString());
  }

}
