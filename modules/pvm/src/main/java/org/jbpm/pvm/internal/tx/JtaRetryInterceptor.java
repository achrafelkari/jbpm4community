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

import javax.transaction.Status;
import javax.transaction.UserTransaction;

import org.jbpm.api.cmd.Command;
import org.jbpm.internal.log.Log;
import org.jbpm.pvm.internal.env.EnvironmentImpl;
import org.jbpm.pvm.internal.svc.RetryInterceptor;


/**
 * @author Tom Baeyens
 */
public class JtaRetryInterceptor extends RetryInterceptor {
  
  private static Log log = Log.getLog(JtaRetryInterceptor.class.getName());

  public <T> T execute(Command<T> command) {
    JtaTransaction jtaTransaction = EnvironmentImpl.getFromCurrent(JtaTransaction.class);
    UserTransaction userTransaction = jtaTransaction.lookupJeeUserTransaction();
    int status = JtaTransaction.getUserTransactionStatus(userTransaction);
    if (status == Status.STATUS_NO_TRANSACTION) {
      return executeWithRetry(command);
    }
    log.trace("transaction in progress.  skipping retry interceptor");
    return executeWithoutRetry(command);
  }
  
  public <T> T executeWithRetry(Command<T> command) {
    return super.execute(command);
  }

  public <T> T executeWithoutRetry(Command<T> command) {
    return next.execute(command);
  }
}
