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
package org.jbpm.test.timer;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

import org.jbpm.api.Execution;
import org.jbpm.api.JbpmException;
import org.jbpm.api.ProcessInstance;
import org.jbpm.api.activity.ActivityExecution;
import org.jbpm.api.activity.ExternalActivityBehaviour;
import org.jbpm.api.job.Job;
import org.jbpm.api.listener.EventListener;
import org.jbpm.api.listener.EventListenerExecution;
import org.jbpm.pvm.internal.util.Clock;
import org.jbpm.test.JbpmTestCase;

/**
 */
public class TimerTransitionAsLoopTest extends JbpmTestCase {

  private static final String TEST_PROCESS_TIMER =
    "<process name='TimerTransitionAsLoop' xmlns='http://jbpm.org/4.3/jpdl'>"
   +"  <start name='start1' g='8,77,48,48'>"
   +"    <transition name='to wait' to='wait for status' g='-24,-18'/>"
   +"  </start>"
   +"  <state name='wait for status' g='134,75,112,52'>"
   +"    <transition name='to get status' to='get status' g='-45,-18'>"
   +"      <timer duedate='2 minutes'/>"
   +"    </transition>"
   +"  </state>"
   +"  <script g='333,80,92,52' name='get status' expr='#{true}' var='status'>"
   +"    <transition name='transition 1' g='261,198:-17,-3' to='wait for status'/>"
   +"  </script>"
   +"</process>";

  public void testTimerTransitionAsLoop() {
    deployJpdlXmlString(TEST_PROCESS_TIMER);

    ProcessInstance processInstance = executionService.startProcessInstanceByKey("TimerTransitionAsLoop");

    Job job = managementService.createJobQuery()
      .processInstanceId(processInstance.getId())
      .uniqueResult();

    managementService.executeJob(job.getId());
  }

  private static final String TEST_TIMER_TRANSITION_SELF = ""
    + "<?xml version='1.0' encoding='UTF-8'?>"
    + "<process name='TimerTransition' xmlns='http://jbpm.org/4.3/jpdl'>"
    + "  <start g='19,50,48,48'>"
    + "    <transition to='guardedWait' />"
    + "  </start>"
    + "  <state name='guardedWait' g='98,46,127,52'>"
    + "    <transition name='go on' to='next step' g='-16,-17'/>"
    + "    <transition name='timeout' to='escalation' g='-43,-16'>"
    + "      <timer duedate='1 hour' />"
    + "    </transition>"
    + "    <transition name='selft' to='guardedWait' />"
    + "  </state>"
    + "  <state name='next step' g='283,46,83,53'/>"
    + "  <state name='escalation' g='118,140,88,52' />"
    + "</process>";

  public void testTimerTransitionContinueBeforeTimerFires() {
    deployJpdlXmlString(TEST_TIMER_TRANSITION_SELF);

    ProcessInstance processInstance = executionService
        .startProcessInstanceByKey("TimerTransition");

    String executionId = processInstance.findActiveExecutionIn("guardedWait").getId();

    // goto self activity
    executionService.signalExecutionById(executionId, "selft");

    // retrieve process instance
    processInstance = executionService.findProcessInstanceById(processInstance.getId());

    // retrieve execution id
    executionId = processInstance.findActiveExecutionIn("guardedWait").getId();

    // signal to next step
    executionService.signalExecutionById(executionId, "go on");

    // retrieve process instance
    processInstance = executionService.findProcessInstanceById(processInstance.getId());

    assertTrue(processInstance.isActive("next step"));

    // verify there is not any jobs any more
    List<Job> jobs = managementService.createJobQuery()
      .processInstanceId(processInstance.getId())
      .list();

    assertEquals(new ArrayList<Job>(), new ArrayList<Job>(jobs));
  }

}
