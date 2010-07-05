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
package org.jbpm.pvm.internal.jobexecutor;

import java.io.PrintWriter;
import java.io.StringWriter;

import javax.transaction.Synchronization;

import org.jbpm.api.JbpmException;
import org.jbpm.api.cmd.Command;
import org.jbpm.api.cmd.Environment;
import org.jbpm.internal.log.Log;
import org.jbpm.pvm.internal.cmd.CommandService;
import org.jbpm.pvm.internal.job.JobImpl;
import org.jbpm.pvm.internal.session.DbSession;
import org.jbpm.pvm.internal.tx.Transaction;

/**
 * @author Tom Baeyens
 */
public class JobExceptionHandler implements Synchronization, Command<Object> {

  private static final Log log = Log.getLog(JobExceptionHandler.class.getName());
  private static final long serialVersionUID = 1L;

  protected CommandService commandService;
  protected long jobDbid;
  protected Throwable exception;

  public JobExceptionHandler(long jobDbid, CommandService commandService) {
    this.commandService = commandService;
    this.jobDbid = jobDbid;
  }

  public void setException(Throwable exception) {
      this.exception = exception;
  }

  public void beforeCompletion() {
  }

  public void afterCompletion(int status) {
    if (exception == null) {
      return;
    }
    // after the transaction rolled back,
    // execute this job exception handler object as a command with
    // the command service so that this gets done in a separate
    // transaction
    log.debug("starting new transaction for handling job exception");
    commandService.execute(this);
    log.debug("completed transaction for handling job exception");
  }

  public Object execute(Environment environment) throws Exception {
    log.debug("handling job "+jobDbid+" exception: "+exception.getMessage());

    // load the job from the db
    DbSession dbSession = environment.get(DbSession.class);
    if (dbSession==null) {
      throw new JbpmException("no job-session configured to handle job");
    }
    JobImpl job = dbSession.get(JobImpl.class, jobDbid);
    // serialize the stack trace
    StringWriter sw = new StringWriter();
    exception.printStackTrace(new PrintWriter(sw));
    if (job != null) {
      // decrement the number of retries
      int decrementedRetries = job.getRetries()-1;
      log.debug("decrementing retries to "+decrementedRetries+" for "+job);
      job.release();
      job.setRetries(decrementedRetries);
      job.setException(sw.toString());

      // notify the job executor after the transaction is completed
      Transaction transaction = environment.get(Transaction.class);
      JobExecutor jobExecutor = environment.get(JobExecutor.class);
      if ( (transaction!=null)
           && (jobExecutor!=null)
         ) {
        log.trace("registering job executor notifier with "+transaction);
        transaction.registerSynchronization(new JobAddedNotification(jobExecutor));
      }
    }
    return null;
  }
}
