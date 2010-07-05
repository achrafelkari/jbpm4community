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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.jbpm.api.activity.ActivityBehaviour;
import org.jbpm.api.activity.ActivityExecution;
import org.jbpm.api.activity.ExternalActivityBehaviour;
import org.jbpm.pvm.internal.builder.ProcessDefinitionBuilder;
import org.jbpm.pvm.internal.client.ClientExecution;
import org.jbpm.pvm.internal.client.ClientProcessDefinition;
import org.jbpm.pvm.internal.client.ClientProcessInstance;
import org.jbpm.test.BaseJbpmTestCase;

/** shows wait states and automatic activities in a simple 
 * sequence based on transitions.
 * 
 * @author Tom Baeyens
 */
public class BasicExecutionFlowTest extends BaseJbpmTestCase {
  
  // automatic activity will log an event in a given list
  
  public static class AutomaticActivity implements ActivityBehaviour {
    private static final long serialVersionUID = 1L;
    List<String> events;
    public AutomaticActivity(List<String> events) {
      this.events = events;
    }
    public void execute(ActivityExecution execution) {
      events.add("execute["+execution.getActivityName()+"]");
    }
  }

  public static class WaitState implements ExternalActivityBehaviour {
    private static final long serialVersionUID = 1L;
    List<String> events;
    public WaitState(List<String> events) {
      this.events = events;
    }
    public void execute(ActivityExecution execution) {
      events.add("execute["+execution.getActivityName()+"]");
      execution.waitForSignal();
    }
    public void signal(ActivityExecution execution, String signalName, Map<String, ?> parameters) throws Exception {
      events.add("signal["+execution.getActivityName()+"]");
      execution.take(signalName);
    }
  }

  public void testChainOfAutomaticActivitiesAndWaitStates() {
    List<String> recordedEvents = new ArrayList<String>(); 
    AutomaticActivity automaticActivity = new AutomaticActivity(recordedEvents);
    WaitState waitState = new WaitState(recordedEvents);

    ClientProcessDefinition processDefinition = ProcessDefinitionBuilder
    .startProcess("loan")
      .startActivity("submit loan request", automaticActivity)
        .initial()
        .transition("evaluate")
      .endActivity()
      .startActivity("evaluate", waitState)
        .transition("wire money", "approve")
        .transition("end", "reject")
      .endActivity()
      .startActivity("wire money", automaticActivity)
        .transition("archive")
      .endActivity()
      .startActivity("archive", waitState)
        .transition("end")
      .endActivity()
      .startActivity("end", waitState)
      .endActivity()
    .endProcess();

    ClientProcessInstance processInstance = processDefinition.startProcessInstance();
    
    List<String> expectedEvents = new ArrayList<String>();
    
    expectedEvents.add("execute[submit loan request]");
    expectedEvents.add("execute[evaluate]");

    assertTrue(processInstance.isActive("evaluate"));
    assertEquals(expectedEvents, recordedEvents);

    processInstance.signal("approve");

    expectedEvents.add("signal[evaluate]");
    expectedEvents.add("execute[wire money]");
    expectedEvents.add("execute[archive]");

    assertTrue(processInstance.isActive("archive"));
    assertEquals(expectedEvents, recordedEvents);

    processInstance.signal();

    expectedEvents.add("signal[archive]");
    expectedEvents.add("execute[end]");

    assertTrue(processInstance.isActive("end"));
    assertEquals(expectedEvents, recordedEvents);
  }
  
  public void testDelayedBegin() {
    List<String> recordedEvents = new ArrayList<String>(); 
    AutomaticActivity automaticActivity = new AutomaticActivity(recordedEvents);
    WaitState waitState = new WaitState(recordedEvents);

    ClientProcessDefinition processDefinition = ProcessDefinitionBuilder
    .startProcess("loan")
      .startActivity("submit loan request", automaticActivity)
        .initial()
        .transition("evaluate")
      .endActivity()
      .startActivity("evaluate", waitState)
        .transition("wire money", "approve")
        .transition("end", "reject")
      .endActivity()
      .startActivity("wire money", automaticActivity)
        .transition("archive")
      .endActivity()
      .startActivity("archive", waitState)
        .transition("end")
      .endActivity()
      .startActivity("end", waitState)
      .endActivity()
    .endProcess();
  
    
    ClientProcessInstance processInstance = processDefinition.createProcessInstance();
    
    // here, inbetween create and start of a process instance, the variables can be initialized  
    // or subprocessinstance-superprocessinstance relation can be set up

    // so we verify that the process execution didn't start yet
    List<String> expectedEvents = new ArrayList<String>();
    assertEquals(expectedEvents, recordedEvents);
    
    processInstance.start();

    expectedEvents.add("execute[submit loan request]");
    expectedEvents.add("execute[evaluate]");

    assertTrue(processInstance.isActive("evaluate"));
    assertEquals(expectedEvents, recordedEvents);
  }
}
