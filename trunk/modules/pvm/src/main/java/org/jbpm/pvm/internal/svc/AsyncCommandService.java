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

import org.jbpm.api.JbpmException;
import org.jbpm.api.cmd.Command;
import org.jbpm.pvm.internal.cmd.CommandService;
import org.jbpm.pvm.internal.env.EnvironmentImpl;
import org.jbpm.pvm.internal.session.MessageSession;


/**
 * @author Tom Baeyens
 */
public class AsyncCommandService implements CommandService {
  
  boolean propagateUserId = true;

  public <T> T execute(Command<T> command) {
    EnvironmentImpl environment = EnvironmentImpl.getCurrent();
    if (environment==null) {
      throw new JbpmException("no environment for verifying authorization");
    }
    MessageSession messageSession = environment.get(MessageSession.class);
    if (messageSession==null) {
      throw new JbpmException("no message session for executing command asynchronously");
    }
    String userId = (propagateUserId ? environment.getAuthenticatedUserId() : null);
    messageSession.send(new AsyncCommandMessage(command, userId));
    return null;
  }
  
  public void setPropagateUserId(boolean propagateUserId) {
    this.propagateUserId = propagateUserId;
  }
}
