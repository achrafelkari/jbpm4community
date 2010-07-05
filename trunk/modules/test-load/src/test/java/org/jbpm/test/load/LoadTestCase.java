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
package org.jbpm.test.load;

import java.io.File;
import java.io.PrintWriter;
import java.util.Date;

import org.hibernate.Query;
import org.hibernate.Session;
import org.jbpm.api.cmd.Command;
import org.jbpm.api.cmd.Environment;
import org.jbpm.pvm.internal.cmd.CommandService;
import org.jbpm.pvm.internal.job.JobImpl;
import org.jbpm.pvm.internal.jobexecutor.JobExecutor;
import org.jbpm.pvm.internal.util.ReflectUtil;
import org.jbpm.test.JbpmTestCase;


/**
 * @author Tom Baeyens
 */
public class LoadTestCase extends JbpmTestCase {

  protected static final long SECOND = 1000;  
  protected static final long MINUTE = 60 * SECOND;  
  protected static final long HOUR = 60 * MINUTE;  

  protected static long checkInterval = 2000;

  protected static String jobsAvailableQueryText =
      "select count(*) "+
      "from "+JobImpl.class.getName()+" as job "+
      "where ( (job.dueDate is null) or (job.dueDate <= :now) ) "+ 
      "  and ( job.retries > 0 )";

  protected static boolean measureMemory = true;

  protected CommandService commandService;
  protected JobExecutor jobExecutor;
  protected long startTime = -1;
  protected long stopTime = -1;
  protected PrintWriter logFileWriter;
  

  public void setUp() throws Exception {
    super.setUp();
    
    if (measureMemory) {
      openMemoryLogFile();
    }
    
    commandService = processEngine.get(CommandService.class);
    jobExecutor = processEngine.get(JobExecutor.class);
  }
  
  protected void tearDown() throws Exception {
    if (measureMemory) {
      closeMemoryLogFile();
    }
    
    super.tearDown();
  }
  
  public void startMeasuringTime() {
    if (stopTime==-1) {
      startTime = System.currentTimeMillis();
    } else {
      startTime = startTime + (System.currentTimeMillis() - stopTime);
      stopTime = -1;
    }
  }

  public void stopMeasuringTime() {
    stopTime = System.currentTimeMillis();
  }

  protected void openMemoryLogFile() throws Exception {
    String testClass = ReflectUtil.getUnqualifiedClassName(getClass());
    logFileWriter = new PrintWriter(new File("target/"+testClass +".txt"));
    logColumnTitles();
  }

  protected void logColumnTitles() {
    logFileWriter.println("Used Memory");
  }

  protected void closeMemoryLogFile() {
    logFileWriter.close();
  }

  protected void logStatus() {
    logFileWriter.println(getUsedMemory());
  }

  protected long getUsedMemory() {
    Runtime runtime = Runtime.getRuntime();
    long total = runtime.totalMemory();
    long free = runtime.freeMemory();
    long used = total - free;
    return used;
  }

  protected void waitTillNoMoreMessages(JobExecutor jobExecutor) throws Exception {
    boolean jobsAvailable = true;
    while (jobsAvailable) {
      log.debug("going to sleep for " + checkInterval + " millis, waiting for the job executor to process more jobs");
      Thread.sleep(checkInterval);
      jobsAvailable = areJobsAvailable();
      logStatus();
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
  
  public String getMeasuredTime() {
    long stop = stopTime!=-1 ? stopTime : System.currentTimeMillis();
    long diff = stop - startTime;
    
    long hours = diff / HOUR;
    diff = diff - (hours * HOUR);
    
    long minutes = diff / MINUTE;
    diff = diff - (minutes * MINUTE);
    
    long seconds = diff / SECOND;
    
    StringBuilder duration = new StringBuilder();
    if (hours!=0) {
      duration.append(hours+"h");
    }
    duration.append(minutes);
    duration.append("'");
    duration.append(seconds);
    duration.append("\"");

    // long millis = diff - (seconds * SECOND);
    // text.append(","+millis);

    return duration.toString();
  }
}
