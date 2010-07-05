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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.jbpm.api.activity.ActivityExecution;
import org.jbpm.api.activity.ExternalActivityBehaviour;
import org.jbpm.pvm.internal.builder.ProcessDefinitionBuilder;
import org.jbpm.pvm.internal.client.ClientExecution;
import org.jbpm.pvm.internal.client.ClientProcessDefinition;
import org.jbpm.pvm.internal.client.ClientProcessInstance;
import org.jbpm.test.BaseJbpmTestCase;

/**
 * @author Tom Baeyens
 */
public class VariableTest extends BaseJbpmTestCase {
  
  public static class WaitState implements ExternalActivityBehaviour {
    private static final long serialVersionUID = 1L;
    public void execute(ActivityExecution execution) {
      execution.waitForSignal();
    }
    public void signal(ActivityExecution execution, String signalName, Map<String, ?> parameters){
    }
  }

  protected ClientProcessDefinition createProcessDefinition() {
    return ProcessDefinitionBuilder
    .startProcess()
      .startActivity("a", new WaitState())
        .initial()
      .endActivity()
    .endProcess();
  }


  public void testSetAndGetVariable() {
    ClientProcessDefinition processDefinition = createProcessDefinition();
    
    ClientExecution processInstance = processDefinition.startProcessInstance();
    
    processInstance.setVariable("customer", "coca-cola");
    assertEquals("coca-cola", processInstance.getVariable("customer"));
    
    processInstance.setVariable("address", "usa");
    assertEquals("usa", processInstance.getVariable("address"));

    processInstance.setVariable("size", "big");
    assertEquals("big", processInstance.getVariable("size"));
  }

  public void testHasVariable() {
    ClientProcessDefinition processDefinition = createProcessDefinition();
    
    ClientExecution processInstance = processDefinition.startProcessInstance();
    
    processInstance.setVariable("customer", "coca-cola");

    assertTrue(processInstance.hasVariable("customer"));
    assertFalse(processInstance.hasVariable("address"));
  }

  public void testSetVariables() {
    ClientProcessDefinition processDefinition = createProcessDefinition();
    
    ClientExecution processInstance = processDefinition.startProcessInstance();

    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("customer", "coca-cola");
    variables.put("address", "usa");
    variables.put("size", "big");

    processInstance.setVariables(variables);

    assertEquals("coca-cola", processInstance.getVariable("customer"));
    assertEquals("usa", processInstance.getVariable("address"));
    assertEquals("big", processInstance.getVariable("size"));
  }

  public void testGetVariables() {
    ClientProcessDefinition processDefinition = createProcessDefinition();
    
    ClientExecution processInstance = processDefinition.startProcessInstance();
    
    processInstance.setVariable("customer", "coca-cola");
    processInstance.setVariable("address", "usa");
    processInstance.setVariable("size", "big");
    
    Map<String, Object> expectedVariables = new HashMap<String, Object>();
    expectedVariables.put("customer", "coca-cola");
    expectedVariables.put("address", "usa");
    expectedVariables.put("size", "big");
    assertEquals(expectedVariables, processInstance.getVariables());
  }

  public void testRemoveVariable() {
    ClientProcessDefinition processDefinition = createProcessDefinition();
    
    ClientExecution processInstance = processDefinition.startProcessInstance();
    
    processInstance.setVariable("customer", "coca-cola");
    processInstance.setVariable("address", "usa");
    processInstance.setVariable("size", "big");
    
    Map<String, Object> expectedVariables = new HashMap<String, Object>();
    expectedVariables.put("customer", "coca-cola");
    expectedVariables.put("address", "usa");
    expectedVariables.put("size", "big");
    
    assertEquals(expectedVariables, processInstance.getVariables());
    
    processInstance.removeVariable("address");
    expectedVariables.remove("address");

    assertEquals(expectedVariables, processInstance.getVariables());
    
    processInstance.removeVariable("customer");
    expectedVariables.remove("customer");

    assertEquals(expectedVariables, processInstance.getVariables());
    
    processInstance.removeVariable("size");
    expectedVariables.remove("size");

    assertEquals(expectedVariables, processInstance.getVariables());
  }

  public void testRemoveVariables() {
    ClientProcessDefinition processDefinition = createProcessDefinition();
    
    ClientExecution processInstance = processDefinition.startProcessInstance();
    
    processInstance.setVariable("customer", "coca-cola");
    processInstance.setVariable("address", "usa");
    processInstance.setVariable("size", "big");
    
    processInstance.removeVariables();
    
    Map<String, Object> expectedVariables = new HashMap<String, Object>();
    assertEquals(expectedVariables, processInstance.getVariables());
  }

  public void testGetVariableKeys() {
    ClientProcessDefinition processDefinition = createProcessDefinition();
    
    ClientExecution processInstance = processDefinition.startProcessInstance();
    
    processInstance.setVariable("customer", "coca-cola");
    processInstance.setVariable("address", "usa");
    processInstance.setVariable("size", "big");
    
    Set<String> expectedVariableKeys = new HashSet<String>();
    expectedVariableKeys.add("customer");
    expectedVariableKeys.add("address");
    expectedVariableKeys.add("size");
    assertEquals(expectedVariableKeys, new HashSet<String>(processInstance.getVariableKeys()));
  }

  public void testGetUnexistingVariable() {
    ClientProcessDefinition processDefinition = createProcessDefinition();
    
    ClientExecution processInstance = processDefinition.startProcessInstance();
    
    assertNull(processInstance.getVariable("answer to life, the universe and everything"));
  }

  public static class VariableActivity implements ExternalActivityBehaviour {
    private static final long serialVersionUID = 1L;
    public void execute(ActivityExecution execution) {
      assertEquals("coca-cola", execution.getVariable("customer"));
      execution.setVariable("message", "Killroy was here");
      execution.waitForSignal();
    }
    public void signal(ActivityExecution execution, String signalName, Map<String, ?> parameters) {
    }
  }

  public void testInitialiseVariablesBeforeProcessInstanceBegin() {
    ClientProcessDefinition processDefinition = ProcessDefinitionBuilder
    .startProcess()
      .startActivity("a", new VariableActivity())
        .initial()
      .endActivity()
    .endProcess();
    
    // here, the process instance is created first, and only later it is begun
    ClientProcessInstance processInstance = processDefinition.createProcessInstance();
    processInstance.setVariable("customer", "coca-cola");
    processInstance.start();
    assertEquals("Killroy was here", processInstance.getVariable("message"));
  }
  
  public void testNullValue() {
    ClientProcessDefinition processDefinition = createProcessDefinition();
    
    ClientExecution processInstance = processDefinition.startProcessInstance();
    
    processInstance.setVariable("niks", null);
    processInstance.setVariable("nada", null);

    assertTrue(processInstance.hasVariables());
    assertTrue(processInstance.hasVariable("niks"));
    assertTrue(processInstance.hasVariable("nada"));

    assertNull(processInstance.getVariable("niks"));
    assertNull(processInstance.getVariable("nada"));
    
    Set<String> expectedKeys = new HashSet<String>();
    expectedKeys.add("niks");
    expectedKeys.add("nada");
    assertEquals(expectedKeys, new HashSet<String>(processInstance.getVariableKeys()));
    
    Map<String, Object> expectedVariables = new HashMap<String, Object>();
    expectedVariables.put("niks", null);
    expectedVariables.put("nada", null);
    assertEquals(expectedVariables, new HashMap<String, Object>(processInstance.getVariables()));
  }

}
