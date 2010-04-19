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
package org.jbpm.test.activity.task;

import java.util.HashMap;
import java.util.Map;

import org.jbpm.api.JbpmException;
import org.jbpm.api.history.HistoryTask;
import org.jbpm.test.JbpmTestCase;

/**
 * Testcase for the several ways of completing a task.
 * 
 * @author jbarrez
 */
public class TaskCompletionTest extends JbpmTestCase {

  private static final String PROCESS = 
    "<process name='taskCompletion'>" +
    "  <start>" +
    "    <transition to='theTask' />" +
    "  </start>" +
    "  <task name='theTask' assignee='johndoe'>" +
    "    <transition name='one' to='stateOne' />" +
    "    <transition name='two' to='stateTwo' />" +
    "  </task>" +
    "  <state name='stateOne'>" + 
    "    <transition to='theEnd' />" +
    "  </state>" +
    "  <state name='stateTwo'>" + 
    "    <transition to='theEnd' />" +
    "  </state>" +
    "  <end name='theEnd' />" +
    "</process>";
  
  public void testCompletionWithNullOrEmptyId() {
    try {
      taskService.completeTask(null);
      fail();
    } catch (JbpmException e) {
      // exception should be thrown
    }
    try {
      taskService.completeTask("");
      fail();
    } catch (JbpmException e) {
      // exception should be thrown
    }
  }
  
  public void testCompletionWithInvalidTaskId() {
    try {
      taskService.completeTask(Long.toString(-123456789L));
      fail();
    } catch (JbpmException e) {
      // exception should be thrown
    }
  }
  
  public void testCompletionWithOutcome() {
    Ids ids = deployAndStartProcessInstance();
    taskService.completeTask(ids.taskId, "one");
    
    assertActivityActive(ids.processInstanceId, "stateOne");
    assertNotActivityActive(ids.processInstanceId, "stateTwo");
    
    assertNoOpenTasks(ids.processInstanceId);
    assertHistoryTaskCreated(ids.processInstanceId, "one");
  }
  
  // Test for JBPM-2425
  public void testCompletionWithInvalidOutcome() {
    Ids ids = deployAndStartProcessInstance();
    
    try {
      taskService.completeTask(ids.taskId, "doesn't exist");
      fail();
    } catch (JbpmException e) {
      // exception should be thrown
    }

    // Task should still be open now (rollback in db)
    assertNotNull("After completion with an invalid outcome, the task should remain unchanged",
            taskService.createTaskQuery().processInstanceId(ids.processInstanceId).uniqueResult());
  }
  
  public void testCompletionWithVariables() {
    Ids ids = deployAndStartProcessInstance();
    Map<String, Object> vars = new HashMap<String, Object>();
    vars.put("testVar", "testValue");
    taskService.completeTask(ids.taskId, "one",vars);
    
    assertEquals("testValue", executionService.getVariable(ids.processInstanceId, "testVar"));
    assertNoOpenTasks(ids.processInstanceId);
    assertHistoryTaskCreated(ids.processInstanceId, null);
  }
  
  public void testCompletionWithOutcomeAndVariables() {
    Ids ids = deployAndStartProcessInstance();
    Map<String, Object> vars = new HashMap<String, Object>();
    vars.put("testVar", "testValue");
    taskService.completeTask(ids.taskId, "two",vars);
    
    assertEquals("testValue", executionService.getVariable(ids.processInstanceId, "testVar"));
    assertActivityActive(ids.processInstanceId, "stateTwo");
    
    assertNoOpenTasks(ids.processInstanceId);
    assertHistoryTaskCreated(ids.processInstanceId, "two");
  }
  
  /** 
   * Returns the process instance id and the taskId of the single task 
   * that is open after process start 
   */
  private Ids deployAndStartProcessInstance() {
    deployJpdlXmlString(PROCESS);
    Ids result = new Ids();
    result.processInstanceId = executionService.startProcessInstanceByKey("taskCompletion").getId();
    result.taskId = taskService.createTaskQuery().processInstanceId(result.processInstanceId).uniqueResult().getId();
    return result;
  }
  
  private void assertHistoryTaskCreated(String executionId, String historicalOutcome) {
    HistoryTask historyTask = historyService.createHistoryTaskQuery()
                                            .executionId(executionId)
                                            .uniqueResult();
    assertNotNull(historyTask);
    if (historicalOutcome != null) {
      assertEquals(historicalOutcome, historyTask.getOutcome());
    }
  }
  
  /* Just a wrapper for two ids, since Java doesnt allow to return multiple values */
  private class Ids {
    
    private String processInstanceId;
    private String taskId;
    
  }
  
}
