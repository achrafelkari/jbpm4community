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
package org.jbpm.bpmn.test.intermediatecatch;

import java.util.Calendar;
import java.util.Date;

import org.jbpm.api.JbpmException;
import org.jbpm.api.ProcessInstance;
import org.jbpm.api.job.Job;
import org.jbpm.pvm.internal.util.Clock;
import org.jbpm.test.JbpmTestCase;
import org.jbpm.test.assertion.CollectionAssertions;
import org.jbpm.test.util.DateUtils;

/**
 * @author Joram Barrez
 */
public class IntermediateCatchTimerEventTest extends JbpmTestCase {
  
  private static final String BAD_PROCESS_1 = 
    "<definitions xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance'>" +
    "  <process id='intermediateCatchEvent'>" +
    "    <startEvent id='theStart' />" +
    "    <sequenceFlow id='flow1' sourceRef='theStart' targetRef='intermediateTimer' />" +
    "    <intermediateCatchEvent id='intermediateTimer' >" +
    "      <timerEventDefinition/>" +
    "    </intermediateCatchEvent>" +
    "    <sequenceFlow id='flow2' sourceRef='intermediateTimer' targetRef='theEnd' />" +
    "    <endEvent id='theEnd' />" +
    "  </process>" +
    "</definitions>";
  
  private static final String BAD_PROCESS_2 = 
    "<definitions xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance'>" +
    "  <process id='intermediateCatchEvent'>" +
    "    <startEvent id='theStart' />" +
    "    <sequenceFlow id='flow1' sourceRef='theStart' targetRef='intermediateTimer' />" +
    "    <intermediateCatchEvent id='intermediateTimer' >" +
    "      <timerEventDefinition>" +
    "        <timeCycle>5 hours</timeCycle>" +
    "        <timeDate>10/10/1985</timeDate>" +
    "      </timerEventDefinition>" +
    "    </intermediateCatchEvent>" +
    "    <sequenceFlow id='flow2' sourceRef='intermediateTimer' targetRef='theEnd' />" +
    "    <endEvent id='theEnd' />" +
    "  </process>" +
    "</definitions>";
  
  private static final String TIMER_CATCH_WITH_TIMECYCLE_DURATION =
    "<definitions xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance'>" +
    "  <process id='timeCycleProcess'>" +
    "    <startEvent id='theStart' />" +
    "    <sequenceFlow id='flow1' sourceRef='theStart' targetRef='intermediateTimer' />" +
    "    <intermediateCatchEvent id='intermediateTimer' >" +
    "      <timerEventDefinition>" +
    "        <timeCycle>5 hours</timeCycle>" +
    "      </timerEventDefinition>" +
    "    </intermediateCatchEvent>" +
    "    <sequenceFlow id='flow2' sourceRef='intermediateTimer' targetRef='theEnd' />" +
    "    <endEvent id='theEnd' />" +
    "  </process>" +
    "</definitions>";
  
  private static final String TIMER_CATCH_WITH_TIMEDATE =
    "<definitions xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance'>" +
    "  <process id='timeDateProcess'>" +
    "    <startEvent id='theStart' />" +
    "    <sequenceFlow id='flow1' sourceRef='theStart' targetRef='intermediateTimer' />" +
    "    <intermediateCatchEvent id='intermediateTimer' >" +
    "      <timerEventDefinition>" +
    "        <timeDate>10/10/2099 00:00:00</timeDate>" +
    "      </timerEventDefinition>" +
    "    </intermediateCatchEvent>" +
    "    <sequenceFlow id='flow2' sourceRef='intermediateTimer' targetRef='theEnd' />" +
    "    <endEvent id='theEnd' />" +
    "  </process>" +
    "</definitions>";
  
  private static final String TIMER_CATCH_WITH_CRON_EXPRESSION =
    "<definitions xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance'>" +
    "  <process id='timeDateProcess'>" +
    "    <startEvent id='theStart' />" +
    "    <sequenceFlow id='flow1' sourceRef='theStart' targetRef='intermediateTimer' />" +
    "    <intermediateCatchEvent id='intermediateTimer' >" +
    "      <timerEventDefinition>" +
    "        <timeCycle>0 * * * * ?</timeCycle>" +
    "      </timerEventDefinition>" +
    "    </intermediateCatchEvent>" +
    "    <sequenceFlow id='flow2' sourceRef='intermediateTimer' targetRef='theEnd' />" +
    "    <endEvent id='theEnd' />" +
    "  </process>" +
    "</definitions>";
  
  private static final String TIMER_CATCH_WITH_CRON_EXPRESSION_2 =
    "<definitions xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance'>" +
    "  <process id='timeDateProcess'>" +
    "    <startEvent id='theStart' />" +
    "    <sequenceFlow id='flow1' sourceRef='theStart' targetRef='intermediateTimer' />" +
    "    <intermediateCatchEvent id='intermediateTimer' >" +
    "      <timerEventDefinition>" +
    "        <timeCycle>0 0 23 ? * FRI</timeCycle>" + // Every friday at 23:00
    "      </timerEventDefinition>" +
    "    </intermediateCatchEvent>" +
    "    <sequenceFlow id='flow2' sourceRef='intermediateTimer' targetRef='theEnd' />" +
    "    <endEvent id='theEnd' />" +
    "  </process>" +
    "</definitions>";
  
  
  protected void tearDown() throws Exception {
    Clock.setExplicitTime(null);
    super.tearDown();
  }
  
  public void testInvalidProcess() {
    final String expectedMsg = "requires either a timeDate or a timeCycle definition (but not both)";
    try {
      deployBpmn2XmlString(BAD_PROCESS_1);
      fail();
    } catch (JbpmException e) {
      assertTrue(e.getMessage().contains(expectedMsg));
    }
    
    try {
      deployBpmn2XmlString(BAD_PROCESS_2);
      fail();
    } catch (JbpmException e) {
      assertTrue(e.getMessage().contains(expectedMsg));
    }
  }
  
  public void testTimeCycleExpression() {
    deployBpmn2XmlString(TIMER_CATCH_WITH_TIMECYCLE_DURATION);
    
    long processStartTime = 5000;
    Clock.setExplicitTime(new Date(processStartTime));
    
    ProcessInstance pi = executionService.startProcessInstanceByKey("timeCycleProcess");
    CollectionAssertions.assertContainsSameElements(pi.findActiveActivityNames(), "intermediateTimer");
    
    Job timerJob = managementService.createJobQuery().processInstanceId(pi.getId()).uniqueResult();
    long expectedTimerDueDate = processStartTime + (5 * 60 * 60 * 1000); // expected = 5 hours in ms
    assertEquals(expectedTimerDueDate, timerJob.getDuedate().getTime());
    
    managementService.executeJob(timerJob.getId());
    assertProcessInstanceEnded(pi);
  }
  
  public void testTimeDateExpression() {
    deployBpmn2XmlString(TIMER_CATCH_WITH_TIMEDATE);
    
    ProcessInstance pi = executionService.startProcessInstanceByKey("timeDateProcess");
    CollectionAssertions.assertContainsSameElements(pi.findActiveActivityNames(), "intermediateTimer");
    
    Job timerJob = managementService.createJobQuery().processInstanceId(pi.getId()).uniqueResult();
    Date expectedDueDate = DateUtils.getDateAtMidnight(10, Calendar.OCTOBER, 2099);
    assertEquals(expectedDueDate.getTime(), timerJob.getDuedate().getTime());
    
    managementService.executeJob(timerJob.getId());
    assertProcessInstanceEnded(pi);
  }
  
  public void testCronExpression() {
    deployBpmn2XmlString(TIMER_CATCH_WITH_CRON_EXPRESSION);
    Clock.setExplicitTime(DateUtils.getDate(20, Calendar.JANUARY, 2010, 0, 1, 1)); // Start on 61 seconds
    
    ProcessInstance pi = executionService.startProcessInstanceByKey("timeDateProcess");
    CollectionAssertions.assertContainsSameElements(pi.findActiveActivityNames(), "intermediateTimer");
    
    Job timerJob = managementService.createJobQuery().processInstanceId(pi.getId()).uniqueResult();
    assertEquals(DateUtils.getDate(20, Calendar.JANUARY, 2010, 0, 2, 0).getTime(), timerJob.getDuedate().getTime()); 
    
    managementService.executeJob(timerJob.getId());
    assertProcessInstanceEnded(pi);
  }
  
  public void testCronExpression2() {
    deployBpmn2XmlString(TIMER_CATCH_WITH_CRON_EXPRESSION_2);
    Clock.setExplicitTime(DateUtils.getDateAtMidnight(21, Calendar.JANUARY, 2010)); // 21/01/2009 is a Thursday
    
    ProcessInstance pi = executionService.startProcessInstanceByKey("timeDateProcess");
    CollectionAssertions.assertContainsSameElements(pi.findActiveActivityNames(), "intermediateTimer");
    
    Job timerJob = managementService.createJobQuery().processInstanceId(pi.getId()).uniqueResult();
    assertEquals(DateUtils.getDate(22, Calendar.JANUARY, 2010, 23, 0, 0).getTime(), timerJob.getDuedate().getTime());
    
    managementService.executeJob(timerJob.getId());
    assertProcessInstanceEnded(pi);
  }
  
 


}
