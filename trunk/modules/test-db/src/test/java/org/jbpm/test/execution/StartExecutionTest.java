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

import org.jbpm.api.Execution;
import org.jbpm.api.JbpmException;
import org.jbpm.api.ProcessInstance;
import org.jbpm.test.JbpmTestCase;


/**
 * @author Tom Baeyens
 */
public class StartExecutionTest extends JbpmTestCase {

  public void testNonExistentProcessKey() {
    try {
      executionService.startProcessInstanceByKey("MeaningOfLife");
      fail("expected exception");
    } catch (JbpmException e) {
      assertTextPresent("no process definition with key 'MeaningOfLife'", e.getMessage());
    }
  }

  public void testNonExistentProcessId() {
    try {
      executionService.startProcessInstanceById("MeaningOfLife");
      fail("expected exception");
    } catch (JbpmException e) {
      assertTextPresent("no process definition with id 'MeaningOfLife'", e.getMessage());
    }
  }

  public void testStartNewExecutionByKey() {
    deployJpdlXmlString(
      "<process name='Insurance claim' key='ICL'>" +
      "  <start>" +
      "    <transition to='a' />" +
      "  </start>" +
      "  <state name='a' />" +
      "</process>"
    );

    ProcessInstance processInstance = executionService.startProcessInstanceByKey("ICL");

    assertNotNull(processInstance);
    assertTrue(processInstance.isActive("a"));
  }

  public void testStartNewExecutionInLatestProcessDefinition() {
    deployJpdlXmlString(
      "<process name='Insurance claim' key='ICL'>" +
      "  <start>" +
      "    <transition to='a' />" +
      "  </start>" +
      "  <state name='a' />" +
      "</process>"
    );

    ProcessInstance processInstance = executionService.startProcessInstanceByKey("ICL");

    assertNotNull(processInstance);
    assertTrue(processInstance.isActive("a"));

    deployJpdlXmlString(
      "<process name='Insurance claim' key='ICL'>" +
      "  <start>" +
      "    <transition to='b' />" +
      "  </start>" +
      "  <state name='b' />" +
      "</process>"
    );

    processInstance = executionService.startProcessInstanceByKey("ICL");

    assertNotNull(processInstance);
    assertTrue(processInstance.isActive("b"));
  }
  
  public void testStartExecutionInLatestByNameWithVariables() {
    deployJpdlXmlString(
      "<process name='Insurance claim' key='ICL'>" +
      "  <start>" +
      "    <transition to='a' />" +
      "  </start>" +
      "  <state name='a' />" +
      "</process>"
    );

    deployJpdlXmlString(
      "<process name='Insurance claim' key='ICL'>" +
      "  <start>" +
      "    <transition to='b' />" +
      "  </start>" +
      "  <state name='b' />" +
      "</process>"
    );

    // create variables that are fed into the process before it starts executing
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("a", new Integer(1));
    variables.put("b", "text");

    // feed the variables in
    ProcessInstance processInstance = executionService.startProcessInstanceByKey("ICL", variables);
    String executionId = processInstance.getId();
    assertTrue(processInstance.isActive("b"));

    // verify that the variables are actually set
    assertEquals(new Integer(1), executionService.getVariable(executionId, "a"));
    assertEquals("text", executionService.getVariable(executionId, "b"));

    // in the generated id, we can see if the right process definition version was taken
    assertTrue(processInstance.getId().startsWith("ICL."));
  }

  public void testStartNewProcessInstanceWithAKey() {
    deployJpdlXmlString(
      "<process name='Insurance claim' key='ICL'>" +
      "  <start>" +
      "    <transition to='a' />" +
      "  </start>" +
      "  <state name='a' />" +
      "</process>"
    );

    Execution processInstance = executionService.startProcessInstanceByKey("ICL", "one");

    assertNotNull(processInstance);
    assertEquals("ICL.one", processInstance.getId());
  }

  public void testStartNewProcessInstanceWithVariables() {
    deployJpdlXmlString(
      "<process name='Insurance claim' key='ICL'>" +
      "  <start>" +
      "    <transition to='a' />" +
      "  </start>" +
      "  <state name='a' />" +
      "</process>"
    );
    
    Map<String,Object> variables = new HashMap<String,Object>();
    variables.put("customer", "John Doe");
    variables.put("type", "Accident");
    variables.put("amount", new Float(763.74));

    ProcessInstance processInstance = executionService.startProcessInstanceByKey("ICL", variables);
    
    String executionId = processInstance.getId();

    assertNotNull(processInstance);
    
    Map<String,Object> expectedVariables = new HashMap<String, Object>(variables); 
    Set<String> expectedVariableNames = new HashSet<String>(expectedVariables.keySet());
    Set<String> variableNames = new HashSet<String>(executionService.getVariableNames(executionId));
    assertEquals(expectedVariableNames, variableNames);
    
    variables = executionService.getVariables(executionId, variableNames);
    assertEquals(expectedVariables, variables);
  }

  public void testStartExecutionById() {
    deployJpdlXmlString(
      "<process name='Insurance claim' key='ICL'>" +
      "  <start>" +
      "    <transition to='a' />" +
      "  </start>" +
      "  <state name='a' />" +
      "</process>"
    );
    
    // start an execution for the process with the given id
    ProcessInstance processInstance = executionService.startProcessInstanceById("ICL-1");
    assertNotNull(processInstance);

    // checking the active activity
    assertTrue(processInstance.isActive("a"));

    // checking the generated id
    assertNull(processInstance.getName());
    assertNull(processInstance.getKey());
    // if there is no user defined name or key specified in the execution,
    // the default id generator will create a unique id like this: processDefinitionId/execution.dbid
    assertTrue(processInstance.getId().startsWith("ICL."));
    // the last part of the execution key should be the dbid.
    Long.parseLong(processInstance.getId().substring(4));
  }

  public void testStartExecutionByIdWithVariables() {
    deployJpdlXmlString(
      "<process name='Insurance claim' key='ICL'>" +
      "  <start>" +
      "    <transition to='a' />" +
      "  </start>" +
      "  <state name='a' />" +
      "</process>"
    );

    // create the map with variables
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("a", new Integer(1));
    variables.put("b", "text");

    // provide the variables in the start execution method
    ProcessInstance processInstance = executionService.startProcessInstanceById("ICL-1", variables);
    String executionId = processInstance.getId();

    assertEquals(new Integer(1), executionService.getVariable(executionId, "a"));
    assertEquals("text", executionService.getVariable(executionId, "b"));
  }
}
