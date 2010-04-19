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
import org.jbpm.pvm.internal.env.EnvironmentImpl;

/** performs an authorization check before the command is executed.
 * @author Tom Baeyens
 */
public class AuthorizationInterceptor extends Interceptor {

  public <T> T execute(Command<T> command) {
    EnvironmentImpl environment = EnvironmentImpl.getCurrent();
    if (environment==null) {
      throw new JbpmException("no environment for verifying authorization");
    }
    AuthorizationSession authorizationSession = environment.get(AuthorizationSession.class);
    if (authorizationSession==null) {
      throw new JbpmException("no AuthorizationSession in environment for verifying authorization");
    }
    authorizationSession.checkPermission(command, environment);
    // if the authorization check succeeded, proceed
    return next.execute(command);
  }

}
