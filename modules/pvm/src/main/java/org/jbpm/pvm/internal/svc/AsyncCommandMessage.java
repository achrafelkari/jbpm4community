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
package org.jbpm.pvm.internal.svc;

import org.jbpm.api.Execution;
import org.jbpm.api.cmd.Command;
import org.jbpm.api.cmd.Environment;
import org.jbpm.pvm.internal.cmd.CommandService;
import org.jbpm.pvm.internal.env.EnvironmentImpl;
import org.jbpm.pvm.internal.job.MessageImpl;

/**
 * @author Tom Baeyens
 */
public class AsyncCommandMessage extends MessageImpl {

  private static final long serialVersionUID = 1L;

  private Command<?> command;
  private String userId;
  
  public AsyncCommandMessage(Command<?> command) {
    this.command = command;
  }

  public AsyncCommandMessage(Command<?> command, String userId) {
    this.command = command;
    this.userId = userId;
  }

  protected void executeVoid(Environment environment) throws Exception {
    execution.setState(Execution.STATE_ACTIVE_ROOT);

    CommandService commandService = environment.get(CommandService.class);
    if (userId == null) {
      commandService.execute(command);
    }
    else {
      EnvironmentImpl environmentImpl = (EnvironmentImpl) environment;
      environmentImpl.setAuthenticatedUserId(userId);
      try {
        commandService.execute(command);
      }
      finally {
        environmentImpl.setAuthenticatedUserId(null);
      }
    }
  }
  
  public String toString() {
    return "AsyncCommandMessage["+dbid+"]";
  }
}
