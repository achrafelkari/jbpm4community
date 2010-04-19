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
package org.jbpm.pvm.internal.model;

import javax.transaction.Status;
import javax.transaction.Synchronization;

import org.jbpm.api.JbpmException;
import org.jbpm.api.cmd.Command;
import org.jbpm.api.cmd.Environment;
import org.jbpm.pvm.internal.cmd.CommandService;
import org.jbpm.pvm.internal.session.DbSession;
import org.jbpm.pvm.internal.tx.Transaction;

public class ExceptionHandlerSynchronization implements Synchronization, Command<Object> {

  private final ExceptionHandlerImpl exceptionHandler;

  private static final long serialVersionUID = 1L;
  
  protected ExecutionImpl execution;
  protected Exception exception;
  protected CommandService commandService;

  public ExceptionHandlerSynchronization(ExceptionHandlerImpl exceptionHandler, ExecutionImpl execution, Exception exception, CommandService commandService) {
    this.exceptionHandler = exceptionHandler;
    this.execution = execution;
    this.exception = exception;
    this.commandService = commandService;
  }

  public void afterCompletion(int status) {
    if (status!=Status.STATUS_ROLLEDBACK) {
      ExceptionHandlerImpl.log.info("WARNING: no rollback after transactional exception handler. did you forget to rollback the transaction ?");
    }
    commandService.execute(this);
  }

  public Object execute(Environment environment) {
    // reload the execution
    DbSession dbSession = environment.get(DbSession.class);
    if (dbSession==null) {
      throw new JbpmException("no "+DbSession.class.getName()+" available in the environment for reloading the execution");
    }
    execution = dbSession.get(ExecutionImpl.class, execution.getId());
    exceptionHandler.executeHandler(execution, exception);
    return null;
  }

  public void beforeCompletion() {
  }

  public void register(Transaction transaction) {
    // registration of the synchronization is delegated to the AfterTxCompletionListener
    // to avoid a dependency on class Synchronization in class ExceptionHandlerImpl
    transaction.registerSynchronization(this);
  }
}