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
package org.jbpm.pvm.internal.tx;

import org.jbpm.api.JbpmException;
import org.jbpm.api.cmd.Command;
import org.jbpm.internal.log.Log;
import org.jbpm.pvm.internal.env.EnvironmentImpl;
import org.jbpm.pvm.internal.svc.Interceptor;


/** calls setRollbackOnly on the transaction in the environment 
 * in case an exception occurs during execution of the command.
 * 
 * @author Tom Baeyens
 */
public class StandardTransactionInterceptor extends Interceptor {
  
  private static final Log log = Log.getLog(StandardTransactionInterceptor.class.getName());
  
  public <T> T execute(Command<T> command) {
    EnvironmentImpl environment = EnvironmentImpl.getCurrent();
    if (environment==null) {
      throw new JbpmException("no environment for managing hibernate transaction");
    }

    StandardTransaction standardTransaction = environment.get(StandardTransaction.class);
    if (standardTransaction==null) {
      throw new JbpmException("no standard-transaction in environment");
    }
    
    standardTransaction.begin();

    try {
      return next.execute(command);
      
    } catch (RuntimeException e) {
      standardTransaction.setRollbackOnly();
      throw e;
      
    } finally {
      standardTransaction.complete();
    }
  }
}
