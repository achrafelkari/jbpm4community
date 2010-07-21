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
package org.jbpm.test.load.executions;

import java.util.ArrayList;
import java.util.List;

import org.jbpm.api.ProcessInstance;
import org.jbpm.internal.log.Log;
import org.jbpm.test.load.LoadTestCase;

/**
 * @author Tom Baeyens
 */
public class ProcessExecutionTest extends LoadTestCase {
  
  private static Log log = Log.getLog(ProcessExecutionTest.class.getName());
  
  static int processes = 1000;
  static int threads = 5;
  
  Throwable exception;
  int finished;

  public void logStatus() {
    String measuredTime = getMeasuredTime();
    logFileWriter.println(getUsedMemory()+"\t"+finished+"\t"+measuredTime);
    logFileWriter.flush();
    log.info(finished+" executions in "+measuredTime);
  }
  
  protected void logColumnTitles() {
    logFileWriter.println("Used Memory\tFinished Executions\tTime");
    logFileWriter.flush();
  }

  public void testExecuteProcesses() throws Exception {
    deployFromClasspath("org/jbpm/test/load/executions/process.jpdl.xml");
    
    startMeasuringTime();
    
    List<Thread> threadList = new ArrayList<Thread>();
    for (int i=0; i<threads; i++) {
      Thread thread = new ProcessExecutor();
      thread.start();
      threadList.add(thread);
    }
    
    for (Thread thread: threadList) {
      while (thread.isAlive()) {
        try {
          thread.join(checkInterval);
        } catch (InterruptedException e) {
          log.info(e.toString());
        }
        logStatus();
      }
      if (exception!=null) {
        break;
      }
    }
    
    if (exception!=null) {
      throw new Exception("thread threw: "+exception.getMessage(), exception);
    } else {
      log.info(finished+" executions in "+getMeasuredTime());
    }
  }
  
  public class ProcessExecutor extends Thread {
    public void run() {
      try {
        for (int i=0; i<processes; i++) {
          executeProcess();
        }
      } catch (RuntimeException e) {
        exception = e;
      }
    }
    public void executeProcess() {
      ProcessInstance processInstance = executionService.startProcessInstanceByKey("Process");
      assertTrue(processInstance.isActive("c"));
      String executionId = processInstance.getId();
      processInstance = executionService.signalExecutionById(executionId);
      assertTrue(processInstance.isEnded());
      processFinished();
    }
  }

  public synchronized void processFinished() {
    finished++;
  }
}
