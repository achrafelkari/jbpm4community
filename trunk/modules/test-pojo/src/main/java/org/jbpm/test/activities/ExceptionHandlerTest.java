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
import org.jbpm.api.listener.EventListener;
import org.jbpm.api.listener.EventListenerExecution;
import org.jbpm.api.model.Event;
import org.jbpm.pvm.internal.builder.ProcessDefinitionBuilder;
import org.jbpm.pvm.internal.client.ClientExecution;
import org.jbpm.pvm.internal.client.ClientProcessDefinition;
import org.jbpm.pvm.internal.client.ClientProcessInstance;
import org.jbpm.test.BaseJbpmTestCase;

/**
 * @author Tom Baeyens
 */
public class ExceptionHandlerTest extends BaseJbpmTestCase
{
  
  public static class WaitState implements ExternalActivityBehaviour {
    private static final long serialVersionUID = 1L;
    public void execute(ActivityExecution execution) {
      execution.waitForSignal();
    }
    public void signal(ActivityExecution execution, String signalName, Map<String, ?> parameters){
    }
  }

  public static class Catcher implements EventListener {
    private static final long serialVersionUID = 1L;
    int timesInvoked = 0;
    public void notify(EventListenerExecution execution) {
      timesInvoked++;
    }
  }

  public static class Batter implements EventListener {
    private static final long serialVersionUID = 1L;
    public void notify(EventListenerExecution execution) {
      throw new RuntimeException("catch me");
    }
  }
  
  public void testExceptionHandlerOnProcessDefinition() {
    Catcher catcher = new Catcher();
    ClientProcessDefinition processDefinition = ProcessDefinitionBuilder
    .startProcess()
      .startExceptionHandler(RuntimeException.class)
        .listener(catcher)
      .endExceptionHandler()
      .startActivity("initial", new WaitState())
        .initial()
        .transition("end")
        .startEvent(Event.END)
          .listener(new Batter())
        .endEvent()
      .endActivity()
      .startActivity("end", new WaitState())
      .endActivity()
    .endProcess();

    ClientProcessInstance processInstance = processDefinition.startProcessInstance();

    assertTrue(processInstance.isActive("initial"));
    assertEquals(0, catcher.timesInvoked);
    processInstance.signal();
    assertEquals(1, catcher.timesInvoked);
    assertTrue(processInstance.isActive("end"));
  }

  public void testExceptionHandlerOnEvent() {
    Catcher catcher = new Catcher();
    ClientProcessDefinition processDefinition = ProcessDefinitionBuilder
    .startProcess()
      .startActivity("initial", new WaitState())
        .initial()
        .transition("end")
        .startEvent(Event.END)
          .startExceptionHandler(RuntimeException.class)
            .listener(catcher)
          .endExceptionHandler()
          .listener(new Batter())
        .endEvent()
      .endActivity()
      .startActivity("end", new WaitState())
      .endActivity()
    .endProcess();

    ClientProcessInstance processInstance = processDefinition.startProcessInstance();

    assertTrue(processInstance.isActive("initial"));
    assertEquals(0, catcher.timesInvoked);
    processInstance.signal();
    assertEquals(1, catcher.timesInvoked);
    assertTrue(processInstance.isActive("end"));
  }

  public void testExceptionHandlerOnAction() {
    Catcher catcher = new Catcher();
    ClientProcessDefinition processDefinition = ProcessDefinitionBuilder
    .startProcess()
      .startActivity("initial", new WaitState())
        .initial()
        .transition("end")
        .startEvent(Event.END)
          .listener(new Batter())
          .startExceptionHandler(RuntimeException.class)
            .listener(catcher)
          .endExceptionHandler()
        .endEvent()
      .endActivity()
      .startActivity("end", new WaitState())
      .endActivity()
    .endProcess();

    ClientProcessInstance processInstance = processDefinition.startProcessInstance();

    assertTrue(processInstance.isActive("initial"));
    assertEquals(0, catcher.timesInvoked);
    processInstance.signal();
    assertEquals(1, catcher.timesInvoked);
    assertTrue(processInstance.isActive("end"));
  }

  public void testExceptionHandlerOnOtherActivity() {
    Catcher catcher = new Catcher();
    ClientProcessDefinition processDefinition = ProcessDefinitionBuilder
    .startProcess()
      .startActivity("initial",new WaitState())
        .initial()
        .transition("end")
        .startEvent(Event.END)
          .listener(new Batter())
        .endEvent()
      .endActivity()
      .startActivity("end", new WaitState())
        .startExceptionHandler(RuntimeException.class)
          .listener(catcher)
        .endExceptionHandler()
      .endActivity()
    .endProcess();

    ClientProcessInstance processInstance = processDefinition.startProcessInstance();

    assertTrue(processInstance.isActive("initial"));
    assertEquals(0, catcher.timesInvoked);
    try {
      processInstance.signal();
      fail("expected exception");
    } catch (RuntimeException e) {
      // OK
    }
  }

  public void testExceptionHandlerOnOtherEvent() {
    Catcher catcher = new Catcher();
    ClientProcessDefinition processDefinition = ProcessDefinitionBuilder.startProcess()
      .startActivity("initial", new WaitState())
        .initial()
        .transition("end")
        .startEvent("other")
          .startExceptionHandler(RuntimeException.class)
            .listener(catcher)
          .endExceptionHandler()
        .endEvent()
        .startEvent(Event.END)
          .listener(new Batter())
        .endEvent()
      .endActivity()
      .startActivity("end", new WaitState())
      .endActivity()
    .endProcess();

    ClientProcessInstance processInstance = processDefinition.startProcessInstance();

    assertTrue(processInstance.isActive("initial"));
    try {
      processInstance.signal();
      fail("expected exception");
    } catch (RuntimeException e) {
      // OK
    }
  }

  public static class BehavedAction implements EventListener {
    private static final long serialVersionUID = 1L;
    public void notify(EventListenerExecution execution) {
      // behaving.  not throwing any exception
    }
  }

  public void testUnmatchedExceptionHandlerOnAction() {
    Catcher catcher = new Catcher();
    ClientProcessDefinition processDefinition = ProcessDefinitionBuilder.startProcess()
      .startActivity("initial", new WaitState())
        .initial()
        .transition("end")
        .startEvent(Event.END)
          .startExceptionHandler(NullPointerException.class)
            .listener(catcher)
          .endExceptionHandler()
          .listener(new Batter())
        .endEvent()
      .endActivity()
      .startActivity("end", new WaitState())
      .endActivity()
    .endProcess();

    ClientProcessInstance processInstance = processDefinition.startProcessInstance();

    assertTrue(processInstance.isActive("initial"));
    try {
      processInstance.signal();
      fail("expected exception");
    } catch (RuntimeException e) {
      // OK
    }
  }

  public static class RethrowingCatcher implements EventListener {
    private static final long serialVersionUID = 1L;
    public void notify(EventListenerExecution execution) {
      // this exception handler will itself throw an exception
      throw new RuntimeException("greetz from the retrhowing catcher");
    }
  }


  public void testRethrowingExceptionHandler() {
    RethrowingCatcher rethrowingCatcher = new RethrowingCatcher();
    ClientProcessDefinition processDefinition = ProcessDefinitionBuilder
    .startProcess()
      .startActivity("initial", new WaitState())
        .initial()
        .transition("end")
        .startEvent(Event.END)
          .startExceptionHandler(RuntimeException.class)
            .listener(rethrowingCatcher)
          .endExceptionHandler()
          .listener(new Batter())
        .endEvent()
      .endActivity()
      .startActivity("end", new WaitState())
      .endActivity()
    .endProcess();
    
    ClientProcessInstance processInstance = processDefinition.startProcessInstance();

    try {
      processInstance.signal();
      fail("expected exception");
    } catch (RuntimeException e) {
      assertTextPresent("greetz from the retrhowing catcher", e.getMessage());
    }
  }
}
