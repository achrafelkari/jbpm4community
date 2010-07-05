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
package org.jbpm.pvm.internal.cmd;

import java.util.Date;

import org.jbpm.api.JbpmException;
import org.jbpm.api.cmd.Environment;
import org.jbpm.api.job.Job;
import org.jbpm.internal.log.Log;
import org.jbpm.pvm.internal.env.EnvironmentImpl;
import org.jbpm.pvm.internal.env.JobContext;
import org.jbpm.pvm.internal.job.JobImpl;
import org.jbpm.pvm.internal.jobexecutor.JobExceptionHandler;
import org.jbpm.pvm.internal.session.DbSession;
import org.jbpm.pvm.internal.tx.Transaction;

/**
 * @author Tom Baeyens
 * @author Huisheng Xu
 */
public class ExecuteJobCmd extends AbstractCommand<Job> {

  private static final long serialVersionUID = 1L;

  private static final Log log = Log.getLog(ExecuteJobCmd.class.getName());

  protected Long jobDbid;

  protected JobExceptionHandler jobExceptionHandler;

  public ExecuteJobCmd(String jobId) {
    if (jobId==null) {
      throw new JbpmException("jobId is null");
    }
    jobDbid = Long.parseLong(jobId);
  }

  public ExecuteJobCmd(Long jobDbid) {
    this.jobDbid = jobDbid;
  }

  public Job execute(Environment environmentInterface) throws Exception {
    EnvironmentImpl environment = (EnvironmentImpl) environmentInterface;
    DbSession dbSession = environment.get(DbSession.class);
    if (dbSession == null) {
      throw new JbpmException("no db-session configured");
    }
    JobImpl job = dbSession.get(JobImpl.class, jobDbid);

    // in case of decision jobs, the job might have been deleted
    // before we execute it (they are in a list)
    if (job != null) {
        this.registerJobExceptionHandler(environment, job);

        JobContext jobContext = new JobContext(job);
        environment.setContext(jobContext);
        try {
          log.debug("executing job " + job + "...");
          Boolean deleteJob = job.execute(environment);
          log.debug("executed job " + job);
          if (deleteJob) {
            dbSession.delete(job);
          }

          // if this job is locked too long, it could be unlocked by the lockmonitor and
          // executed by another thread.
          Date lockExpirationDate = job.getLockExpirationTime();
          // can be null if it was rescheduled
          if (lockExpirationDate != null) {
              long lockExpiration = lockExpirationDate.getTime();
              long currentTime = System.currentTimeMillis();
              if (currentTime>lockExpiration) {
                throw new JbpmException("job took too long: lock expired "
                  + (currentTime - lockExpiration) + "ms ago");
              }
          }
        } catch (Exception exception) {
          log.error("exception while executing '" + job + "'", exception);
          handleJobExecutionException(exception);
        } finally {
          environment.removeContext(jobContext);
        }

    } else {
        log.debug("job " + jobDbid + " no longer exists");
    }
    return job;
  }

  /** This transaction will be marked for rollback.  A command will be associated with the
   * Transaction.EVENT_AFTERCOMPLETION (after the job locks of the current transaction are
   * released).  Then the command will update the job with the exception details in a separate
   * transaction. */
  protected void handleJobExecutionException(Exception exception) {
    if (jobExceptionHandler != null) {
      jobExceptionHandler.setException(exception);
    } else {
      log.warn("jobExceptionHandler hasnot initialized for exception : " + exception.getMessage());
    }

    if (exception instanceof RuntimeException) {
      throw (RuntimeException) exception;
    }
    throw new JbpmException("job failed: " + exception.getMessage(), exception);
  }

  protected void registerJobExceptionHandler(Environment environment, JobImpl job) {
    Transaction transaction = environment.get(Transaction.class);
    CommandService commandService = (CommandService) environment
        .get(CommandService.NAME_NEW_TX_REQUIRED_COMMAND_SERVICE);

    jobExceptionHandler = new JobExceptionHandler(job.getDbid(), commandService);
    try {
      transaction.registerSynchronization(jobExceptionHandler);
    } catch(Exception ex) {
      log.warn("cannot register synchronization on current transaction : " + ex.getMessage()
        + " job : " + job.getDbid());
    }
  }
}
