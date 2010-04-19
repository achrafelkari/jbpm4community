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

import java.util.Collection;
import java.util.Date;

import org.jbpm.api.cmd.Command;
import org.jbpm.internal.log.Log;
import org.jbpm.pvm.internal.cmd.CommandService;

/** this thread is responsible for acquiring jobs in the job that need to be 
 * executed and then let the JobExecutor dispatch the acquired ids to one of the
 * JobExecutorThreads in the pool.  There is only one dispatcher thread per 
 * JobExecutor.  
 * 
 * @author Tom Baeyens, Guillaume Porcher
 */
public class DispatcherThread extends Thread {

  private static final Log log = Log.getLog(DispatcherThread.class.getName());
  
  protected JobExecutor jobExecutor;
  protected volatile boolean isActive = true;
  protected boolean checkForNewJobs;
  protected int currentIdleInterval;
  protected Object semaphore = new Object();

  protected DispatcherThread(JobExecutor jobExecutor) {
    this(jobExecutor, "DispatcherThread");
  }

  protected DispatcherThread(JobExecutor jobExecutor, String name) {
    super(name);
    this.jobExecutor = jobExecutor;
  }

  public void run() {
    log.info("starting " + getName());
    currentIdleInterval = jobExecutor.getIdleMillis();
    try {
      while (isActive) {
        try {
          // checkForNewJobs is set to true in jobWasAdded() below
          checkForNewJobs = false;

          // try to acquire jobs
          Collection<Long> acquiredJobDbids = acquireJobs();

          // no exception so resetting the currentIdleInterval
          currentIdleInterval = jobExecutor.getIdleMillis();
          if ((acquiredJobDbids != null) && (!acquiredJobDbids.isEmpty())) {
            submitAcquiredJobDbids(acquiredJobDbids);
            log.debug("added jobs "+acquiredJobDbids+" to the queue");

          } else if (isActive) {
            long waitPeriod = getWaitPeriod();
            if (waitPeriod > 0) {
              synchronized (semaphore) {
                if (!checkForNewJobs) {
                  log.trace(getName()+" will wait for max "+waitPeriod+"ms on "+jobExecutor);
                  semaphore.wait(waitPeriod);
                  log.trace(getName()+" woke up");
                } else {
                  log.debug("skipped wait because new message arrived");
                }
              }
            }
          }

        } catch (InterruptedException e) {
          log.info((isActive ? "active" : "inactive") + " job dispatcher thread '" + getName() + "' got interrupted");
        } catch (Exception e) {
          // the exception doesn't have to be logged if jboss is still booting
          // this should be replaced with proper dependency configuration
          // https://jira.jboss.org/jira/browse/JBPM-2501
          if (e.getMessage().indexOf("couldn't lookup 'UserTransaction' from jndi: UserTransaction not bound: UserTransaction not bound")==-1) {
            log.error("exception in job executor thread. waiting " + currentIdleInterval + " milliseconds", e);
          }
          try {
            synchronized (semaphore) {
              semaphore.wait(currentIdleInterval);
            }
          } catch (InterruptedException e2) {
            log.trace("delay after exception got interrupted", e2);
          }
          // after an exception, the current idle interval is doubled to prevent
          // continuous exception generation when e.g. the db is unreachable
          currentIdleInterval = currentIdleInterval * 2;
        }
      }
    } finally {
      log.info(getName() + " leaves cyberspace");
    }
  }

  protected void submitAcquiredJobDbids(Collection<Long> jobDbids) {
    log.debug("submitting jobs "+jobDbids);
    jobExecutor.getThreadPool().submit(
        new JobParcel(jobExecutor.getCommandExecutor(), jobDbids));
    log.trace("jobs "+jobDbids+" were submitted");
  }

  protected Collection<Long> acquireJobs() {
    CommandService commandService = jobExecutor.getCommandExecutor();
    Command<Collection<Long>> acquireJobsCommand = jobExecutor.getAcquireJobsCommand();
    return commandService.execute(acquireJobsCommand);
  }

  protected Date getNextDueDate() {
    CommandService commandService = jobExecutor.getCommandExecutor();
    Command<Date> getNextDueDate = jobExecutor.getNextDueDateCommand();
    return commandService.execute(getNextDueDate);
  }

  protected long getWaitPeriod() {
    long interval = jobExecutor.getIdleMillis();
    Date nextDueDate = getNextDueDate();
    if (nextDueDate != null) {
      long currentTimeMillis = System.currentTimeMillis();
      long nextDueDateTime = nextDueDate.getTime();
      if (nextDueDateTime < currentTimeMillis + currentIdleInterval) {
        interval = nextDueDateTime - currentTimeMillis;
      }
    }
    if (interval < 0) {
      interval = 0;
    }
    return interval;
  }

  public void deactivate() {
    deactivate(false);
  }
  
  public void deactivate(boolean join) {
    if (isActive) {
      log.debug("deactivating "+getName());
      isActive = false;
      interrupt();
      if (join) {
        try {
          log.debug("joining "+getName());
          join();
        } catch (InterruptedException e) {
          log.trace("joining "+getName()+" got interrupted");
        }
      }
    } else {
      log.trace("ignoring deactivate: "+getName()+" is not active");
    }
  }

  public void jobWasAdded() {
    log.trace("notifying job executor dispatcher thread of new job");
    synchronized (semaphore) {
      checkForNewJobs = true;
      semaphore.notify();
    }
  }

  public boolean isActive() {
    return isActive;
  }
}
