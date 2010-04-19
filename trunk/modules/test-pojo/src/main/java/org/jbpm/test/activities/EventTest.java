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

import org.jbpm.api.activity.ActivityBehaviour;
import org.jbpm.api.activity.ActivityExecution;
import org.jbpm.api.activity.ExternalActivityBehaviour;
import org.jbpm.api.listener.EventListener;
import org.jbpm.api.listener.EventListenerExecution;
import org.jbpm.api.model.Event;
import org.jbpm.pvm.internal.builder.ProcessDefinitionBuilder;
import org.jbpm.pvm.internal.client.ClientExecution;
import org.jbpm.pvm.internal.client.ClientProcessDefinition;
import org.jbpm.pvm.internal.client.ClientProcessInstance;
import org.jbpm.pvm.internal.model.ActivityImpl;
import org.jbpm.pvm.internal.model.ExecutionImpl;
import org.jbpm.test.BaseJbpmTestCase;

/** shows how actions are listeners to following processDefinition events:
 * <ul>
 *   <li>activity-leave</li>
 *   <li>transition</li>
 *   <li>activity-enter</li>
 *   <li>custom event</li>
 * </ul>
 *
 * @author Tom Baeyens
 */
public class EventTest extends BaseJbpmTestCase {
  
  // activity leave action ////////////////////////////////////////////////////////
  
  public static class ActivityEndAction implements EventListener {
    private static final long serialVersionUID = 1L;
    public void notify(EventListenerExecution execution) {
      notify((ExecutionImpl)execution);
    }
    public void notify(ExecutionImpl execution) {
      execution.setVariable("msg", "Kilroy was here");

      assertTrue(execution.isActive("initial"));
      assertEquals("initial", execution.getEventSource().getName());
      assertEquals("leave activity action test", execution.getProcessDefinition().getName());
      assertEquals("end", execution.getTransition().getDestination().getName());
    }
  }
  
  public void testEventListenerOnActivityEnd() {
    ActivityEndAction activityEndAction = new ActivityEndAction();
    
    ClientProcessDefinition processDefinition = ProcessDefinitionBuilder
    .startProcess("leave activity action test")
      .startActivity("initial", new WaitState())
        .initial()
        .transition("end")
        .startEvent(Event.END)
          .listener(activityEndAction)
        .endEvent()
      .endActivity()
      .startActivity("end", new WaitState())
      .endActivity()
    .endProcess();

    ClientProcessInstance processInstance = processDefinition.startProcessInstance();

    assertTrue(processInstance.isActive("initial"));
    processInstance.signal();
    assertEquals("Kilroy was here", processInstance.getVariable("msg"));
  }

  // transition action ////////////////////////////////////////////////////////

  public static class TransitionAction implements EventListener {
    private static final long serialVersionUID = 1L;
    public void notify(EventListenerExecution execution) {
      notify((ExecutionImpl)execution);
    }
    public void notify(ExecutionImpl execution) {
      execution.setVariable("msg", "Kilroy was here");
      
      assertEquals("t", execution.getEventSource().getName());
      assertEquals("transition action test", execution.getProcessDefinition().getName());
      assertEquals("end", execution.getTransition().getDestination().getName());
    }
  }
  
  public void testEventListenerOnTransition() {
    TransitionAction transitionAction = new TransitionAction();
    
    ClientProcessDefinition processDefinition = ProcessDefinitionBuilder
    .startProcess("transition action test")
      .startActivity("initial", new WaitState())
        .initial()
        .startFlow("end")
          .name("t")
          .listener(transitionAction)
        .endFlow()
      .endActivity()
      .startActivity("end", new WaitState())
      .endActivity()
    .endProcess();

    ClientProcessInstance processInstance = processDefinition.startProcessInstance();

    assertTrue(processInstance.isActive("initial"));
    processInstance.signal("t");
    assertEquals("Kilroy was here", processInstance.getVariable("msg"));
  }

  // activity enter action ////////////////////////////////////////////////////////

  public static class ActivityStartAction implements EventListener {
    private static final long serialVersionUID = 1L;
    public void notify(EventListenerExecution execution) {
      notify((ExecutionImpl)execution);
    }
    public void notify(ExecutionImpl execution) {
      execution.setVariable("msg", "Kilroy was here");

      assertTrue(execution.isActive("end"));
      assertEquals("end", execution.getEventSource().getName());
      assertEquals("enter activity action test", execution.getProcessDefinition().getName());
      assertEquals("end", execution.getTransition().getDestination().getName());
    }
  }
  
  public void testEventListenerOnActivityStart() {
    ActivityStartAction activityBeginAction = new ActivityStartAction();
    
    ClientProcessDefinition processDefinition = ProcessDefinitionBuilder
    .startProcess("enter activity action test")
      .startActivity("initial", new WaitState())
        .initial()
        .transition("end")
      .endActivity()
      .startActivity("end", new WaitState())
        .startEvent(Event.START)
          .listener(activityBeginAction)
        .endEvent()
      .endActivity()
    .endProcess();

    ClientExecution processInstance = processDefinition.startProcessInstance();

    assertTrue(processInstance.isActive("initial"));
    processInstance.signal();
    assertEquals("Kilroy was here", processInstance.getVariable("msg"));
  }

  // custom event ////////////////////////////////////////////////////////////
  
  public static class WaitStateWithCustomEvent implements ExternalActivityBehaviour {
    private static final long serialVersionUID = 1L;
    public void execute(ActivityExecution execution) throws Exception {
      execution.waitForSignal();
    }
    public void signal(ActivityExecution execution, String signal, Map<String, ?> parameters) throws Exception {
      signal((ExecutionImpl)execution, signal, parameters);
    }
    public void signal(ExecutionImpl execution, String signal, Map<String, ?> parameters) throws Exception {
      ActivityImpl activity = execution.getActivity();
      if ( (signal!=null)
           && (activity!=null)
           && (activity.hasEvent(signal))
         ) {
        execution.fire(signal, activity);
        execution.waitForSignal();
      }
    }
  }

  public static class CheckRivetsAction implements EventListener {
    private static final long serialVersionUID = 1L;
    public void notify(EventListenerExecution execution) {
      notify((ExecutionImpl)execution);
    }
    public void notify(ExecutionImpl execution) {
      execution.setVariable("msg", "Kilroy was here");
      
      assertTrue(execution.isActive("initial"));
      assertEquals("initial", execution.getEventSource().getName());
      assertEquals("custom activity action test", execution.getProcessDefinition().getName());
      assertNull(execution.getTransition());
    }
  }

  public void testCustomEventInActivity() {
    CheckRivetsAction checkRivetsAction = new CheckRivetsAction();
    
    ClientProcessDefinition processDefinition = ProcessDefinitionBuilder
    .startProcess("custom activity action test")
      .startActivity("initial", new WaitStateWithCustomEvent())
        .initial()
        .transition("end")
        .startEvent("end of riveter shift") // http://en.wikipedia.org/wiki/Kilroy_was_here
          .listener(checkRivetsAction)
        .endEvent()
      .endActivity()
      .startActivity("end", new WaitState())
      .endActivity()
    .endProcess();

    ClientExecution execution = processDefinition.startProcessInstance();

    assertTrue(execution.isActive("initial"));
    assertNull(execution.getVariable("msg"));

    execution.signal("end of riveter shift");

    assertTrue(execution.isActive("initial"));
    assertEquals("Kilroy was here", execution.getVariable("msg"));
    
    execution.signal();

    assertTrue(execution.isActive("end"));
  }

  public static class EndState implements ActivityBehaviour {
    private static final long serialVersionUID = 1L;
    public void execute(ActivityExecution execution) throws Exception {
      execution.end();
    }
  }
}
