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
package org.jbpm.test.activity.group;

import java.util.HashSet;
import java.util.Set;

import org.jbpm.api.Execution;
import org.jbpm.api.ProcessInstance;
import org.jbpm.test.JbpmTestCase;


/**
 * @author Tom Baeyens
 */
public class GroupConcurrencyCombinationTest extends JbpmTestCase {

  
  public void testGroupConcurrencyWithStartEndSignalFirstGroup() {
    deployJpdlXmlString(
      "<process name='GroupConcurrency'>" +
      "  <start>" +
      "    <transition to='fork'/>" +
      "  </start>" +
      "  <fork name='fork'>" +
      "    <transition to='group'/>" +
      "    <transition to='concurrent-wait'/>" +
      "  </fork>" +
      "  <group name='group'>" +
      "    <start name='group-start'>" +
      "      <transition to='group-wait'/>" +
      "    </start>" +
      "    <state name='group-wait'>" +
      "      <transition to='group-end'/>" +
      "    </state>" +
      "    <end name='group-end'/>" +
      "    <transition to='join'/>" +
      "  </group>" +
      "  <state name='concurrent-wait'>" +
      "    <transition to='join'/>" +
      "  </state>" +
      "  <join name='join'>" +
      "     <transition to='end'/>" +
      "  </join>" +
      "  <state name='end'/>" +
      "</process>"
    );

    ProcessInstance processInstance = executionService.startProcessInstanceByKey("GroupConcurrency");
    
    Set<String> expectedActivityNames = new HashSet<String>();
    expectedActivityNames.add("group-wait");
    expectedActivityNames.add("concurrent-wait");
    assertEquals(expectedActivityNames, processInstance.findActiveActivityNames());
    
    Execution executionInGroupWait = processInstance.findActiveExecutionIn("group-wait");
    assertNotNull(executionInGroupWait);

    Execution executionInConcurrentWait = processInstance.findActiveExecutionIn("concurrent-wait");
    assertNotNull(executionInConcurrentWait);
    
    processInstance = executionService.signalExecutionById(executionInGroupWait.getId());
    
    expectedActivityNames = new HashSet<String>();
    expectedActivityNames.add("concurrent-wait");
    assertEquals(expectedActivityNames, processInstance.findActiveActivityNames());

    processInstance = executionService.signalExecutionById(executionInConcurrentWait.getId());
    
    expectedActivityNames = new HashSet<String>();
    expectedActivityNames.add("end");
    assertEquals(expectedActivityNames, processInstance.findActiveActivityNames());
  }

  public void testGroupConcurrencyWithStartEndSignalFirstConcurrent() {
    deployJpdlXmlString(
      "<process name='GroupConcurrency'>" +
      "  <start>" +
      "    <transition to='fork'/>" +
      "  </start>" +
      "  <fork name='fork'>" +
      "    <transition to='group'/>" +
      "    <transition to='concurrent-wait'/>" +
      "  </fork>" +
      "  <group name='group'>" +
      "    <start name='group-start'>" +
      "      <transition to='group-wait'/>" +
      "    </start>" +
      "    <state name='group-wait'>" +
      "      <transition to='group-end'/>" +
      "    </state>" +
      "    <end name='group-end'/>" +
      "    <transition to='join'/>" +
      "  </group>" +
      "  <state name='concurrent-wait'>" +
      "    <transition to='join'/>" +
      "  </state>" +
      "  <join name='join'>" +
      "     <transition to='end'/>" +
      "  </join>" +
      "  <state name='end'/>" +
      "</process>"
    );

    ProcessInstance processInstance = executionService.startProcessInstanceByKey("GroupConcurrency");
    
    Set<String> expectedActivityNames = new HashSet<String>();
    expectedActivityNames.add("group-wait");
    expectedActivityNames.add("concurrent-wait");
    assertEquals(expectedActivityNames, processInstance.findActiveActivityNames());
    
    Execution executionInGroupWait = processInstance.findActiveExecutionIn("group-wait");
    assertNotNull(executionInGroupWait);

    Execution executionInConcurrentWait = processInstance.findActiveExecutionIn("concurrent-wait");
    assertNotNull(executionInConcurrentWait);
    
    processInstance = executionService.signalExecutionById(executionInConcurrentWait.getId());
    
    expectedActivityNames = new HashSet<String>();
    expectedActivityNames.add("group-wait");
    assertEquals(expectedActivityNames, processInstance.findActiveActivityNames());

    processInstance = executionService.signalExecutionById(executionInGroupWait.getId());
    
    expectedActivityNames = new HashSet<String>();
    expectedActivityNames.add("end");
    assertEquals(expectedActivityNames, processInstance.findActiveActivityNames());
  }

  public void testGroupConcurrencyDirectTransitionsSignalFirstGroup() {
    deployJpdlXmlString(
      "<process name='GroupConcurrency'>" +
      "  <start>" +
      "    <transition to='fork'/>" +
      "  </start>" +
      "  <fork name='fork'>" +
      "    <transition to='group-wait'/>" +
      "    <transition to='concurrent-wait'/>" +
      "  </fork>" +
      "  <group name='group'>" +
      "    <state name='group-wait'>" +
      "      <transition to='join'/>" +
      "    </state>" +
      "  </group>" +
      "  <state name='concurrent-wait'>" +
      "    <transition to='join'/>" +
      "  </state>" +
      "  <join name='join'>" +
      "     <transition to='end'/>" +
      "  </join>" +
      "  <state name='end'/>" +
      "</process>"
    );

    ProcessInstance processInstance = executionService.startProcessInstanceByKey("GroupConcurrency");
    
    Set<String> expectedActivityNames = new HashSet<String>();
    expectedActivityNames.add("group-wait");
    expectedActivityNames.add("concurrent-wait");
    assertEquals(expectedActivityNames, processInstance.findActiveActivityNames());
    
    Execution executionInGroupWait = processInstance.findActiveExecutionIn("group-wait");
    assertNotNull(executionInGroupWait);

    Execution executionInConcurrentWait = processInstance.findActiveExecutionIn("concurrent-wait");
    assertNotNull(executionInConcurrentWait);
    
    processInstance = executionService.signalExecutionById(executionInGroupWait.getId());
    
    expectedActivityNames = new HashSet<String>();
    expectedActivityNames.add("concurrent-wait");
    assertEquals(expectedActivityNames, processInstance.findActiveActivityNames());

    processInstance = executionService.signalExecutionById(executionInConcurrentWait.getId());
    
    expectedActivityNames = new HashSet<String>();
    expectedActivityNames.add("end");
    assertEquals(expectedActivityNames, processInstance.findActiveActivityNames());
  }

  public void testGroupConcurrencyDirectTransitionsSignalFirstConcurrent() {
    deployJpdlXmlString(
      "<process name='GroupConcurrency'>" +
      "  <start>" +
      "    <transition to='fork'/>" +
      "  </start>" +
      "  <fork name='fork'>" +
      "    <transition to='group-wait'/>" +
      "    <transition to='concurrent-wait'/>" +
      "  </fork>" +
      "  <group name='group'>" +
      "    <state name='group-wait'>" +
      "      <transition to='join'/>" +
      "    </state>" +
      "  </group>" +
      "  <state name='concurrent-wait'>" +
      "    <transition to='join'/>" +
      "  </state>" +
      "  <join name='join'>" +
      "     <transition to='end'/>" +
      "  </join>" +
      "  <state name='end'/>" +
      "</process>"
    );

    ProcessInstance processInstance = executionService.startProcessInstanceByKey("GroupConcurrency");
    
    Set<String> expectedActivityNames = new HashSet<String>();
    expectedActivityNames.add("group-wait");
    expectedActivityNames.add("concurrent-wait");
    assertEquals(expectedActivityNames, processInstance.findActiveActivityNames());
    
    Execution executionInGroupWait = processInstance.findActiveExecutionIn("group-wait");
    assertNotNull(executionInGroupWait);

    Execution executionInConcurrentWait = processInstance.findActiveExecutionIn("concurrent-wait");
    assertNotNull(executionInConcurrentWait);
    
    processInstance = executionService.signalExecutionById(executionInConcurrentWait.getId());
    
    expectedActivityNames = new HashSet<String>();
    expectedActivityNames.add("group-wait");
    assertEquals(expectedActivityNames, processInstance.findActiveActivityNames());

    processInstance = executionService.signalExecutionById(executionInGroupWait.getId());
    
    expectedActivityNames = new HashSet<String>();
    expectedActivityNames.add("end");
    assertEquals(expectedActivityNames, processInstance.findActiveActivityNames());
  }
}
