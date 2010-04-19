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

import java.util.Map;

import org.jbpm.api.activity.ActivityExecution;
import org.jbpm.api.activity.ExternalActivityBehaviour;
import org.jbpm.api.model.OpenExecution;
import org.jbpm.pvm.internal.builder.ProcessDefinitionBuilder;
import org.jbpm.pvm.internal.client.ClientExecution;
import org.jbpm.pvm.internal.client.ClientProcessDefinition;
import org.jbpm.test.BaseJbpmTestCase;

/**
 *  
 *    +---+     +--------+     +---+ 
 *    | a |     | b      |     | c |
 *    |   | --> |        | --> |   |  
 *    |   |     | SCOPE! |     |   |
 *    +---+     +--------+     +---+  
 *
 * @author Tom Baeyens
 */
public class ScopeVariableTest extends BaseJbpmTestCase
{

  public static class WaitState implements ExternalActivityBehaviour {
    private static final long serialVersionUID = 1L;
    public void execute(ActivityExecution execution) {
      execution.waitForSignal();
    }
    public void signal(ActivityExecution execution, String signalName, Map<String, ?> parameters) {
      execution.take(signalName);
    }
  }
  
  public void testOuterscopeLookup() {
    ClientProcessDefinition processDefinition = ProcessDefinitionBuilder
    .startProcess()
      .startActivity("a", new WaitState())
        .initial()
        .transition("b")
      .endActivity()
      .startActivity("b", new WaitState())
        .transition("c")
        .variable("makesSureThisBecomesALocalScope")
      .endActivity()
      .startActivity("c", new WaitState())
      .endActivity()
    .endProcess();
  
    ClientExecution processInstance = processDefinition.startProcessInstance();
    processInstance.setVariable("destination", "anywhere");
    
    processInstance.signal();
    
    OpenExecution bScope = processInstance.getExecution("b");

    // check if the global vars are still visible within the scope for b.
    assertEquals("anywhere", bScope.getVariable("destination"));
    
    bScope.createVariable("temp", "23C");
    assertEquals("23C", bScope.getVariable("temp"));
    assertNull(processInstance.getVariable("temp"));
  }

  public void testLocalVariableLookup() {
    ClientProcessDefinition processDefinition = ProcessDefinitionBuilder
    .startProcess()
      .startActivity("a", new WaitState())
        .initial()
        .transition("b")
      .endActivity()
      .startActivity("b", new WaitState())
        .transition("c")
        .variable("makesSureThisBecomesALocalScope")
      .endActivity()
      .startActivity("c", new WaitState())
      .endActivity()
    .endProcess();
  
    ClientExecution processInstance = processDefinition.startProcessInstance();

    processInstance.signal();
    OpenExecution bScope = processInstance.getExecution("b");

    bScope.createVariable("temp", "23C");
    
    assertEquals("23C", bScope.getVariable("temp"));
    assertNull(processInstance.getVariable("temp"));
    
    processInstance.signal(bScope);
    
    assertTrue(bScope.isEnded());
    assertNull(processInstance.getVariable("temp"));
  }

  public void testLocalVariableUpdate() {
    ClientProcessDefinition processDefinition = ProcessDefinitionBuilder
    .startProcess()
      .startActivity("a", new WaitState())
        .initial()
        .transition("b")
      .endActivity()
      .startActivity("b", new WaitState())
        .transition("c")
        .variable("makesSureThisBecomesALocalScope")
      .endActivity()
      .startActivity("c", new WaitState())
      .endActivity()
    .endProcess();
  
    ClientExecution processInstance = processDefinition.startProcessInstance();

    processInstance.signal();
    OpenExecution bScope = processInstance.getExecution("b");

    bScope.createVariable("temp", "23C");
    bScope.setVariable("temp", "28C");
    
    assertEquals("28C", bScope.getVariable("temp"));
    
    processInstance.signal(bScope);
    
    assertTrue(bScope.isEnded());
    assertNull(processInstance.getVariable("temp"));
  }

  public void testDefaultCreationOnGlobalScope() {
    ClientProcessDefinition processDefinition = ProcessDefinitionBuilder
    .startProcess()
      .startActivity("a", new WaitState())
        .initial()
        .transition("b")
      .endActivity()
      .startActivity("b", new WaitState())
        .transition("c")
        .variable("makesSureThisBecomesALocalScope")
      .endActivity()
      .startActivity("c", new WaitState())
      .endActivity()
    .endProcess();
  
    ClientExecution processInstance = processDefinition.startProcessInstance();

    processInstance.signal();
    OpenExecution bScope = processInstance.getExecution("b");

    bScope.setVariable("temp", "28C");
    assertEquals("28C", bScope.getVariable("temp"));
    
    processInstance.signal(bScope);
    
    assertTrue(bScope.isEnded());
    assertEquals("28C", processInstance.getVariable("temp"));
  }

  public void testVariableUpdatesOnEndedScope() {
    ClientProcessDefinition processDefinition = ProcessDefinitionBuilder
    .startProcess()
      .startActivity("a", new WaitState())
        .initial()
        .transition("b")
      .endActivity()
      .startActivity("b", new WaitState())
        .transition("c")
        .variable("makesSureThisBecomesALocalScope")
      .endActivity()
      .startActivity("c", new WaitState())
      .endActivity()
    .endProcess();
  
    ClientExecution processInstance = processDefinition.startProcessInstance();

    processInstance.signal();
    OpenExecution bScope = processInstance.getExecution("b");

    bScope.createVariable("temp", "28C");
    
    processInstance.signal(bScope);
  }
}
