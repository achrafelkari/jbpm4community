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

import org.jbpm.api.Execution;
import org.jbpm.api.JbpmException;
import org.jbpm.api.activity.ActivityBehaviour;
import org.jbpm.api.activity.ActivityExecution;
import org.jbpm.api.activity.ExternalActivityBehaviour;
import org.jbpm.pvm.internal.builder.ProcessDefinitionBuilder;
import org.jbpm.pvm.internal.client.ClientProcessDefinition;
import org.jbpm.pvm.internal.client.ClientProcessInstance;
import org.jbpm.test.BaseJbpmTestCase;

/** shows the basics of the state property on the execution.
 * 
 * The state is automatically managed by the execution.
 * An execution can only receive external triggers in case it is in 
 * the active state.  
 * 
 * Nested executions can occur in case of scoped executions
 * and in case of concurrent executions.  In both cases, only 
 * leave executions in the execution hierarchy are active.
 * 
 * Executions are either locked or active.  So in any state which 
 * is not active, the execution is locked and cannot accept external 
 * signals.  Executions can be ended in 3 ways:
 * 
 *  1) with execution.end() : then the state will be set to 'ended'
 *  2) with execution.cancel() : then the state will be set to 'cancelled'
 *  3) with execution.end(String) : then the state will be set to the 
 *     given state string.  An exception will be raised if the given 
 *     state maches any of the known states.
 * 
 * @author Tom Baeyens
 */
public class ExecutionStateTest extends BaseJbpmTestCase {

  public static class AutomaticActivity implements ActivityBehaviour {
    private static final long serialVersionUID = 1L;
    public void execute(ActivityExecution execution) {
      assertEquals(Execution.STATE_ACTIVE_ROOT, execution.getState());
    }
  }

  public static class WaitState implements ExternalActivityBehaviour {
    private static final long serialVersionUID = 1L;
    public void execute(ActivityExecution execution) {
      assertEquals(Execution.STATE_ACTIVE_ROOT, execution.getState());
      execution.waitForSignal();
    }
    public void signal(ActivityExecution execution, String signalName, Map<String, ?> parameters) {
      assertEquals(Execution.STATE_ACTIVE_ROOT, execution.getState());
      execution.take(signalName);
    }
  }
  
  public void testBasicState() {
    ClientProcessDefinition processDefinition = ProcessDefinitionBuilder.startProcess()
      .startActivity("a", new AutomaticActivity())
        .initial()
        .transition("b")
      .endActivity()
      .startActivity("b", new WaitState())
        .transition("c")
      .endActivity()
      .startActivity("c", new AutomaticActivity())
      .endActivity()
    .endProcess();

    ClientProcessInstance processInstance = processDefinition.createProcessInstance();
    
    assertEquals(Execution.STATE_CREATED, processInstance.getState());
    
    processInstance.start();

    assertEquals(Execution.STATE_ACTIVE_ROOT, processInstance.getState());

    processInstance.signal();

    assertEquals(Execution.STATE_ENDED, processInstance.getState());
  }

  public void testSignalOnInactiveExecution() {
    ClientProcessDefinition processDefinition = ProcessDefinitionBuilder.startProcess()
      .startActivity("a", new AutomaticActivity())
        .initial()
        .transition("b")
      .endActivity()
      .startActivity("b", new WaitState())
        .transition("c")
        .variable("makesSureThisBecomesALocalScope")
      .endActivity()
      .startActivity("c", new AutomaticActivity())
      .endActivity()
    .endProcess();
  
    ClientProcessInstance processInstance = processDefinition.createProcessInstance();
    processInstance.start();
  
    assertEquals(Execution.STATE_INACTIVE_SCOPE, processInstance.getState());
  
    try {
      processInstance.signal();
      fail("expected exception");
    } catch (JbpmException e) {
      // OK
      assertTextPresent("process-instance is not active: inactive", e.getMessage());
    }
  }
  
  public void testCustomEndState() {
    ClientProcessDefinition processDefinition = ProcessDefinitionBuilder.startProcess()
      .startActivity("a", new AutomaticActivity())
        .initial()
        .transition("b")
      .endActivity()
      .startActivity("b", new WaitState())
        .transition("c")
      .endActivity()
      .startActivity("c", new AutomaticActivity())
      .endActivity()
    .endProcess();
    
    ClientProcessInstance processInstance = processDefinition.createProcessInstance();
    processInstance.start();
    processInstance.end("error");
    
    assertEquals("error", processInstance.getState());
    
    try {
      processInstance.signal();
      fail("expected exception");
    } catch (JbpmException e) {
      // OK
      assertTextPresent("process-instance is not active: error", e.getMessage());
    }
  }

  public void testInvalidCustomStates() {
    ClientProcessDefinition processDefinition = ProcessDefinitionBuilder
    .startProcess()
      .startActivity("a", new AutomaticActivity())
        .initial()
        .transition("b")
      .endActivity()
      .startActivity("b", new WaitState())
        .transition("c")
      .endActivity()
      .startActivity("c", new AutomaticActivity())
      .endActivity()
    .endProcess();
    
    ClientProcessInstance processInstance = processDefinition.createProcessInstance();
    processInstance.start();
    
    try {
      processInstance.end(Execution.STATE_CREATED);
      fail("expected exception");
    } catch (JbpmException e) {
      // OK
      assertTextPresent("invalid end state: "+Execution.STATE_CREATED, e.getMessage());
    }
    try {
      processInstance.end(Execution.STATE_ACTIVE_ROOT);
      fail("expected exception");
    } catch (JbpmException e) {
      // OK
      assertTextPresent("invalid end state: "+Execution.STATE_ACTIVE_ROOT, e.getMessage());
    }
    try {
      processInstance.end(Execution.STATE_ACTIVE_CONCURRENT);
      fail("expected exception");
    } catch (JbpmException e) {
      // OK
      assertTextPresent("invalid end state: "+Execution.STATE_ACTIVE_CONCURRENT, e.getMessage());
    }
    try {
      processInstance.end(Execution.STATE_INACTIVE_CONCURRENT_ROOT);
      fail("expected exception");
    } catch (JbpmException e) {
      // OK
      assertTextPresent("invalid end state: "+Execution.STATE_INACTIVE_CONCURRENT_ROOT, e.getMessage());
    }
    try {
      processInstance.end(Execution.STATE_INACTIVE_SCOPE);
      fail("expected exception");
    } catch (JbpmException e) {
      // OK
      assertTextPresent("invalid end state: "+Execution.STATE_INACTIVE_SCOPE, e.getMessage());
    }
    try {
      processInstance.end(Execution.STATE_SUSPENDED);
      fail("expected exception");
    } catch (JbpmException e) {
      // OK
      assertTextPresent("invalid end state: "+Execution.STATE_SUSPENDED, e.getMessage());
    }
    try {
      processInstance.end(Execution.STATE_ASYNC);
      fail("expected exception");
    } catch (JbpmException e) {
      // OK
      assertTextPresent("invalid end state: "+Execution.STATE_ASYNC, e.getMessage());
    }
  }
}
