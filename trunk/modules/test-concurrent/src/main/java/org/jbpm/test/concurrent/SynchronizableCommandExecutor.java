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
/**
 * 
 */
package org.jbpm.test.concurrent;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

import org.jbpm.api.cmd.Command;
import org.jbpm.api.job.Job;
import org.jbpm.pvm.internal.cmd.ExecuteJobCmd;
import org.jbpm.pvm.internal.env.EnvironmentFactory;
import org.jbpm.pvm.internal.env.EnvironmentImpl;
import org.jbpm.pvm.internal.tx.StandardTransaction;

/**
 * Executes a given command in a separate thread.
 * 
 * Has several sync points available (see {@link CommandExecutionSynchronization}):
 * - before/after transaction is started
 * - before/after job execution
 * - before/after transaction is done (ie commit)
 * 
 * TODO: implement transaction sync, now only sync on job execution is done - waiting until use case passes by
 * 
 * @author Joram Barrez
 */
public class SynchronizableCommandExecutor extends Thread {
    
    /** Used to create environment blocks */
    private EnvironmentFactory environmentFactory;
  
    /** The database id of the job that will be executed */
    private String jobId;
    
    /** The command that will be executed */
    private Command command;
    
    /** 
     * If an exception occurs in a thread that is not the JUnit thread, then
     * the JUnit thread will never notice this. The solution is to catch the
     * exception and store it in this field, so the JUnit thread can check
     * if there were any exceptions when executing the job.
     */
    private Exception exception;
    
    /** Indicates if the thread must be blocked (ie this.wait() must be called) */
    private boolean threadBlocked;
    
    /**
     * List of synchronizations that will be called on specific point in the
     * execution of this thread.
     */
    private List<CommandExecutionSynchronization> synchronizations;
    
    /* SYNC PRIMITIVES */
    
    private CyclicBarrier afterExecutionBarrier;
    
    private List<SynchronizableCommandExecutor> executorsSyncedAfterExecution;
    
    private CyclicBarrier beforeExecutionBarrier;
    
    /**
     * Constructor to be used when the thread must execute a job
     */
    public SynchronizableCommandExecutor(EnvironmentFactory environmentFactory, Job job) {
      this.environmentFactory = environmentFactory;
      this.jobId = job.getId();
      this.threadBlocked = false;
      
      this.synchronizations = new ArrayList<CommandExecutionSynchronization>();
      synchronizations.add(new DefaultSynchronization());
    }
    
    /**
     * Constructor to be used when the thread must execute a command
     */
    public SynchronizableCommandExecutor(EnvironmentFactory environmentFactory, Command command) {
      this.environmentFactory = environmentFactory;
      this.command = command;
      this.threadBlocked = false;
      
      this.synchronizations = new ArrayList<CommandExecutionSynchronization>();
      synchronizations.add(new DefaultSynchronization());
    }
    
    public void run() {
      
      EnvironmentImpl environment = environmentFactory.openEnvironment();
      StandardTransaction standardTransaction = environment.get(StandardTransaction.class);
      standardTransaction.begin();

      try {
        
        handleBeforeExecutionSynchronizations();
        
        if (jobId != null) {
          ExecuteJobCmd executeJobCmd = new ExecuteJobCmd(jobId);
          executeJobCmd.execute(environment);
        }
        
        if (command !=  null) {
          command.execute(environment);
        }
        
        handleAfterExecutionSynchronizations();
        
      } catch (Exception e) {
        standardTransaction.setRollbackOnly();
        this.exception = e;
        
      } finally {
        
        try {
          standardTransaction.complete();
        } catch (Exception e) {
          this.exception = e;
        }
        
      }
      environment.close();
    }
    
    /**
     * Executes all synchronizations that must be executed before the job is executed
     */
    private void handleBeforeExecutionSynchronizations() {
      for (CommandExecutionSynchronization synchronization : synchronizations) {
        synchronization.beforeExecution();
      }
    }
    
    /**
     * Executes all synchronizations that must be executed when the job is executed
     */
    private void handleAfterExecutionSynchronizations() {
      for (CommandExecutionSynchronization synchronization : synchronizations) {
        synchronization.afterExecution();
      }
    }
    
    /**
     * Unit tests that use this class can use this method to synchronize
     * when the job just has been executed.
     * 
     * @param blockJobExecutor If true, the jobExecutor will be halted
     * when leaving the synchronisation point
     */
    public void waitUntilExecutionFinished(boolean blockJobExecutor) {
      if (afterExecutionBarrier != null) {
        try {
          threadBlocked = blockJobExecutor;
          afterExecutionBarrier.await();
        } catch (InterruptedException e) {
          e.printStackTrace();
        } catch (BrokenBarrierException e) {
          e.printStackTrace();
        }
      }
    }
    
    public SynchronizableCommandExecutor synchroniseAfterExecution() {
      if (isAlive()) {
        throw new RuntimeException("Cannot set synchronization point once the thread has been started");
      }
      
      if (afterExecutionBarrier == null ) {
        setSyncPointForAfterExecution(new CyclicBarrier(2));
      } else {
        setSyncPointForAfterExecution(new CyclicBarrier(afterExecutionBarrier.getParties() + 1));
      }
      
      setSyncPointForAfterExecution(afterExecutionBarrier);
      
      return this;
    }
    
    public SynchronizableCommandExecutor synchroniseAfterExecution(SynchronizableCommandExecutor otherExecutor) {
      if (this.isAlive() || otherExecutor.isAlive()) {
        throw new RuntimeException("Cannot set synchronization point once the thread has been started");
      }
      
      if (executorsSyncedAfterExecution == null) {
        executorsSyncedAfterExecution = new ArrayList<SynchronizableCommandExecutor>();
      }
      executorsSyncedAfterExecution.add(otherExecutor);
      
      int threadsInvolved = 0;
      if (afterExecutionBarrier != null) {
        threadsInvolved += afterExecutionBarrier.getParties();
      } else {
        threadsInvolved++;
      }
      
      if (otherExecutor.afterExecutionBarrier != null) {
        threadsInvolved += otherExecutor.afterExecutionBarrier.getParties();
      } else {
        threadsInvolved++;
      }
      
      setSyncPointForAfterExecution(new CyclicBarrier(threadsInvolved));
      
      return this; 
    }
    
    private void setSyncPointForAfterExecution(CyclicBarrier syncpoint) {
      this.afterExecutionBarrier = syncpoint;
      if (executorsSyncedAfterExecution == null) {
        executorsSyncedAfterExecution = new ArrayList<SynchronizableCommandExecutor>();
      }
      for (SynchronizableCommandExecutor executor : executorsSyncedAfterExecution) {
        executor.afterExecutionBarrier = this.afterExecutionBarrier;
      }
    }
    
    public SynchronizableCommandExecutor synchroniseBeforeExecution() {
      if (isAlive()) {
        throw new RuntimeException("Cannot set synchronization point once the JobExecutorEmulator has been started");
      }
      
      if (beforeExecutionBarrier == null ) {
        beforeExecutionBarrier = new CyclicBarrier(2);
      } else {
        beforeExecutionBarrier = new CyclicBarrier(beforeExecutionBarrier.getParties() + 1);
      }
      
      return this;
    }
    
    /**
     * Helper method: check if the flag 'threadBlocked' has been raised.
     * If so, this thread will block until it is notified again.
     */
    private void blockIfNeeded() {
      if (threadBlocked) {
        synchronized (this) {
          try {
            threadBlocked = false;
            wait();
          } catch (InterruptedException e) {
            e.printStackTrace();
          }
        }
      }
    }
    
    /**
     * Unit tests can use this method to proceed a halted JobExecutorEmulator.
     * (ie this means that the wait() was called on this thread in the past).
     */
    public void goOn() {
      synchronized (this) {
          notify();
      };
    }
    
    /**
     * Adds a custom synchronization to the JobExecutorEmulator.
     */
    public void addSynchronization(CommandExecutionSynchronization synchronization) {
      synchronizations.add(synchronization);
    }
    
    /* GETTERS AND SETTERS */
    
    public Exception getException() {
      return exception;
    }
    
    public boolean isBlockThread() {
      return threadBlocked;
    }

    public void setBlockThread(boolean blockThread) {
      this.threadBlocked = blockThread;
    }
    
    public void setAfterExecutionBarrier(CyclicBarrier afterExecutionBarrier) {
      this.afterExecutionBarrier = afterExecutionBarrier;
    }

    public void setBeforeExecutionBarrier(CyclicBarrier beforeExecutionBarrier) {
      this.beforeExecutionBarrier = beforeExecutionBarrier;
    }

    /**
     * Default synchronization, executed by all threads.
     * The default logic will synchronize at every sync point (ie barrier) 
     * which is not null and will check if any external caller has raised
     * the 'threadBlocked' flag.
     */
    private class DefaultSynchronization extends CommandExecutionSynchronization {
      
      public void afterExecution() {
      
      if (afterExecutionBarrier != null) {
        try {
          afterExecutionBarrier.await();
          blockIfNeeded();
        } catch (InterruptedException e) {
          e.printStackTrace();
        } catch (BrokenBarrierException e) {
          e.printStackTrace();
        }
      }
    }

    public void beforeExecution() {
      
      if (beforeExecutionBarrier != null) {
        try {
          beforeExecutionBarrier.await();
          blockIfNeeded();
        } catch (InterruptedException e) {
          e.printStackTrace();
        } catch (BrokenBarrierException e) {
          e.printStackTrace();
        }
      }
    }

  }

}
