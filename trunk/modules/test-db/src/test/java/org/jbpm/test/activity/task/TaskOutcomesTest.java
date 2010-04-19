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

import java.util.HashSet;
import java.util.Set;

import org.jbpm.api.JbpmException;
import org.jbpm.api.ProcessInstance;
import org.jbpm.api.task.Task;
import org.jbpm.test.JbpmTestCase;


/**
 * @author Tom Baeyens
 */
public class TaskOutcomesTest extends JbpmTestCase {
  
  private static final String UNNAMED_TRANSITION_PROCESS_NAME = "UnnamedTransition";
  
  private static final String UNNAMED_TRANSITION_PROCESS =
    "<process name='" + UNNAMED_TRANSITION_PROCESS_NAME +"'>" +
    "  <start>" +
    "    <transition to='review' />" +
    "  </start>" +
    "  <task name='review' " +
    "        assignee='johndoe'>" +
    "    <transition to='wait' />" +
    "  </task>" +
    "  <state name='wait'/>" +
    "</process>";
  
  private static final String NAMED_TRANSITION_PROCESS_NAME = "NamedTransition";
  
  private static final String NAMED_TRANSITION_PROCESS =
    "<process name='" +  NAMED_TRANSITION_PROCESS_NAME +"'>" +
    "  <start>" +
    "    <transition to='review' />" +
    "  </start>" +
    "  <task name='review' " +
    "        assignee='johndoe'>" +
    "    <transition name='theOneAndOnly' to='wait' />" +
    "  </task>" +
    "  <state name='wait'/>" +
    "</process>";
  
  private static final String NO_TRANSITION_PROCESS_NAME = "NoTransition";
  
  private static final String NO_TRANSITION_PROCESS = 
    "<process name='" + NO_TRANSITION_PROCESS_NAME + "'>" +
    "  <start>" +
    "    <transition to='review' />" +
    "  </start>" +
    "  <task name='review' " +
    "        assignee='johndoe'>" +
    "  </task>" +
    "  <state name='wait'/>" +
    "</process>";
  
  private static final String MULTIPLE_NAMED_TRANSITIONS_PROCESS_NAME = "MultipleTransitions";
  
  private static final String MULTIPLE_NAMED_TRANSITIONS_PROCESS = 
    "<process name='" + MULTIPLE_NAMED_TRANSITIONS_PROCESS_NAME + "'>" +
    "  <start>" +
    "    <transition to='review' />" +
    "  </start>" +
    "  <task name='review' " +
    "        assignee='johndoe'>" +
    "    <transition name='left' to='wait1' />" +
    "    <transition name='middle' to='wait2' />" +
    "    <transition name='right' to='wait3' />" +
    "  </task>" +
    "  <state name='wait1'/>" +
    "  <state name='wait2'/>" +
    "  <state name='wait3'/>" +
    "</process>";
  
  private static final String MULTIPLE_NOT_ALL_NAMED_TRANSITIONS_PROCESS_NAME = "MultipleTransitionsWithUnnamed";
  
  private static final String MULTIPLE_NOT_ALL_NAMED_TRANSITIONS_PROCESS = 
    "<process name='" + MULTIPLE_NOT_ALL_NAMED_TRANSITIONS_PROCESS_NAME + "'>" +
    "  <start>" +
    "    <transition to='review' />" +
    "  </start>" +
    "  <task name='review' " +
    "        assignee='johndoe'>" +
    "    <transition name='left' to='wait1' />" +
    "    <transition to='wait2' />" +
    "    <transition name='right' to='wait3' />" +
    "  </task>" +
    "  <state name='wait1'/>" +
    "  <state name='wait2'/>" +
    "  <state name='wait3'/>" +
    "</process>";
  
  
  public void testGetOutcomesNoTransition() {
    deployJpdlXmlString(NO_TRANSITION_PROCESS);
    executionService.startProcessInstanceByKey(NO_TRANSITION_PROCESS_NAME);
    
    Task task = taskService.createTaskQuery().assignee("johndoe").uniqueResult();
    Set<String> outcomes = taskService.getOutcomes(task.getId());
    
    Set<String> expectedOutcomes = new HashSet<String>();
    assertEquals(expectedOutcomes, outcomes);
  }

  public void testGetOutcomesSingleUnnamedTransition() {
    deployJpdlXmlString(UNNAMED_TRANSITION_PROCESS);
    executionService.startProcessInstanceByKey(UNNAMED_TRANSITION_PROCESS_NAME);
    
    Task task = taskService.createTaskQuery().assignee("johndoe").uniqueResult();
    
    Set<String> outcomes = taskService.getOutcomes(task.getId());
    Set<String> expectedOutcomes = new HashSet<String>();
    expectedOutcomes.add(null);
    
    assertEquals(expectedOutcomes, outcomes);
  }

  public void testGetOutcomesSingleNamedTransition() {
    deployJpdlXmlString(NAMED_TRANSITION_PROCESS);
    executionService.startProcessInstanceByKey(NAMED_TRANSITION_PROCESS_NAME);
    
    Task task = taskService.createTaskQuery().assignee("johndoe").uniqueResult();
    Set<String> outcomes = taskService.getOutcomes(task.getId());
    
    Set<String> expectedOutcomes = new HashSet<String>();
    expectedOutcomes.add("theOneAndOnly");
    
    assertEquals(expectedOutcomes, outcomes);
  }

  public void testGetOutcomesMultipleTransitionsAllNamed() {
    deployJpdlXmlString(MULTIPLE_NAMED_TRANSITIONS_PROCESS);
    executionService.startProcessInstanceByKey(MULTIPLE_NAMED_TRANSITIONS_PROCESS_NAME);
    
    Task task = taskService.createTaskQuery().assignee("johndoe").uniqueResult();
    Set<String> outcomes = taskService.getOutcomes(task.getId());
    
    Set<String> expectedOutcomes = new HashSet<String>();
    expectedOutcomes.add("left");
    expectedOutcomes.add("right");
    expectedOutcomes.add("middle");
    
    assertEquals(expectedOutcomes, outcomes);
  }
  
  public void testGetOutcomesMultipleTransitionsNotAllNamed() {
    deployJpdlXmlString(MULTIPLE_NOT_ALL_NAMED_TRANSITIONS_PROCESS);
    executionService.startProcessInstanceByKey(MULTIPLE_NOT_ALL_NAMED_TRANSITIONS_PROCESS_NAME);
    
    Task task = taskService.createTaskQuery().assignee("johndoe").uniqueResult();
    Set<String> outcomes = taskService.getOutcomes(task.getId());
    
    Set<String> expectedOutcomes = new HashSet<String>();
    expectedOutcomes.add("left");
    expectedOutcomes.add(null);
    expectedOutcomes.add("right");
    
    assertEquals(expectedOutcomes, outcomes);
  }

  /**
   * If a  task has one outgoing transition without a name then
   * taskService.completeTask(taskId) will take that outgoing transition
   */
  public void testCompleteTaskWithOneUnnamedTransitionById() {
    deployJpdlXmlString(UNNAMED_TRANSITION_PROCESS);  
    ProcessInstance processInstance = executionService.startProcessInstanceByKey(UNNAMED_TRANSITION_PROCESS_NAME);
    
    Task task = taskService.createTaskQuery().assignee("johndoe").uniqueResult();
    taskService.completeTask(task.getId());
    
    processInstance = executionService.findProcessInstanceById(processInstance.getId());
    assertNotNull(processInstance.findActiveExecutionIn("wait"));
  }
  
  /**
   * If a  task has one outgoing transition without a name then
   * taskService.completeTask(taskId, null) will take that outgoing transition 
   */
  public void testCompleteTaskWithOneUnnamedTransitionByIdAndNullTransition() {
    deployJpdlXmlString(UNNAMED_TRANSITION_PROCESS);  
    ProcessInstance processInstance = executionService.startProcessInstanceByKey(UNNAMED_TRANSITION_PROCESS_NAME);
    
    Task task = taskService.createTaskQuery().assignee("johndoe") .uniqueResult();
    taskService.completeTask(task.getId(), (String) null);
    
    processInstance = executionService.findProcessInstanceById(processInstance.getId());
    assertNotNull(processInstance.findActiveExecutionIn("wait"));
  }
  
  /**
   * If a  task has one outgoing transition without a name then
   * taskService.completeTask(taskId, "anyvalue") will result in an exception 
   */
  public void testCompleteTaskWithOneUnnamedTransitionByIdAndWrongTransition() {
    deployJpdlXmlString(UNNAMED_TRANSITION_PROCESS);  
    executionService.startProcessInstanceByKey(UNNAMED_TRANSITION_PROCESS_NAME);
    
    Task task = taskService.createTaskQuery().assignee("johndoe") .uniqueResult();
    try {
      taskService.completeTask(task.getId(), "anyValue");
      fail();
    } catch (JbpmException e) {
      // Exception is expected
      e.printStackTrace();
    }
    assertNotNull("After completion with an invalid outcome, the task should remain unchanged",
            taskService.createTaskQuery().assignee("johndoe") .uniqueResult());
  }

  /**
   * If a task has one named transition, then taskService.completeTask(id)
   * will take that transition since there is only one.
   */
  public void testCompleteTaskWithSingleNamedTransitionById() {
    deployJpdlXmlString(NAMED_TRANSITION_PROCESS);
    ProcessInstance processInstance = executionService.startProcessInstanceByKey(NAMED_TRANSITION_PROCESS_NAME);
    
    Task task = taskService.createTaskQuery().assignee("johndoe").uniqueResult();

    taskService.completeTask(task.getId());
    processInstance = executionService.findProcessInstanceById(processInstance.getId());
    assertNotNull(processInstance.findActiveExecutionIn("wait")); 
  }
  
  /**
   * If a task has one named transition, then taskService.completeTask(id, null)
   * will result in an exception since there is no unnamed transition.
   */
  public void testCompleteTaskWithSingleNamedTransitionByIdAndNullTransition() {
    deployJpdlXmlString(NAMED_TRANSITION_PROCESS);
    executionService.startProcessInstanceByKey(NAMED_TRANSITION_PROCESS_NAME);
    
    Task task = taskService.createTaskQuery().assignee("johndoe").uniqueResult();
    try {
      taskService.completeTask(task.getId(), (String) null);
      fail();
    } catch(JbpmException e) {
      // Exception is expected
    }
    assertNotNull(taskService.createTaskQuery().assignee("johndoe").uniqueResult());
  }
  
  /**
   * If a task has one named transition, then 
   * taskService.completeTask(taskId, "anyvalue") will result in an exception 
   */
  public void testCompleteTaskWithSingleNamedTransitionByIdAndWrongTransition() {
    deployJpdlXmlString(NAMED_TRANSITION_PROCESS);
    executionService.startProcessInstanceByKey(NAMED_TRANSITION_PROCESS_NAME);
    
    Task task = taskService.createTaskQuery().assignee("johndoe").uniqueResult();

    try {
      taskService.completeTask(task.getId(), "anyValue");
      fail();
    } catch (JbpmException e) {
      // Exception is expected
    } 
    assertNotNull("After completion with an invalid outcome, the task should remain unchanged",
            taskService.createTaskQuery().assignee("johndoe") .uniqueResult());
  }
 
  public void testCompleteTaskWithSingleNamedTransitionByIdAndTransition() {
    deployJpdlXmlString(NAMED_TRANSITION_PROCESS);
    ProcessInstance processInstance = executionService.startProcessInstanceByKey(NAMED_TRANSITION_PROCESS_NAME);
    
    Task task = taskService.createTaskQuery().assignee("johndoe").uniqueResult();
    taskService.completeTask(task.getId(), "theOneAndOnly");
    assertActivityActive(processInstance.getId(), "wait");
  }

  /**
   * If a task has multiple outgoing transitions (but not all are named), then
   * taskService.completeTask(taskId) will take the transition without a name 
   */
  public void testCompleteTaskWithMultipleTransitionsNotAllNamedById() {
    deployJpdlXmlString(MULTIPLE_NOT_ALL_NAMED_TRANSITIONS_PROCESS);
    ProcessInstance processInstance = executionService.startProcessInstanceByKey(MULTIPLE_NOT_ALL_NAMED_TRANSITIONS_PROCESS_NAME);
    
    Task task = taskService.createTaskQuery().assignee("johndoe").uniqueResult(); 
    taskService.completeTask(task.getId());
    assertActivityActive(processInstance.getId(), "wait2");
  }
  
  /**
   * If a task has multiple outgoing transitions (but not all are named), then
   * taskService.completeTask(taskId, null) will take the transition without a name 
   */
  public void testCompleteTaskWithMultipleTransitionsNotAllNamedByIdAndNullTransition() {
    deployJpdlXmlString(MULTIPLE_NOT_ALL_NAMED_TRANSITIONS_PROCESS);
    ProcessInstance processInstance = executionService.startProcessInstanceByKey(MULTIPLE_NOT_ALL_NAMED_TRANSITIONS_PROCESS_NAME);
    
    Task task = taskService.createTaskQuery().assignee("johndoe").uniqueResult(); 
    taskService.completeTask(task.getId(), (String) null);
    assertActivityActive(processInstance.getId(), "wait2");
  }
  
  /**
   * If a task has multiple outgoing transitions (but not all are named), then
   * taskService.completeTask(taskId, "anyvalue") will result in an exception 
   */
  public void testCompleteTaskWithMultipleTransitionsNotAllNamedByIdAndWrongTransition() {
    deployJpdlXmlString(MULTIPLE_NOT_ALL_NAMED_TRANSITIONS_PROCESS);
    executionService.startProcessInstanceByKey(MULTIPLE_NOT_ALL_NAMED_TRANSITIONS_PROCESS_NAME);
    
    Task task = taskService.createTaskQuery().assignee("johndoe").uniqueResult(); 
    
    try {
      taskService.completeTask(task.getId(), "anyValue");
      fail();
    } catch (JbpmException e) {
      // Exception is expected
    }
    assertNotNull("After completion with an invalid outcome, the task should remain unchanged",
            taskService.createTaskQuery().assignee("johndoe") .uniqueResult());
  }
  
  /**
   * If a task has multiple outgoing transitions (but not all are named), then
   * taskService.completeTask(taskId, "correctTransition") will take the correct transition
   */
  public void testCompleteTaskWithMultipleTransitionsNotAllNamedByIdAndTransition() {
    deployJpdlXmlString(MULTIPLE_NOT_ALL_NAMED_TRANSITIONS_PROCESS);
    ProcessInstance processInstance = executionService.startProcessInstanceByKey(MULTIPLE_NOT_ALL_NAMED_TRANSITIONS_PROCESS_NAME);

    Task task = taskService.createTaskQuery().assignee("johndoe").uniqueResult();
    taskService.completeTask(task.getId(), "right");
    assertActivityActive(processInstance.getId(), "wait3");
  }
  
  /**
   * If a task has multiple outgoing transitions (all are named), then
   * taskService.completeTask(taskId) will result in an exception .
   */
  public void testCompleteTaskWithMultipleTransitionsAllNamedById() {
    deployJpdlXmlString(MULTIPLE_NAMED_TRANSITIONS_PROCESS);
    executionService.startProcessInstanceByKey(MULTIPLE_NAMED_TRANSITIONS_PROCESS_NAME);

    Task task = taskService.createTaskQuery().assignee("johndoe").uniqueResult();
    try {
      taskService.completeTask(task.getId());
      fail();
    } catch (JbpmException e) {
      // Exception is expected
    }
  }
  
  /**
   * If a task has multiple outgoing transitions (all are named), then
   * taskService.completeTask(taskId, null) will result in an exception 
   */
  public void testCompleteTaskWithMultipleTransitionsAllNamedByIdAndNullTransition() {
    deployJpdlXmlString(MULTIPLE_NAMED_TRANSITIONS_PROCESS);
    executionService.startProcessInstanceByKey(MULTIPLE_NAMED_TRANSITIONS_PROCESS_NAME);

    Task task = taskService.createTaskQuery().assignee("johndoe").uniqueResult();
    try {
      taskService.completeTask(task.getId());
    } catch (JbpmException e) {
      // Exception is expected
    }
    assertNotNull(taskService.createTaskQuery().assignee("johndoe") .uniqueResult());
  }
  
  /**
   * If a task has multiple outgoing transitions (all are named), then
   * taskService.completeTask(taskId, "anyvalue") will result in an exception  
   */
  public void testCompleteTaskWithMultipleTransitionsAllNamedByIdAndWrongTransition() {
    deployJpdlXmlString(MULTIPLE_NAMED_TRANSITIONS_PROCESS);
    executionService.startProcessInstanceByKey(MULTIPLE_NAMED_TRANSITIONS_PROCESS_NAME);

    Task task = taskService.createTaskQuery().assignee("johndoe").uniqueResult();
    try {
      taskService.completeTask(task.getId());
      fail();
    } catch (JbpmException e) {
      // Exception is expected
    }
    assertNotNull("After completion with an invalid outcome, the task should remain unchanged",
            taskService.createTaskQuery().assignee("johndoe") .uniqueResult());
  }
  
  /**
   * If a task has multiple outgoing transitions (all are named), then
   * taskService.completeTask(taskId, "Accept") will take the 'Accept' transition 
   */
  public void testCompleteTaskWithMultipleTransitionsAllNamedByIdAndTransition() {
    deployJpdlXmlString(MULTIPLE_NAMED_TRANSITIONS_PROCESS);
    ProcessInstance pi = executionService.startProcessInstanceByKey(MULTIPLE_NAMED_TRANSITIONS_PROCESS_NAME);

    Task task = taskService.createTaskQuery().assignee("johndoe").uniqueResult();
    taskService.completeTask(task.getId(), "left");
    assertActivityActive(pi.getId(), "wait1");
  }
  
}
