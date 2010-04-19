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

import java.util.List;

import org.hibernate.StaleStateException;
import org.jbpm.api.Execution;
import org.jbpm.api.ProcessInstance;
import org.jbpm.api.job.Job;
import org.jbpm.pvm.internal.cmd.SignalCmd;


/**
 * Concurrency test case: when a timer is defined on an activity, a job
 * will be executed by the jobExecutor. But when at the same time a 
 * signal is done on the same activity, a conflicting situation occurs.
 * 
 * This test case will mimic this behaviour to understand how the different
 * databases react on such a conflict. 
 * 
 * @author Joram Barrez
 */
public class TimerVsSignalConcurrencyTest extends ConcurrentJbpmTestCase {
  
  public void testStaleObjectExceptionThrown() {
    
    deployJpdlXmlString(
            "<process name='timer_vs_signal'>" +
            "  <start>" +
            "    <transition to='wait' />" +
            "  </start>" +
            "  <state name='wait'>" +
            "    <transition name='timeout' to='end'>" +
            "      <timer duedate='1 second' />" +
            "    </transition>" +
            "    <transition to='end' name='go on' />" +
            "  </state>" +
            "  <end name='end' />" +
            "</process>"
          );
    
    ProcessInstance processInstance = executionService.startProcessInstanceByKey("timer_vs_signal");
    final List<Job> jobs = managementService.createJobQuery().processInstanceId(processInstance.getId()).list();
    assertEquals(1, jobs.size());

    //SynchronizableCommandExecutor jobExecutorEmulator = new SynchronizableCommandExecutor(environmentFactory, jobs.get(0));
    SynchronizableCommandExecutor jobExecutorEmulator = new SynchronizableCommandExecutor(environmentFactory, jobs.get(0));
    jobExecutorEmulator.synchroniseAfterExecution();
    jobExecutorEmulator.start();

    jobExecutorEmulator.waitUntilExecutionFinished(true); // transaction will be stalled until signal is done
    
    // Cause conflicting transaction
    final Execution executionAtState = processInstance.findActiveExecutionIn("wait");
    assertNotNull(executionAtState);
    
    SignalCmd signalCmd = new SignalCmd(executionAtState.getId(), "go on", null);
    SynchronizableCommandExecutor signalThread = new SynchronizableCommandExecutor(environmentFactory, signalCmd);
    signalThread.start();
    
    // Best effort: wait 1 sec and see if the staleObjectException has been caused
    synchronized (this) {
      try {
        wait(1000L);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }
    
    jobExecutorEmulator.goOn();
    
    try {
      jobExecutorEmulator.join();
      signalThread.join();
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    

    
    if (!(jobExecutorEmulator.getException() instanceof StaleStateException
            || signalThread.getException() instanceof StaleStateException)) {
      fail("None of the threads threw a StaleStateException");
    }
    
  }

}
