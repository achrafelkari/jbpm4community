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
package org.jbpm.test.activity.subprocess;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.internal.compiler.ast.AssertStatement;
import org.jbpm.api.JbpmException;
import org.jbpm.api.ProcessInstance;
import org.jbpm.api.task.Task;
import org.jbpm.test.JbpmTestCase;


/**
 * Test case for different usages of the subprocess activity.
 * 
 * @author Joram Barrez
 */
public class SubProcessTest extends JbpmTestCase {
  
  private static final String MAIN_PROCESS =
    "<process name='mainProcess'>" +
    "  <start>" +
    "    <transition to='review' />" +
    "  </start>" +
    "  <sub-process name='review' sub-process-key='SubProcessReview'>" +
    "    <transition name='ok' to='next step'/>" +
    "    <transition name='nok' to='update'/>" +
    "    <transition name='reject' to='close'/>" +
    "  </sub-process>" +
    "  <state name='next step'/>" +
    "  <state name='update'/>" +
    "  <end name='close'/>" +
    "</process>";  
    
  private static final String SUB_PROCESS =
    "<process name='SubProcessReview'>" +
    "  <start>" +
    "    <transition to='get approval'/>" +
    "  </start>" +
    "  <task name='get approval' assignee='johndoe'>" +
    "    <transition name='ok' to='ok'/>" +
    "    <transition name='nok' to='nok'/>" +
    "    <transition name='reject' to='reject'/>" +
    "  </task>" +
    "  <end name='ok' />" +
    "  <end name='nok' />" +
    "  <end name='reject' />" +
    "</process>";  
  
  private static final String MAIN_PROCESS_NO_WAIT_STATE =
    "<process name='mainProcess'>" +
    "  <start>" +
    "    <transition to='review' />" +
    "  </start>" +
    "  <sub-process name='review' sub-process-key='SubProcessReview'>" +
    "    <transition to='theMainEnd'/>" +
    "  </sub-process>" +
    "  <end name='theMainEnd'/>" +
    "</process>";  
  
  private static final String SUB_PROCESS_NO_WAIT_STATE =
    "<process name='SubProcessReview'>" +
    "  <start>" +
    "    <transition to='theEnd'/>" +
    "  </start>" +
    "  <end name='theEnd' />" +
    "</process>";  
  
  private static final String MAIN_PROCESS_SUB_EL =
    "<process name='mainProcess'>" +
    "  <start>" +
    "    <transition to='review' />" +
    "  </start>" +
    "  <sub-process name='review' sub-process-key='#{dynamic_subprocess}'>" +
    "    <transition name='ok' to='next step'/>" +
    "    <transition name='nok' to='update'/>" +
    "    <transition name='reject' to='close'/>" +
    "  </sub-process>" +
    "  <state name='next step'/>" +
    "  <state name='update'/>" +
    "  <end name='close'/>" +
    "</process>"; 
  
  public void testSubProcessOutcomeToState() {
    deployJpdlXmlString(SUB_PROCESS);
    deployJpdlXmlString(MAIN_PROCESS);
    
    ProcessInstance processInstance = executionService.startProcessInstanceByKey("mainProcess");
    Task task = taskService.findPersonalTasks("johndoe").get(0);
    taskService.completeTask(task.getId(), "nok");
    assertActivityActive(processInstance.getId(), "update");
  }

  
  public void testDynamicSubProcess() {
    deployJpdlXmlString(SUB_PROCESS);
    deployJpdlXmlString(MAIN_PROCESS_SUB_EL);
    
    Map<String, String> vars = new HashMap<String, String>();
    vars.put("dynamic_subprocess", "SubProcessReview");
    ProcessInstance processInstance = executionService.startProcessInstanceByKey("mainProcess",vars);
    Task task = taskService.findPersonalTasks("johndoe").get(0);
    taskService.completeTask(task.getId(), "reject");
    assertProcessInstanceEnded(processInstance); 
  }
  
  public void testDynamicSubProcessNotFound() {
    String expectedError = "Subprocess 'DOES_NOT_EXIST' could not be found.";
    deployJpdlXmlString(SUB_PROCESS);
    deployJpdlXmlString(MAIN_PROCESS_SUB_EL);
    
    Map<String, String> vars = new HashMap<String, String>();
    vars.put("dynamic_subprocess", "DOES_NOT_EXIST");
    try {
      ProcessInstance processInstance = executionService.startProcessInstanceByKey("mainProcess",vars);
      fail("Should not happen, error expected: " + expectedError);
    } catch (JbpmException je) {
      assertEquals(expectedError, je.getMessage());
    }
  }
  
  public void testDynamicSubProcessWrongProperty() {
    String expectedError = "Subprocess key '#{dynamic_subprocess}' could not be resolved.";
    deployJpdlXmlString(SUB_PROCESS);
    deployJpdlXmlString(MAIN_PROCESS_SUB_EL);
    
    Map<String, String> vars = new HashMap<String, String>();
    vars.put("WRONG_PROPERTY", "VALUE_DOES_NOT_MATTER");
    try {
      ProcessInstance processInstance = executionService.startProcessInstanceByKey("mainProcess",vars);
      fail("Should not happen, error expected: " + expectedError);
    } catch (JbpmException je) {
      assertEquals(expectedError, je.getMessage());
    }
  }
  
  public void testSubProcessOutcomeToEnd() {
    deployJpdlXmlString(SUB_PROCESS);
    deployJpdlXmlString(MAIN_PROCESS);
    
    ProcessInstance processInstance = executionService.startProcessInstanceByKey("mainProcess");
    Task task = taskService.findPersonalTasks("johndoe").get(0);
    taskService.completeTask(task.getId(), "reject");
    assertProcessInstanceEnded(processInstance); 
  }

  
  // Test for JBPM-2651
  public void testSubProcessNoWaitStates() {
    deployJpdlXmlString(SUB_PROCESS_NO_WAIT_STATE);
    deployJpdlXmlString(MAIN_PROCESS_NO_WAIT_STATE);
    
    executionService.startProcessInstanceByKey("mainProcess");
  }
  
}
