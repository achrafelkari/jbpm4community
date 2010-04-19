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

import org.jbpm.api.ProcessInstance;
import org.jbpm.api.job.Job;


/**
 * Test case for tests that only test basic usage (ie no corner cases).
 * 
 * @author Joram Barrez
 */
public class BasicUsageTest extends ConcurrentJbpmTestCase {
  
  // Test for JBPM-2331
  public void testBasicTimeout() {
    deployJpdlXmlString(
            "<process key='TimerTest' name='Timer Test'>" +
            "  <start name='thestart'>" +
            "    <transition to='wait'/>" +
            "  </start>" +
            "  <state name='wait'>" +
            "    <transition name='timeout' to='end'>" +
            "      <timer duedate='30 seconds'/>" +
            "    </transition>" +
            "  </state>" +
            "  <end name='end'/>" +
            "</process>"
          );

    ProcessInstance processInstance = executionService.startProcessInstanceByKey("TimerTest");
    assertActivityActive(processInstance.getId(), "wait");
    
    Job job = managementService.createJobQuery()
      .processInstanceId(processInstance.getId())
      .uniqueResult();
    
    SynchronizableCommandExecutor executor = new SynchronizableCommandExecutor(environmentFactory, job);
    executor.start();
    
    try {
      executor.join();
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    
    assertProcessInstanceEnded(processInstance);
  }

}
