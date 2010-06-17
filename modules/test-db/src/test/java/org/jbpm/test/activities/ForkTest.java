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
package org.jbpm.test.activities;

import org.jbpm.api.task.Task;
import org.jbpm.test.JbpmTestCase;

/**
 * @author Joram Barrez
 */
public class ForkTest extends JbpmTestCase {
  
  // Test for JBPM-2297
  public void testBasicForkUsage() {
    deployJpdlXmlString(
      "<process name='sboxCreation'>" +
      "<start name='start'> " +
      "   <transition to='fork (review)'/> " +
      "</start> " +
      "<fork name='fork (review)'> " +
      "   <transition name='Reporting review' to='Review (risk)'/> " +
      "   <transition name='Risk review' to='Review (reporting)'/> " +
      "</fork> " +
      "<task candidate-groups='risk-dept' name='Review (risk)'> " +
      "   <transition name='Approve'  to='join (approved)'/> " +
      "   <transition name='Reject'   to='Rejected by Risk'/> " +
      "</task> " +
      "<task candidate-groups='reporting-dept' name='Review (reporting)'> " +
      "   <transition name='Approve' to='join (approved)'/> " +
      "   <transition name='Reject' to='Rejected by Reporting'/> " +
      "</task> " +
      "<join name='join (approved)'> " +
      "   <transition to='end1'/> " +
      "</join> " +
      "<end-cancel name='Rejected by Reporting'/> " +
      "<end name='end1'/> " +
      "<end-cancel name='Rejected by Risk'/> " +
      "</process>");
    
    String processInstanceId = executionService.startProcessInstanceByKey("sboxCreation").getId();
    Task riskTask = taskService.createTaskQuery().activityName("Review (risk)").uniqueResult();
    Task reportTask = taskService.createTaskQuery().activityName("Review (reporting)").uniqueResult();
    
    taskService.completeTask(riskTask.getId(), "Approve");
    taskService.completeTask(reportTask.getId(), "Reject");
    
    assertProcessInstanceEnded(processInstanceId);
  }
  
  
  private static final String UNSTRUCTURED_CONCURRENT_PROCESS =
    "<process name='unstructuredConcurrency'>" +
    "<start name='start'> " +
    "   <transition to='theFork'/> " +
    "</start> " +
    "<fork name='theFork'> " +
    "   <transition name='pathA' to='A'/> " +
    "   <transition name='pathB' to='B'/> " +
    "</fork> " +
    "<task name='A'> " +
    "   <transition to='theJoin'/> " +
    "</task> " +
    "<task name='B'> " +
    "   <transition to='end2'/> " +
    "</task> " +
    "<join name='theJoin'> " +
    "   <transition to='waitState'/> " +
    "</join> " +
    "<state name='waitState'>" +
    "  <transition to='end1' />" +
    "</state>" +
    "<end name='end1'/> " +
    "<end name='end2'/> " +
    "</process>";
  
  // Test for JBPM-2040
  public void testDefaultUnstructuredForkBehaviour() {
    deployJpdlXmlString(UNSTRUCTURED_CONCURRENT_PROCESS);
    String processInstanceId = executionService.startProcessInstanceByKey("unstructuredConcurrency").getId();
    
    Task taskA = taskService.createTaskQuery().activityName("A").uniqueResult();
    taskService.completeTask(taskA.getId());
    assertActivitiesActive(processInstanceId, "waitState", "B");
    
    Task taskB = taskService.createTaskQuery().activityName("B").uniqueResult();
    taskService.completeTask(taskB.getId());
    assertProcessInstanceEnded(processInstanceId);
  }
  
  //Test for JBPM-2040
  public void testDefaultUnstructuredForkBehaviour2() {
    deployJpdlXmlString(UNSTRUCTURED_CONCURRENT_PROCESS);
    String processInstanceId = executionService.startProcessInstanceByKey("unstructuredConcurrency").getId();
    
    Task taskB = taskService.createTaskQuery().activityName("B").uniqueResult();
    taskService.completeTask(taskB.getId());
    assertProcessInstanceEnded(processInstanceId);
    
    assertTrue("There are still open tasks left",
      taskService.createTaskQuery().processInstanceId(processInstanceId).list().isEmpty());
  }
  
  //Test for JBPM-2040
  public void testUnstructuredForkBehaviourWhenEndingExecutionOnly() {
    // We're now changing the default end behaviour to ending
    // only the execution instead of the process instance
    deployJpdlXmlString(addEndExecutionToConcurrentProcess());
    String processInstanceId = executionService.startProcessInstanceByKey("unstructuredConcurrency").getId();
    
    Task taskB = taskService.createTaskQuery().activityName("B").uniqueResult();
    taskService.completeTask(taskB.getId());
    
    assertProcessInstanceActive(processInstanceId);
    assertActivityActive(processInstanceId, "A");
    
    Task taskA = taskService.createTaskQuery().activityName("A").uniqueResult();
    taskService.completeTask(taskA.getId());
    assertActivityActive(processInstanceId, "waitState");
    assertProcessInstanceActive(processInstanceId);
    
    executionService.signalExecutionById(
            executionService.findExecutionById(processInstanceId)
                            .findActiveExecutionIn("waitState").getId());
    assertProcessInstanceEnded(processInstanceId);
  }
  
  //Test for JBPM-2040
  public void testUnstructuredForkBehaviourWhenEndingExecutionOnly2() {
    // We're now changing the default end behaviour to ending
    // only the execution instead of the process instance
    deployJpdlXmlString(addEndExecutionToConcurrentProcess());
    String processInstanceId = executionService.startProcessInstanceByKey("unstructuredConcurrency").getId();
    
    Task taskA = taskService.createTaskQuery().activityName("A").uniqueResult();
    taskService.completeTask(taskA.getId());
    assertActivitiesActive(processInstanceId, "B", "waitState");
    
    Task taskB = taskService.createTaskQuery().activityName("B").uniqueResult();
    taskService.completeTask(taskB.getId());
    assertProcessInstanceActive(processInstanceId); 
  }
  
  private String addEndExecutionToConcurrentProcess() {
    return  UNSTRUCTURED_CONCURRENT_PROCESS.replace("<end name='end1'/>", "<end name='end1' ends='execution'/>")
                                           .replace("<end name='end2'/>", "<end name='end2' ends='execution'/>");
  }

}
