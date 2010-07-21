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

import java.io.PrintWriter;

import org.jbpm.test.JbpmTestCase;

/**
 * @author Tom Baeyens
 */
public class LoadTestCase extends JbpmTestCase {

  protected static final long SECOND = 1000;  
  protected static final long MINUTE = 60 * SECOND;  
  protected static final long HOUR = 60 * MINUTE;  

  protected static long checkInterval = 2000;

  protected static boolean measureMemory = true;

  protected long startTime = -1;
  protected long stopTime = -1;
  protected PrintWriter logFileWriter;

  protected void setUp() throws Exception {
    super.setUp();

    if (measureMemory) {
      openMemoryLogFile();
    }
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
    String testClass = getClass().getSimpleName();
    logFileWriter = new PrintWriter("target/"+testClass +".txt");
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
