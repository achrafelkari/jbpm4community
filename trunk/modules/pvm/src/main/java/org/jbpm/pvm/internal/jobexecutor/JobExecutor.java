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

import java.io.Serializable;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.jbpm.api.JbpmException;
import org.jbpm.api.cmd.Command;
import org.jbpm.internal.log.Log;
import org.jbpm.pvm.internal.cmd.CommandService;

/** manager for job execution threads and their configuration.
 * 
 * @author Tom Baeyens, Guillaume Porcher
 */
public class JobExecutor implements Serializable {
  
  private static final Log log = Log.getLog(JobExecutor.class.getName());

  private static final long serialVersionUID = 1L;

  // configuration parameters
  
  private CommandService commandService;
  private String name = "JobExecutor-"+getHostName();
  private int nbrOfThreads = 3;
  private int idleMillis = 5*1000; // default normal poll interval is 5 seconds
  private int idleMillisMax = 5*60*1000; // default max poll interval in case of continuous exceptions is 5 minutes
  private int historySize = 200;
  private int lockMillis = 30*60*1000; // default max lock time is 30 minutes

  // runtime state

  private Command<Collection<Long>> acquireJobsCommand;
  private Command<Date> nextDueDateCommand;
  
  private boolean isActive;

  private ExecutorService threadPool;
  private DispatcherThread dispatcherThread;
  
  private List<JobHistoryEntry> history = new ArrayList<JobHistoryEntry>();
  
  /** starts the {@link DispatcherThread} and {@link JobExecutorThread}s for this job executor */
  public synchronized void start() {
    if (commandService==null) {
      throw new JbpmException("no command executor available in job executor");
    }
    if (! isActive) {
      acquireJobsCommand = new AcquireJobsCmd(this);
      nextDueDateCommand = new GetNextDueDateCmd();
      
      isActive = true;
      log.trace("starting thread pool for job executor '"+name+"'...");
      threadPool = new ThreadPoolExecutor(nbrOfThreads, 
                                          nbrOfThreads, 
                                          0L, 
                                          TimeUnit.MILLISECONDS,
                                          new ArrayBlockingQueue<Runnable>(nbrOfThreads), 
                                          JobRejectionHandler.INSTANCE);

      log.trace("starting dispatcher thread for job executor '"+name+"'...");
      dispatcherThread = new DispatcherThread(this);
      dispatcherThread.start();
      
    } else {
      log.trace("ignoring start: job executor '"+name+"' is already started'");
    }
  }

  static final class JobRejectionHandler implements RejectedExecutionHandler {
    
    static final JobRejectionHandler INSTANCE = new JobRejectionHandler();

    public void rejectedExecution(Runnable task, ThreadPoolExecutor executor) {
      try {
        executor.getQueue().put(task);
      } catch (InterruptedException e) {
        throw new RejectedExecutionException("queuing " + task + " got interrupted", e);
      }
    }
  }

  /** stops with join set to false.
   * @see #stop(boolean) */ 
  public synchronized void stop() {
    stop(false);
  }
  
  /** signals to all threads managed by this job executor to stop.  Stopping the 
   * dispatcher thread is blocking till the dispatcher is no more alive.  If join is set to true, 
   * this method will block until all job executor threads 
   * are joined and actually dead. If join is false, the job executor threads are only told to 
   * stop, without waiting till they are actually stopped. It may be that job executor threads 
   * are in the middle of executing a job and they may finish after this method returned.
   */
  public synchronized void stop(boolean join) {
    log.debug("stopping job executor");
    if (isActive) {
      isActive = false;
      dispatcherThread.deactivate(join);
      threadPool.shutdown();
      if (join) {
        try {
          threadPool.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
          log.trace("joining "+getName()+" got interrupted");
        }
      }
    } else {
      log.trace("ignoring stop: job executor '"+name+"' not started");
    }
  }

  public void jobWasAdded() {
    if ( (dispatcherThread!=null)
         && (dispatcherThread.isActive())
       ) {
      dispatcherThread.jobWasAdded();
    }
  }

  static String getHostName() {
    try {
      return InetAddress.getLocalHost().getHostAddress();
    } catch (UnknownHostException e) {
      return "localhost";
    }
  }

  protected ExecutorService getThreadPool() {
    return threadPool;
  }

  // getters //////////////////////////////////////////////////////////////////
  
  public String getName() {
    return name;
  }
  public int getHistorySize() {
    return historySize;
  }
  public int getIdleMillis() {
    return idleMillis;
  }
  public boolean isActive() {
    return isActive;
  }
  public int getIdleMillisMax() {
    return idleMillisMax;
  }
  public int getLockMillis() {
    return lockMillis;
  }
  public int getNbrOfThreads() {
    return nbrOfThreads;
  }
  public CommandService getCommandExecutor() {
    return commandService;
  }
  public Command<Collection<Long>> getAcquireJobsCommand() {
    return acquireJobsCommand;
  }
  public Command<Date> getNextDueDateCommand() {
    return nextDueDateCommand;
  }
  public DispatcherThread getDispatcherThread() {
    return dispatcherThread;
  }
  public List<JobHistoryEntry> getHistory() {
    return history;
  }

  // configuration setters ////////////////////////////////////////////////////

  public void setCommandExecutor(CommandService commandService) {
    this.commandService = commandService;
  }
  public void setName(String name) {
    this.name = name;
  }
  public void setNbrOfJobExecutorThreads(int nbrOfJobExecutorThreads) {
    this.nbrOfThreads = nbrOfJobExecutorThreads;
  }
  public void setIdleInterval(int idleInterval) {
    this.idleMillis = idleInterval;
  }
  public void setMaxIdleInterval(int maxIdleInterval) {
    this.idleMillisMax = maxIdleInterval;
  }
  public void setHistoryMaxSize(int historyMaxSize) {
    this.historySize = historyMaxSize;
  }
  public void setMaxLockTime(int maxLockTime) {
    this.lockMillis = maxLockTime;
  }
}
