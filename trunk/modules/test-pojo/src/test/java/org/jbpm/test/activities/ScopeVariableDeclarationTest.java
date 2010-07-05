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
import java.util.Map;

import org.jbpm.api.activity.ActivityExecution;
import org.jbpm.api.activity.ExternalActivityBehaviour;
import org.jbpm.api.model.Activity;
import org.jbpm.api.model.OpenExecution;
import org.jbpm.pvm.internal.builder.ProcessDefinitionBuilder;
import org.jbpm.pvm.internal.client.ClientExecution;
import org.jbpm.pvm.internal.client.ClientProcessDefinition;
import org.jbpm.pvm.internal.model.ExecutionImpl;
import org.jbpm.pvm.internal.wire.descriptor.StringDescriptor;
import org.jbpm.test.BaseJbpmTestCase;


/**
 * @author Tom Baeyens
 */
public class ScopeVariableDeclarationTest extends BaseJbpmTestCase {

  public static class WaitState implements ExternalActivityBehaviour {
    private static final long serialVersionUID = 1L;
    public void execute(ActivityExecution execution) {
      execution.waitForSignal();
    }
    public void signal(ActivityExecution execution, String signalName, Map<String, ?> parameters) {
      execution.take(signalName);
    }
  }
  
  public static class Composite implements ExternalActivityBehaviour {
    private static final long serialVersionUID = 1L;
    public void execute(ActivityExecution execution) {
      execute((ExecutionImpl)execution);
    }
    public void execute(ExecutionImpl execution) {
      Activity child = execution.getActivity().getActivities().get(0);
      execution.execute(child);
    }
    public void signal(ActivityExecution execution, String signalName, Map<String, ?> parameters) {
    }
  }
  
  public void testProcessInstanceVariableDeclaration() {
    ClientProcessDefinition processDefinition = ProcessDefinitionBuilder
    .startProcess()
      .variable("flight")
      .startActivity("a", new WaitState())
        .initial()
      .endActivity()
    .endProcess();
    
    ClientExecution processInstance = processDefinition.startProcessInstance();

    assertTrue(processInstance.hasVariable("flight"));
    assertNull(processInstance.getVariable("flight"));
  }

  public void testProcessInstanceVariableDeclarationWithInitialValue() {
    ClientProcessDefinition processDefinition = ProcessDefinitionBuilder.startProcess()
      .startVariable("flight")
        .initialValue(new StringDescriptor("B52"))
      .endVariable()
      .startActivity("a", new WaitState())
        .initial()
      .endActivity()
    .endProcess();
    
    ClientExecution processInstance = processDefinition.startProcessInstance();

    assertTrue(processInstance.hasVariable("flight"));
    assertEquals("B52", processInstance.getVariable("flight"));
  }

  public void testNestedScopeDeclarations() {
    ClientProcessDefinition processDefinition = ProcessDefinitionBuilder.startProcess()
      .startVariable("flight")
        .initialValue(new StringDescriptor("B52"))
      .endVariable()
      .startActivity("outer", new Composite())
        .startVariable("duration")
          .initialValue(new StringDescriptor("22 minutes"))
        .endVariable()
        .startActivity("middle", new Composite())
          .startVariable("altitude")
            .initialValue(new StringDescriptor("31000 ft"))
          .endVariable()
          .startActivity("inner", new Composite())
            .startVariable("passengers")
              .initialValue(new StringDescriptor("52"))
            .endVariable()
            .startActivity("start", new WaitState())
              .initial()
              .startVariable("fuel")
                .initialValue(new StringDescriptor("kerosine"))
              .endVariable()
            .endActivity()
          .endActivity()
        .endActivity()
      .endActivity()
    .endProcess();
    
    Map<String, Object> expectedVariables = new HashMap<String, Object>();
    
    ClientExecution processInstance = processDefinition.startProcessInstance();
    
    expectedVariables.put("flight", "B52");
    assertEquals(expectedVariables, processInstance.getVariables());
    
    OpenExecution outerExecution = processInstance.getExecution("outer");
    expectedVariables.put("duration", "22 minutes");
    assertEquals(expectedVariables, outerExecution.getVariables());

    OpenExecution middleExecution = outerExecution.getExecution("middle");
    expectedVariables.put("altitude", "31000 ft");
    assertEquals(expectedVariables, middleExecution.getVariables());

    OpenExecution innerExecution = middleExecution.getExecution("inner");
    expectedVariables.put("passengers", "52");
    assertEquals(expectedVariables, innerExecution.getVariables());

    OpenExecution startExecution = innerExecution.getExecution("start");
    expectedVariables.put("fuel", "kerosine");
    assertEquals(expectedVariables, startExecution.getVariables());
  }

  public void testHiddenVariable() {
    ClientProcessDefinition processDefinition = ProcessDefinitionBuilder
    .startProcess()
      .startVariable("flight")
        .initialValue(new StringDescriptor("B52"))
      .endVariable()
      .startActivity("c", new Composite())
        .startVariable("flight")
          .initialValue(new StringDescriptor("U2"))
        .endVariable()
        .startActivity("i", new WaitState())
          .initial()
          .startVariable("flight")
            .initialValue(new StringDescriptor("C130"))
          .endVariable()
        .endActivity()
      .endActivity()
    .endProcess();
    
    ClientExecution processInstance = processDefinition.startProcessInstance();
    OpenExecution executionC = processInstance.getExecution("c");
    OpenExecution executionI = executionC.getExecution("i");
    
    assertEquals("B52", processInstance.getVariable("flight"));
    assertEquals("U2", executionC.getVariable("flight"));
    assertEquals("C130", executionI.getVariable("flight"));
  }

  public void testAutomaticScopeManagement() {
    /*
    process 
     ${flight} = 'B52'
    +--------------------------------------------------------------------+
    | outer                                                              |
    |  ${duration} = '22 minutes'                                        |
    | +-------------------------------+                                  |  
    | | left-middle                   |                                  |
    | |  ${altitude} = '31000 ft'     |                                  |
    | | +---------------------------+ |   +----------------------------+ |
    | | | left-inner                | |   | right-middle               | |       
    | | |  ${passengers}            | |   |  ${customer} = 'coca cola' | |       
    | | | +-----------------------+ | |   |  +--------------------+    | |
    | | | | left-start            | -------> | right-inner        |    | |
    | | | |  ${fuel} = 'kerosine' | | |   |  |  ${date} = 'today' |    | |
    | | | +-----------------------+ | |   |  +--------------------+    | |
    | | +---------------------------+ |   +----------------------------+ |
    +--------------------------------------------------------------------+
    */
    ClientProcessDefinition processDefinition = ProcessDefinitionBuilder.startProcess()
      .startVariable("flight")
        .initialValue(new StringDescriptor("B52"))
      .endVariable()
      .startActivity("outer", new Composite())
        .startVariable("duration")
          .initialValue(new StringDescriptor("22 minutes"))
        .endVariable()
        .startActivity("left-middle", new Composite())
          .startVariable("altitude")
            .initialValue(new StringDescriptor("31000 ft"))
          .endVariable()
          .startActivity("left-inner", new Composite())
            .startVariable("passengers")
              .initialValue(new StringDescriptor("52"))
            .endVariable()
            .startActivity("left-start", new WaitState())
              .initial()
              .transition("right-inner")
              .startVariable("fuel")
                .initialValue(new StringDescriptor("kerosine"))
              .endVariable()
            .endActivity()
          .endActivity()
        .endActivity()
        .startActivity("right-middle", new Composite())
          .startVariable("customer")
            .initialValue(new StringDescriptor("coca-cola"))
          .endVariable()
          .startActivity("right-inner", new WaitState())
            .startVariable("date")
              .initialValue(new StringDescriptor("today"))
            .endVariable()
          .endActivity()
        .endActivity()
      .endActivity()
    .endProcess();
    
    ClientExecution processInstance = processDefinition.startProcessInstance();
    OpenExecution outerExecution = processInstance.getExecution("outer");
    OpenExecution leftMiddleExecution = outerExecution.getExecution("left-middle");
    OpenExecution leftInnerExecution = leftMiddleExecution.getExecution("left-inner");
    OpenExecution leftStartExecution = leftInnerExecution.getExecution("left-start");

    Map<String, Object> expectedVariables = new HashMap<String, Object>();
    expectedVariables.put("flight", "B52");
    expectedVariables.put("duration", "22 minutes");
    expectedVariables.put("altitude", "31000 ft");
    expectedVariables.put("passengers", "52");
    expectedVariables.put("fuel", "kerosine");
    assertEquals(expectedVariables, leftStartExecution.getVariables());

    processInstance.signal(leftStartExecution);

    OpenExecution rightMiddleExecution = outerExecution.getExecution("right-middle");
    OpenExecution rightInnerExecution = rightMiddleExecution.getExecution("right-inner");

    expectedVariables = new HashMap<String, Object>();
    expectedVariables.put("flight", "B52");
    expectedVariables.put("duration", "22 minutes");
    
    assertEquals(expectedVariables, outerExecution.getVariables());
    
    expectedVariables.put("customer", "coca-cola");
    assertEquals(expectedVariables, rightMiddleExecution.getVariables());

    expectedVariables.put("date", "today");
    assertEquals(expectedVariables, rightInnerExecution.getVariables());
  }


}
