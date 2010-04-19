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
package org.jbpm.test.activities;

import org.jbpm.api.JbpmException;
import org.jbpm.api.ProcessInstance;
import org.jbpm.test.JbpmTestCase;

/**
 * @author Tom Baeyens
 */
public class StateTest extends JbpmTestCase {

  public void testWaitStatesSequence() {
    deployJpdlXmlString(
      "<process name='ThreeStates'>" +
      "  <start name='a'>" +
      "    <transition to='b' />" +
      "  </start>" +
      "  <state name='b'>" +
      "    <transition to='c' />" +
      "  </state>" +
      "  <state name='c'>" +
      "    <transition to='d' />" +
      "  </state>" +
      "  <end name='d' />" +
      "</process>"
    );
    
    ProcessInstance processInstance = executionService.startProcessInstanceById("ThreeStates-1");
    assertTrue(processInstance.isActive("b"));

    String executionId = processInstance.getId();
    processInstance = executionService.signalExecutionById(executionId);
    assertTrue(processInstance.isActive("c"));

    processInstance = executionService.signalExecutionById(executionId);
    assertTrue(processInstance.isEnded());
  }

  public void testExternalDecision() {
    deployJpdlXmlString(
      "<process name='p'>" +
      "  <start>" +
      "    <transition to='ed' />" +
      "  </start>" +
      "  <state name='ed'>" +
      "    <transition name='left'   to='b' />" +
      "    <transition name='middle' to='c' />" +
      "    <transition name='right'  to='d' />" +
      "  </state>" +
      "  <state name='b' />" +
      "  <state name='c' />" +
      "  <state name='d' />" +
      "</process>"
    );

    ProcessInstance processInstance = executionService.startProcessInstanceByKey("p", "one");
    assertTrue(processInstance.isActive("ed"));
    processInstance = executionService.signalExecutionById("p.one", "left");
    assertTrue(processInstance.isActive("b"));

    executionService.startProcessInstanceById("p-1", "two");
    processInstance = executionService.signalExecutionById("p.two", "middle");
    assertTrue(processInstance.isActive("c"));

    executionService.startProcessInstanceById("p-1", "three");
    processInstance = executionService.signalExecutionById("p.three", "right");
    assertTrue(processInstance.isActive("d"));
  }

  public void testDefaultSignalWithNamedTransitions() {
    deployJpdlXmlString(
      "<process name='p'>" +
      "  <start>" +
      "    <transition to='a' />" +
      "  </start>" +
      "  <state name='a'>" +
      "    <transition name='left'   to='b' />" +
      "    <transition name='middle' to='c' />" +
      "    <transition name='right'  to='d' />" +
      "  </state>" +
      "  <state name='b' />" +
      "  <state name='c' />" +
      "  <state name='d' />" +
      "</process>"
    );

    executionService.startProcessInstanceByKey("p", "one");
    try {
      executionService.signalExecutionById("p.one", "left");
    } catch (JbpmException e) {
      assertTextPresent("no matching transition or event for default signal in state(a)", e.getMessage());
    }
  }

  public void testNamedSignalWithoutMatchingTransition() {
    deployJpdlXmlString(
      "<process name='p'>" +
      "  <start>" +
      "    <transition to='a' />" +
      "  </start>" +
      "  <state name='a'>" +
      "    <transition name='left'   to='b' />" +
      "    <transition name='middle' to='c' />" +
      "    <transition name='right'  to='d' />" +
      "  </state>" +
      "  <state name='b' />" +
      "  <state name='c' />" +
      "  <state name='d' />" +
      "</process>"
    );

    executionService.startProcessInstanceByKey("p", "one");
    ProcessInstance processInstance = executionService.signalExecutionById("p.one", "up");
    assertTrue(processInstance.isActive("a"));
  }

  public void testDefaultSignalWithoutTransitions() {
    deployJpdlXmlString(
      "<process name='p'>" +
      "  <start>" +
      "    <transition to='a' />" +
      "  </start>" +
      "  <state name='a' />" +
      "</process>"
    );

    executionService.startProcessInstanceByKey("p", "one");
    ProcessInstance processInstance = executionService.signalExecutionById("p.one");
    assertTrue(processInstance.isActive("a"));
  }
  
  /**
   * Test for JBPM-1214
   * 
   * When a self transition comes back into the same state in the same transaction,
   * potentially there could be optimistick locking failures.
   */
  public void testSelfTransition() {
    deployJpdlXmlString(
            "<process name='selfTransition'>" +
            "  <start>" +
            "    <transition to='wait' />" +
            "  </start>" +
            "  <state name='wait' >" +
            "    <transition to='wait' />" +
            "  </state>" +
            "</process>"
          );
    ProcessInstance processInstance = executionService.startProcessInstanceByKey("selfTransition");
    assertTrue(processInstance.isActive("wait"));
    executionService.signalExecutionById(processInstance.getId());
    assertTrue(processInstance.isActive("wait"));
  }
  
  /**
   * Test for JBPM-1214
   * 
   * When a wait state is signalled and the execution comes back to the
   * wait state, there can potentially be optimistic locking exceptions.
   */
  public void testLoopBackToSignalledState() {
    deployJpdlXmlString(
            "<process name='loopBackToState'>" +
            "  <start>" +
            "    <transition to='wait' />" +
            "  </start>" +
            "  <state name='wait' >" +
            "    <transition to='go further' />" +
            "  </state>" +
            "  <custom name='go further' class='org.jbpm.test.activities.PassThroughActivity' >" +
            "    <transition to='go even further' />" +
            "  </custom>" +
            "  <custom name='go even further' class='org.jbpm.test.activities.PassThroughActivity' >" +
            "    <transition to='wait' />" +
            "  </custom>" +
            "  <end name='end' />" +
            "</process>"
          );
    
    ProcessInstance processInstance = executionService.startProcessInstanceByKey("loopBackToState");
    assertTrue(processInstance.isActive("wait"));
    executionService.signalExecutionById(processInstance.getId());
    assertTrue(processInstance.isActive("wait"));
  }
  
}
