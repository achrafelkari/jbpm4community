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
package org.jbpm.bpmn.test.startevent;

import java.util.Calendar;
import java.util.List;

import org.jbpm.api.JbpmException;
import org.jbpm.api.ProcessDefinition;
import org.jbpm.api.ProcessDefinitionQuery;
import org.jbpm.api.ProcessInstance;
import org.jbpm.api.ProcessInstanceQuery;
import org.jbpm.api.job.Job;
import org.jbpm.pvm.internal.util.Clock;
import org.jbpm.test.JbpmTestCase;
import org.jbpm.test.util.DateUtils;


/**
 * @author Joram Barrez
 */
public class TimerStartEventTest extends JbpmTestCase {
  
  private static final String INVALID_PROCESS_1 = 
    "<definitions xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance'>" +
    "  <process id='invalidProcess1'>" +
    "    <startEvent id='theStart' >" +
    "      <timerEventDefinition />" +
    "    </startEvent>" +
    "    <sequenceFlow id='flow1' sourceRef='theStart' targetRef='theEnd' />" +
    "    <endEvent id='theEnd' />" +
    "  </process>" +
    "</definitions>";
  
  private static final String INVALID_PROCESS_2 = 
    "<definitions xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance'>" +
    "  <process id='invalidProcess1'>" +
    "    <startEvent id='theStart' >" +
    "      <timerEventDefinition >" +
    "        <timeCycle>5 hours</timeCycle>" +
    "        <timeDate>10/10/1985</timeDate>" +
    "      </timerEventDefinition>" +
    "    </startEvent>" +
    "    <sequenceFlow id='flow1' sourceRef='theStart' targetRef='theEnd' />" +
    "    <endEvent id='theEnd' />" +
    "  </process>" +
    "</definitions>";
  
  private static final String INVALID_PROCESS_3 = 
    "<definitions xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance'>" +
    "  <process id='invalidProcess1'>" +
    "    <startEvent id='theStart' >" +
    "      <timerEventDefinition >" +
    "        <timeCycle>5 abcdefghijklmnop</timeCycle>" +
    "      </timerEventDefinition>" +
    "    </startEvent>" +
    "    <sequenceFlow id='flow1' sourceRef='theStart' targetRef='theEnd' />" +
    "    <endEvent id='theEnd' />" +
    "  </process>" +
    "</definitions>";
  
  private static final String INVALID_PROCESS_4 = 
    "<definitions xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance'>" +
    "  <process id='invalidProcess1'>" +
    "    <startEvent id='theStart' >" +
    "      <timerEventDefinition >" +
    "        <timeCycle>Z 0 22 * * ?</timeCycle>" +
    "      </timerEventDefinition>" +
    "    </startEvent>" +
    "    <sequenceFlow id='flow1' sourceRef='theStart' targetRef='theEnd' />" +
    "    <endEvent id='theEnd' />" +
    "  </process>" +
    "</definitions>";
  
  private static final String TIMER_START_FIXED_DUEDATE = 
    "<definitions xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance'>" +
    "  <process id='timerStartFixedDueDate'>" +
    "    <startEvent id='theStart' >" +
    "      <timerEventDefinition >" +
    "        <timeDate>10/10/2099 00:00:00</timeDate>" +
    "      </timerEventDefinition>" +
    "    </startEvent>" +
    "    <sequenceFlow id='flow1' sourceRef='theStart' targetRef='wait' />" +
    "    <receiveTask id='wait' />" +
    "    <sequenceFlow id='flow2' sourceRef='wait' targetRef='theEnd' />" +
    "    <endEvent id='theEnd' />" +
    "  </process>" +
    "</definitions>";
  
  private static final String TIMER_START_TIMECYCLE_DURATION = 
    "<definitions xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance'>" +
    "  <process id='timerStartTimeCycleDuration'>" +
    "    <startEvent id='theStart' >" +
    "      <timerEventDefinition >" +
    "        <timeCycle>10 hours</timeCycle>" +
    "      </timerEventDefinition>" +
    "    </startEvent>" +
    "    <sequenceFlow id='flow1' sourceRef='theStart' targetRef='wait' />" +
    "    <receiveTask id='wait' />" +
    "    <sequenceFlow id='flow2' sourceRef='wait' targetRef='theEnd' />" +
    "    <endEvent id='theEnd' />" +
    "  </process>" +
    "</definitions>";
  
  private static final String TIMER_START_TIMECYCLE_CRON_EXPR = 
    "<definitions xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance'>" +
    "  <process id='timerStartTimeCycleCronExpression' name='timerStartCron'>" +
    "    <startEvent id='theStart' >" +
    "      <timerEventDefinition >" +
    "        <timeCycle>0 0 22 * * ?</timeCycle>" + // every day at 22:00
    "      </timerEventDefinition>" +
    "    </startEvent>" +
    "    <sequenceFlow id='flow1' sourceRef='theStart' targetRef='wait' />" +
    "    <receiveTask id='wait' />" +
    "    <sequenceFlow id='flow2' sourceRef='wait' targetRef='theEnd' />" +
    "    <endEvent id='theEnd' />" +
    "  </process>" +
    "</definitions>";
  
  @Override
  protected void tearDown() throws Exception {
    Clock.setExplicitTime(null);
    super.tearDown();
  }
  
  public void testInvalidProcess() {
    testDeployInvalidProcess(INVALID_PROCESS_1);
    testDeployInvalidProcess(INVALID_PROCESS_2);
    testDeployInvalidProcess(INVALID_PROCESS_3);
    testDeployInvalidProcess(INVALID_PROCESS_4);
  }
  
  private void testDeployInvalidProcess(String process) {
    try {
      deployBpmn2XmlString(process);
      fail();
    } catch (JbpmException e) {
      // Exception is expected
    }
  }
  
  public void testTimerStartEventWithFixedDuedate() {
    deployBpmn2XmlString(TIMER_START_FIXED_DUEDATE);
    
    // After deployment, there should be one job in the database that starts a new process instance
    Job startProcessTimer = managementService.createJobQuery().uniqueResult();
    assertNotNull(startProcessTimer);
    assertEquals(DateUtils.getDateAtMidnight(10, Calendar.OCTOBER, 2099).getTime(), startProcessTimer.getDueDate().getTime());
    
    ProcessInstanceQuery procInstQuery = executionService.createProcessInstanceQuery()
                            .processDefinitionId(findProcessDefinitionId("timerStartFixedDueDate"));
    
    // Triggering the job should start a new process instance of the deployed process definition
    assertEquals(0, procInstQuery.count());
    managementService.executeJob(startProcessTimer.getId());
    assertEquals(1, procInstQuery.count());
    
    // Since a fixed duedate was used, the job should have been deleted from the database
    startProcessTimer = managementService.createJobQuery().uniqueResult();
    assertNull(startProcessTimer);
  }
  
  public void testTimerStartEventWithDurationAsTimeCycle() {
    Clock.setExplicitTime(DateUtils.getDateAtMidnight(10, Calendar.OCTOBER, 2099));
    deployBpmn2XmlString(TIMER_START_TIMECYCLE_DURATION);
    
    // After deployment, there should be one job in the database that starts a new process instance
    Job startProcessTimer = managementService.createJobQuery().uniqueResult();
    assertNotNull(startProcessTimer);
    assertEquals(DateUtils.getDate(10, Calendar.OCTOBER, 2099, 10, 0, 0).getTime(), startProcessTimer.getDueDate().getTime());
    
    // Triggering the job should start a new process instance of the deployed process definition
    ProcessInstanceQuery procInstQuery = executionService.createProcessInstanceQuery()
                      .processDefinitionId(findProcessDefinitionId("timerStartTimeCycleDuration"));
    assertEquals(0, procInstQuery.count());
    
    // need to change current date to calculate the next duedate internally correctly
    Clock.setExplicitTime(DateUtils.getDate(10, Calendar.OCTOBER, 2099, 10, 0, 0)); 
    managementService.executeJob(startProcessTimer.getId());
    assertEquals(1, procInstQuery.count());
    
    // Since a timeCycle was used, the job should have been recreated with a new duedate
    startProcessTimer = managementService.createJobQuery().uniqueResult();
    assertEquals(DateUtils.getDate(10, Calendar.OCTOBER, 2099, 20, 0, 0).getTime(), startProcessTimer.getDueDate().getTime());
    
    
    // So we need to manually delete it
    managementService.deleteJob(Long.valueOf(startProcessTimer.getId()));
  }
  
  public void testTimerStartEventWithCronExpressionAsTimeCycle() {
    Clock.setExplicitTime(DateUtils.getDateAtMidnight(10, Calendar.OCTOBER, 2099));
    deployBpmn2XmlString(TIMER_START_TIMECYCLE_CRON_EXPR);
    
    // After deployment, there should be one job in the database that starts a new process instance
    Job startProcessTimer = managementService.createJobQuery().uniqueResult();
    assertNotNull(startProcessTimer);
    assertEquals(DateUtils.getDate(10, Calendar.OCTOBER, 2099, 22, 0, 0).getTime(), startProcessTimer.getDueDate().getTime());
    
    // Triggering the job should start a new process instance of the deployed process definition
    ProcessInstanceQuery procInstQuery = executionService.createProcessInstanceQuery()
                      .processDefinitionId(findProcessDefinitionId("timerStartTimeCycleCronExpression"));
    assertEquals(0, procInstQuery.count());
    
    // need to change current date to calculate the next duedate correctly
    Clock.setExplicitTime(DateUtils.getDate(10, Calendar.OCTOBER, 2099, 22, 0, 0)); 
    managementService.executeJob(startProcessTimer.getId());
    assertEquals(1, procInstQuery.count());
    
    // Since a timeCycle was used, the job should have been recreated with a new duedate
    startProcessTimer = managementService.createJobQuery().uniqueResult();
    assertEquals(DateUtils.getDate(11, Calendar.OCTOBER, 2099, 22, 0, 0).getTime(), startProcessTimer.getDueDate().getTime());
    
    // So we need to manually delete it
    managementService.deleteJob(Long.valueOf(startProcessTimer.getId()));
  }
  
  public void testDeleteProcessDefinitionBeforeTimerTriggers() {
    Clock.setExplicitTime(DateUtils.getDateAtMidnight(10, Calendar.OCTOBER, 2099));
    String deployId = deployBpmn2XmlString(TIMER_START_TIMECYCLE_DURATION);
    
    // Delete the process definition
    String procDefId = findProcessDefinitionId("timerStartTimeCycleDuration");
    repositoryService.deleteDeploymentCascade(deployId);
    registeredDeployments.remove(0);
    
    // After process definition deletion, the timer is still in the database
    Job startProcessTimer = managementService.createJobQuery().uniqueResult();
    assertNotNull(startProcessTimer);
    
    // When the timer triggers, it notices that the process definition is gone and it deletes itselves
    // No new process instance should have been started
    ProcessInstanceQuery procInstQuery = executionService.createProcessInstanceQuery().processDefinitionId(procDefId);
    assertEquals(0, procInstQuery.count());
    
    managementService.executeJob(startProcessTimer.getId());
    startProcessTimer = managementService.createJobQuery().uniqueResult();
    assertNull(startProcessTimer);
    assertEquals(0, procInstQuery.count());
  }
  
  /*
   * Processes with a timer start event should be createable through the executionService.
   */
  public void testStartProcessInstanceByKey() {
    deployBpmn2XmlString(TIMER_START_TIMECYCLE_DURATION);
    ProcessInstance pi = executionService.startProcessInstanceByKey("timerStartTimeCycleDuration");
    assertNotNull(pi);
    
    Job startProcessTimer = managementService.createJobQuery().uniqueResult();
    managementService.deleteJob(Long.valueOf(startProcessTimer.getId()));
  }
  
  public void testOnlyOneStartProcessActiveAfterRedeploy() {
    deployBpmn2XmlString(TIMER_START_TIMECYCLE_DURATION);
    List<Job> startProcessTimers = managementService.createJobQuery().list();
    assertEquals(1, startProcessTimers.size());
    String firstJobId = startProcessTimers.get(0).getId();
    
    // Redeploy -> first job should be deleted
    deployBpmn2XmlString(TIMER_START_TIMECYCLE_DURATION);
    assertEquals(2, repositoryService.createProcessDefinitionQuery().list().size());
    startProcessTimers = managementService.createJobQuery().list();
    assertEquals(1, startProcessTimers.size());
    
    String secondJobId = startProcessTimers.get(0).getId();
    assertTrue(!firstJobId.equals(secondJobId));
    
    managementService.deleteJob(Long.valueOf(secondJobId));
  }
  
  public void testLatestProcessDefinitionUsedAfterRedeploy() {
    deployBpmn2XmlString(TIMER_START_TIMECYCLE_DURATION);
    Job startProcessTimer = managementService.createJobQuery().uniqueResult();
    
    // Redeploy 
    deployBpmn2XmlString(TIMER_START_TIMECYCLE_DURATION);
    List<ProcessDefinition> procDefs = repositoryService.createProcessDefinitionQuery()
                                          .orderAsc(ProcessDefinitionQuery.PROPERTY_VERSION).list();
    assertEquals(2, procDefs.size());
    
    // Firing start process timer -> new process instance for latests version of proc def
    startProcessTimer = managementService.createJobQuery().uniqueResult();
    managementService.executeJob(startProcessTimer.getId());
    ProcessInstance procInst = executionService.createProcessInstanceQuery().uniqueResult();
    assertEquals(procDefs.get(1).getId(), procInst.getProcessDefinitionId());
    
    managementService.deleteJob(Long.valueOf(startProcessTimer.getId()));
  }
  
  private String findProcessDefinitionId(String processDefinitionKey) {
    return repositoryService.createProcessDefinitionQuery()
              .processDefinitionName(processDefinitionKey).uniqueResult().getId();
  }

}
