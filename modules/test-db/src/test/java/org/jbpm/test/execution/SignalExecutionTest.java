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
package org.jbpm.test.execution;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.jbpm.api.ProcessInstance;
import org.jbpm.test.JbpmTestCase;


/**
 * @author Tom Baeyens
 */
public class SignalExecutionTest extends JbpmTestCase {
  
  public void testSignalExecutionById() {
    deployJpdlXmlString(
      "<process name='Insurance claim' key='ICL'>" +
      "  <start>" +
      "    <transition to='a' />" +
      "  </start>" +
      "  <state name='a'>" +
      "    <transition to='b' />" +
      "  </state>" +
      "  <state name='b'>" +
      "    <transition to='c' />" +
      "  </state>" +
      "  <state name='c' />" +
      "</process>"
    );

    ProcessInstance processInstance = executionService.startProcessInstanceByKey("ICL", "82436");
    
    assertTrue(processInstance.isActive("a"));
    
    processInstance = executionService.signalExecutionById("ICL.82436");

    assertTrue(processInstance.isActive("b"));

    processInstance = executionService.signalExecutionById("ICL.82436");

    assertTrue(processInstance.isActive("c"));
  }

  public void testSignalExecutionWithVariables() {
    deployJpdlXmlString(
      "<process name='Insurance claim' key='ICL'>" +
      "  <start>" +
      "    <transition to='a' />" +
      "  </start>" +
      "  <state name='a'>" +
      "    <transition to='b' />" +
      "  </state>" +
      "  <state name='b' />" +
      "</process>"
    );

    ProcessInstance processInstance = executionService.startProcessInstanceByKey("ICL", "82436");

    Map<String,Object> variables = new HashMap<String,Object>();
    variables.put("customer", "John Doe");
    variables.put("type", "Accident");
    variables.put("amount", new Float(763.74));

    processInstance = executionService.signalExecutionById("ICL.82436", variables);

    assertNotNull(processInstance);
    String pid = processInstance.getId();
    assertTrue(processInstance.isActive("b"));
    
    Map<String,Object> expectedVariables = new HashMap<String, Object>(variables); 
    Set<String> expectedVariableNames = new HashSet<String>(expectedVariables.keySet());
    Set<String> variableNames = new HashSet<String>(executionService.getVariableNames(pid));
    assertEquals(expectedVariableNames, variableNames);
    
    variables = executionService.getVariables(pid, variableNames);
    assertEquals(expectedVariables, variables);
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

    ProcessInstance processInstance = executionService.startProcessInstanceByKey("p");
    processInstance = executionService.signalExecutionById(processInstance.getId());
    assertTrue(processInstance.isActive("a"));
  }
}
