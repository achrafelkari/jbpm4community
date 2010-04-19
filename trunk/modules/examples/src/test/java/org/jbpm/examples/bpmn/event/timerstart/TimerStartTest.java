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
package org.jbpm.examples.bpmn.event.timerstart;

import java.util.Calendar;

import org.jbpm.api.NewDeployment;
import org.jbpm.api.ProcessInstanceQuery;
import org.jbpm.api.job.Job;
import org.jbpm.pvm.internal.util.Clock;
import org.jbpm.test.JbpmTestCase;
import org.jbpm.test.util.DateUtils;


/**
 * @author Joram Barrez
 */
public class TimerStartTest extends JbpmTestCase {
  
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    Clock.setExplicitTime(DateUtils.getDateAtMidnight(10, Calendar.OCTOBER, 2099));
    NewDeployment deployment = repositoryService.createDeployment();
    deployment.addResourceFromClasspath("org/jbpm/examples/bpmn/event/timerstart/timer_start_event.bpmn.xml");
    registerDeployment(deployment.deploy());
  }
  
  @Override
  protected void tearDown() throws Exception {
    Clock.setExplicitTime(null);
    super.tearDown();
  }
  
  public void testTimerStartEventWithDurationAsTimeCycle() {
    
    // After deployment, there should be one job in the database that starts a new process instance
    Job startProcessTimer = managementService.createJobQuery().uniqueResult();
    assertNotNull(startProcessTimer);
    assertEquals(DateUtils.getDate(10, Calendar.OCTOBER, 2099, 10, 0, 0).getTime(), startProcessTimer.getDuedate().getTime());
    
    // Triggering the job should start a new process instance of the deployed process definition
    ProcessInstanceQuery procInstQuery = executionService.createProcessInstanceQuery();
    assertEquals(0, procInstQuery.count());
    
    // need to change current date to calculate the next duedate internally correctl
    Clock.setExplicitTime(DateUtils.getDate(10, Calendar.OCTOBER, 2099, 10, 0, 0)); 
    managementService.executeJob(startProcessTimer.getId());
    assertEquals(1, procInstQuery.count());
    
    // Since a timeCycle was used, the job should have been recreated with a new duedate
    startProcessTimer = managementService.createJobQuery().uniqueResult();
    assertEquals(DateUtils.getDate(10, Calendar.OCTOBER, 2099, 20, 0, 0).getTime(), startProcessTimer.getDuedate().getTime());
    
    
    // So we need to manually delete it
    managementService.deleteJob(Long.valueOf(startProcessTimer.getId()));
  }

}
