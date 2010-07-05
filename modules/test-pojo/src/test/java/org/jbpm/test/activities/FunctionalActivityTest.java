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
import org.jbpm.api.listener.EventListener;
import org.jbpm.api.listener.EventListenerExecution;
import org.jbpm.api.model.Activity;
import org.jbpm.api.model.OpenExecution;
import org.jbpm.pvm.internal.builder.ProcessDefinitionBuilder;
import org.jbpm.pvm.internal.client.ClientProcessDefinition;
import org.jbpm.pvm.internal.client.ClientProcessInstance;
import org.jbpm.pvm.internal.model.ExecutionImpl;
import org.jbpm.test.BaseJbpmTestCase;


/** shows how functional activities (activities that have a specific function 
 * not related to control transition) can be implemented.
 * 
 * Examples of functional activities could be sending an email, doing a 
 * SQL update on a database, generating a file and so on.
 * 
 * Functional activities can be used as activity behaviour in a transition based 
 * process, event listener and as activity behaviour in nested activity 
 * execution.
 * 
 * @author Tom Baeyens
 */
public class FunctionalActivityTest extends BaseJbpmTestCase {

  public static class FunctionalActivity implements ActivityBehaviour, EventListener {
    private static final long serialVersionUID = 1L;
    List<String> events;
    public FunctionalActivity(List<String> events) {
      this.events = events;
    }
    public void execute(ActivityExecution execution) {
      perform(execution);
    }
    public void notify(EventListenerExecution execution) {
      perform(execution);
    }
    void perform(OpenExecution execution) {
      events.add("performed automatic activity");
    }
  }

  public void testFunctionalActivityAsActivityBehaviourWithTransitions() {
    List<String> recordedEvents = new ArrayList<String>(); 
    FunctionalActivity functionalActivity = new FunctionalActivity(recordedEvents);

    ClientProcessDefinition processDefinition = ProcessDefinitionBuilder
    .startProcess()
      .startActivity("a", functionalActivity)
        .initial()
        .transition("b")
      .endActivity()
      .startActivity("b", functionalActivity)
        .transition("c")
      .endActivity()
      .startActivity("c", functionalActivity)
      .endActivity()
    .endProcess();
    
    ClientProcessInstance processInstance = processDefinition.createProcessInstance();
    
    List<String> expectedEvents = new ArrayList<String>();
    
    assertFalse(processInstance.isEnded());
    assertEquals(expectedEvents, recordedEvents);
    
    processInstance.start();
    
    expectedEvents.add("performed automatic activity");
    expectedEvents.add("performed automatic activity");
    expectedEvents.add("performed automatic activity");

    assertEquals(expectedEvents, recordedEvents);
  }

  
  public static class AutomaticActivity implements ActivityBehaviour {
    private static final long serialVersionUID = 1L;
    public void execute(ActivityExecution execution) {
    }
  }

  public void testFunctionalActivityAsEventListener() {
    List<String> recordedEvents = new ArrayList<String>(); 
    FunctionalActivity functionalActivity = new FunctionalActivity(recordedEvents);

    ClientProcessDefinition processDefinition = ProcessDefinitionBuilder
    .startProcess()
      .startActivity("a", new AutomaticActivity())
        .initial()
        .startFlow("b")
          .listener(functionalActivity)
        .endFlow()
      .endActivity()
      .startActivity("b", new AutomaticActivity())
      .endActivity()
    .endProcess();
    
    ClientProcessInstance processInstance = processDefinition.createProcessInstance();
    processInstance.start();
    
    List<String> expectedEvents = new ArrayList<String>();
    expectedEvents.add("performed automatic activity");

    assertEquals(expectedEvents, recordedEvents);
  }

  public static class Composite implements ExternalActivityBehaviour {
    private static final long serialVersionUID = 1L;
    public void execute(ActivityExecution execution) {
      execute((ExecutionImpl)execution);
    }
    public void execute(ExecutionImpl execution) {
      Activity nestedActivity = execution.getActivity().getActivities().get(0);
      execution.execute(nestedActivity);
    }
    public void signal(ActivityExecution execution, String signalName, Map<String, ?> parameters) {
    }
  }

  public void testFunctionalActivityAsNestedActivity() {
    List<String> recordedEvents = new ArrayList<String>(); 
    FunctionalActivity functionalActivity = new FunctionalActivity(recordedEvents);

    ClientProcessDefinition processDefinition = ProcessDefinitionBuilder
    .startProcess()
      .startActivity("a", new Composite())
        .initial()
        .startActivity(functionalActivity)
        .endActivity()
      .endActivity()
    .endProcess();
    
    ClientProcessInstance processInstance = processDefinition.createProcessInstance();
    processInstance.start();
    
    List<String> expectedEvents = new ArrayList<String>();
    expectedEvents.add("performed automatic activity");

    assertEquals(expectedEvents, recordedEvents);
  }

}
