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
package org.jbpm.test.load.async;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import org.hibernate.Query;
import org.hibernate.Session;
import org.jbpm.api.cmd.Command;
import org.jbpm.api.cmd.Environment;
import org.jbpm.pvm.internal.cmd.CommandService;
import org.jbpm.pvm.internal.job.JobImpl;
import org.jbpm.pvm.internal.jobexecutor.JobExecutor;
import org.jbpm.test.JbpmTestCase;


/**
 * @author Tom Baeyens
 */
public class JobExecutorTestCase extends JbpmTestCase {

  long timeoutMillis = 20 * 1000; // 20 seconds
  long checkInterval = 400;

  static String jobsAvailableQueryText =
      "select count(*) "+
      "from "+JobImpl.class.getName()+" as job "+
      "where ( (job.dueDate is null) or (job.dueDate <= :now) ) "+ 
      "  and ( job.retries > 0 )";

  protected CommandService commandService;
  protected JobExecutor jobExecutor;

  protected void setUp() throws Exception {
    super.setUp();
    
    commandService = processEngine.get(CommandService.class);
    jobExecutor = processEngine.get(JobExecutor.class);
  }
  
  protected void waitTillNoMoreMessages() {

    // install a timer that will interrupt if it takes too long
    // if that happens, it will lead to an interrupted exception and the test
    // will fail
    TimerTask interruptTask = new TimerTask() {

      Thread testThread = Thread.currentThread();

      public void run() {
        log.debug("test " + getName() + " took too long. going to interrupt..." + testThread);
        testThread.interrupt();
      }
    };
    Timer timer = new Timer();
    timer.schedule(interruptTask, timeoutMillis);

    try {
      boolean jobsAvailable = true;
      while (jobsAvailable) {
        log.debug("going to sleep for " + checkInterval + " millis, waiting for the job executor to process more jobs");
        Thread.sleep(checkInterval);
        jobsAvailable = areJobsAvailable();
      }

    } catch (InterruptedException e) {
      fail("test execution exceeded treshold of " + timeoutMillis + " milliseconds");
    } finally {
      timer.cancel();
    }
  }

  public boolean areJobsAvailable() {
    return commandService.execute(new Command<Boolean>() {
      private static final long serialVersionUID = 1L;

      public Boolean execute(Environment environment) {
        Session session = environment.get(Session.class);

        Query query = session.createQuery(jobsAvailableQueryText);
        query.setDate("now", new Date());
        
        Long jobs = (Long) query.uniqueResult();

        if (jobs.longValue()>0) {
          log.debug("found "+jobs+" more jobs to process");
          return true;
        }
        log.debug("no more jobs to process");
        
        return false;
      }
    });
  }

}
