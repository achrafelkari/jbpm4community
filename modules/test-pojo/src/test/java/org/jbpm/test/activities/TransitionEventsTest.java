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

import org.jbpm.api.listener.EventListener;
import org.jbpm.api.listener.EventListenerExecution;
import org.jbpm.api.model.Event;
import org.jbpm.pvm.internal.builder.ProcessDefinitionBuilder;
import org.jbpm.pvm.internal.client.ClientExecution;
import org.jbpm.pvm.internal.client.ClientProcessDefinition;
import org.jbpm.pvm.internal.client.ClientProcessInstance;
import org.jbpm.pvm.internal.model.ExecutionImpl;
import org.jbpm.test.BaseJbpmTestCase;

/**
 * @author Tom Baeyens
 */
public class TransitionEventsTest extends BaseJbpmTestCase {
  
  public class Recorder implements EventListener {
    private static final long serialVersionUID = 1L;
    public List<Object> events = new ArrayList<Object>();
    public void notify(EventListenerExecution execution) {
      notify((ExecutionImpl)execution);
    }
    public void notify(ExecutionImpl execution) {
      events.add(execution.getEvent()+" on "+execution.getEventSource());
    }
  }

  public void testBasicEvents(){
    Recorder fromListener = new Recorder();
    Recorder toListener = new Recorder();
    Recorder transitionListener = new Recorder();

    /*
    +------+        +----+    
    | from |------->| to |
    +------+        +----+    
    */
    
    ClientProcessDefinition processDefinition = ProcessDefinitionBuilder
    .startProcess("p")
      .startActivity("from", new WaitState())
        .initial()
        .startEvent(Event.END) 
          .listener(fromListener)
        .endEvent()
        .startFlow("to")
          .listener(transitionListener)
        .endFlow()
      .endActivity()
      .startActivity("to", new WaitState())
        .startEvent(Event.START) 
          .listener(toListener)
        .endEvent()
      .endActivity()
    .endProcess();

    ClientExecution execution = processDefinition.startProcessInstance();
    execution.signal();

    assertEquals("[event(end) on activity(from)]", 
            fromListener.events.toString());

    assertEquals("[event(take) on (from)-->(to)]", 
            transitionListener.events.toString());

    assertEquals("[event(start) on activity(to)]", 
            toListener.events.toString());
  }

  public void testCompositeLeave(){
    Recorder processListener = new Recorder();
    Recorder outsideListener = new Recorder();
    Recorder compositeListener = new Recorder();
    Recorder insideListener = new Recorder();

    /*
    +--------------+
    | composite    |
    |  +--------+  |     +---------+    
    |  | inside |------->| outside |
    |  +--------+  |     +---------+    
    +--------------+
    */
    
    ClientProcessDefinition processDefinition = ProcessDefinitionBuilder
    .startProcess("p")
      .startEvent(Event.END) 
        .listener(processListener)
      .endEvent()
      .startEvent(Event.START) 
        .listener(processListener)
      .endEvent()
      .startEvent(Event.TAKE) 
        .listener(processListener)
      .endEvent()
      .startActivity("composite")
        .startEvent(Event.END) 
          .listener(compositeListener)
        .endEvent()
        .startEvent(Event.START) 
          .listener(compositeListener)
        .endEvent()
        .startEvent(Event.TAKE) 
          .listener(compositeListener)
        .endEvent()
        .startActivity("inside", new WaitState())
          .initial()
          .transition("outside")
          .startEvent(Event.END) 
            .listener(insideListener)
          .endEvent()
          .startEvent(Event.START) 
            .listener(insideListener)
          .endEvent()
          .startEvent(Event.TAKE) 
            .listener(insideListener)
          .endEvent()
        .endActivity()
      .endActivity()
      .startActivity("outside", new WaitState())
        .startEvent(Event.END) 
          .listener(outsideListener)
        .endEvent()
        .startEvent(Event.START) 
          .listener(outsideListener)
        .endEvent()
        .startEvent(Event.TAKE) 
          .listener(outsideListener)
        .endEvent()
      .endActivity()
    .endProcess();

    ClientProcessInstance processInstance = processDefinition.startProcessInstance();

    assertEquals("[event(start) on process(p)]", 
                 processListener.events.toString());

    assertEquals(0, outsideListener.events.size());
    assertEquals(0, compositeListener.events.size());
    assertEquals(0, insideListener.events.size());
    
    assertTrue(processInstance.isActive("inside"));

    processInstance.signal();
    
    assertEquals("[event(start) on process(p)]", 
                 processListener.events.toString());

    assertEquals("[event(start) on activity(outside)]",
                 outsideListener.events.toString());
    
    assertEquals("[event(end) on activity(composite)]",
                 compositeListener.events.toString());
    
    assertEquals("[event(end) on activity(inside)]",
                 insideListener.events.toString());
  }

  public void testCompositeEnter(){
    Recorder processListener = new Recorder();
    Recorder outsideListener = new Recorder();
    Recorder compositeListener = new Recorder();
    Recorder insideListener = new Recorder();

    /*
                   +--------------+
                   | composite    |
    +---------+    |  +--------+  |
    | outside |------>| inside |  |
    +---------+    |  +--------+  |
                   +--------------+
    */
    
    ClientProcessDefinition processDefinition = ProcessDefinitionBuilder
    .startProcess("p")
      .startEvent(Event.END) 
        .listener(processListener)
      .endEvent()
      .startEvent(Event.START) 
        .listener(processListener)
      .endEvent()
      .startEvent(Event.TAKE) 
        .listener(processListener)
      .endEvent()
      .startActivity("outside", new WaitState())
        .initial()
        .transition("inside")
        .startEvent(Event.END) 
          .listener(outsideListener)
        .endEvent()
        .startEvent(Event.START) 
          .listener(outsideListener)
        .endEvent()
        .startEvent(Event.TAKE) 
          .listener(outsideListener)
        .endEvent()
      .endActivity()
      .startActivity("composite")
        .startEvent(Event.END) 
          .listener(compositeListener)
        .endEvent()
        .startEvent(Event.START) 
          .listener(compositeListener)
        .endEvent()
        .startEvent(Event.TAKE) 
          .listener(compositeListener)
        .endEvent()
        .startActivity("inside", new WaitState())
          .startEvent(Event.END) 
            .listener(insideListener)
          .endEvent()
          .startEvent(Event.START) 
            .listener(insideListener)
          .endEvent()
          .startEvent(Event.TAKE) 
            .listener(insideListener)
          .endEvent()
        .endActivity()
      .endActivity()
    .endProcess();
    
    ClientExecution execution = processDefinition.startProcessInstance();

    assertEquals("[event(start) on process(p)]", 
                 processListener.events.toString());

    assertEquals(0, outsideListener.events.size());
    assertEquals(0, compositeListener.events.size());
    assertEquals(0, insideListener.events.size());

    execution.signal();
    
    assertEquals("[event(start) on process(p)]", 
                 processListener.events.toString());

    assertEquals("[event(end) on activity(outside)]",
                 outsideListener.events.toString());
    
    assertEquals("[event(start) on activity(composite)]",
    		     compositeListener.events.toString());
    
    assertEquals("[event(start) on activity(inside)]",
                 insideListener.events.toString());
  }

  public void testSelfTransition(){
    Recorder processListener = new Recorder();
    Recorder compositeListener = new Recorder();
    Recorder insideListener = new Recorder();

    /*
    +-----------------+
    | composite       |
    |  +--------+     |    
    |  | inside |---+ |
    |  |        |   | |
    |  |        |<--+ |
    |  +--------+     |    
    +-----------------+
    */
    
    ClientProcessDefinition processDefinition = ProcessDefinitionBuilder
    .startProcess("p")
      .startEvent(Event.END) 
        .listener(processListener)
      .endEvent()
      .startEvent(Event.START) 
        .listener(processListener)
      .endEvent()
      .startEvent(Event.TAKE) 
        .listener(processListener)
      .endEvent()
      .startActivity("composite")
        .startEvent(Event.END) 
          .listener(compositeListener)
        .endEvent()
        .startEvent(Event.START) 
          .listener(compositeListener)
        .endEvent()
        .startEvent(Event.TAKE) 
          .listener(compositeListener)
        .endEvent()
        .startActivity("inside", new WaitState())
          .initial()
          .transition("inside")
          .startEvent(Event.END) 
            .listener(insideListener)
          .endEvent()
          .startEvent(Event.START) 
            .listener(insideListener)
          .endEvent()
          .startEvent(Event.TAKE) 
            .listener(insideListener)
          .endEvent()
        .endActivity()
      .endActivity()
    .endProcess();

    ClientProcessInstance processInstance = processDefinition.startProcessInstance();

    assertEquals("[event(start) on process(p)]", 
                 processListener.events.toString());

    assertEquals(0, compositeListener.events.size());
    assertEquals(0, insideListener.events.size());
    
    assertTrue(processInstance.isActive("inside"));

    processInstance.signal();
    
    assertEquals("[event(start) on process(p)]", 
                 processListener.events.toString());
    
    assertEquals(0, compositeListener.events.size());
    
    assertEquals("[event(end) on activity(inside), " +
    		      "event(start) on activity(inside)]",
                 insideListener.events.toString());
  }

  public void testPropagateSelfTransition(){
    Recorder processListener = new Recorder();
    Recorder compositeListener = new Recorder();

    /*
    +-----------------+
    | composite       |
    |  +--------+     |    
    |  | inside |---+ |
    |  |        |   | |
    |  |        |<--+ |
    |  +--------+     |    
    +-----------------+
    */
    
    ClientProcessDefinition processDefinition = ProcessDefinitionBuilder
    .startProcess("p")
      .startEvent(Event.END) 
        .listener(processListener, true)
      .endEvent()
      .startEvent(Event.START) 
        .listener(processListener, true)
      .endEvent()
      .startEvent(Event.TAKE) 
        .listener(processListener, true)
      .endEvent()
      .startActivity("composite")
        .startEvent(Event.END) 
          .listener(compositeListener, true)
        .endEvent()
        .startEvent(Event.START) 
          .listener(compositeListener, true)
        .endEvent()
        .startEvent(Event.TAKE) 
          .listener(compositeListener, true)
        .endEvent()
        .startActivity("inside", new WaitState())
          .initial()
          .transition("inside")
        .endActivity()
      .endActivity()
    .endProcess();

    ClientProcessInstance processInstance = processDefinition.startProcessInstance();

    assertEquals("[event(start) on process(p)]", 
                 processListener.events.toString());

    assertEquals(0, compositeListener.events.size());
    
    assertTrue(processInstance.isActive("inside"));

    processInstance.signal();
    
    assertEquals("[event(end) on activity(inside), " +
                  "event(take) on (inside)-->(inside), " +
                  "event(start) on activity(inside)]",
                 compositeListener.events.toString());

    assertEquals("[event(start) on process(p), " +
    		      "event(end) on activity(inside), " +
                  "event(take) on (inside)-->(inside), " +
                  "event(start) on activity(inside)]", 
                 processListener.events.toString());
  }


  public void testCompositeLeaveInheritedTransition(){
    Recorder processListener = new Recorder();
    Recorder outsideListener = new Recorder();
    Recorder compositeListener = new Recorder();
    Recorder insideListener = new Recorder();

    /*
    +--------------+        +---------+ 
    | composite    |------->| outside |
    |  +--------+  |        +---------+   
    |  | inside |  |
    |  +--------+  |
    +--------------+
    */
    
    ClientProcessDefinition processDefinition = ProcessDefinitionBuilder
    .startProcess("p")
      .startEvent(Event.END) 
        .listener(processListener)
      .endEvent()
      .startEvent(Event.START) 
        .listener(processListener)
      .endEvent()
      .startEvent(Event.TAKE) 
        .listener(processListener)
      .endEvent()
      .startActivity("composite")
        .transition("outside")
        .startEvent(Event.END) 
          .listener(compositeListener)
        .endEvent()
        .startEvent(Event.START) 
          .listener(compositeListener)
        .endEvent()
        .startEvent(Event.TAKE) 
          .listener(compositeListener)
        .endEvent()
        .startActivity("inside", new WaitState())
          .initial()
          .startEvent(Event.END) 
            .listener(insideListener)
          .endEvent()
          .startEvent(Event.START) 
            .listener(insideListener)
          .endEvent()
          .startEvent(Event.TAKE) 
            .listener(insideListener)
          .endEvent()
        .endActivity()
      .endActivity()
      .startActivity("outside", new WaitState())
        .startEvent(Event.END) 
          .listener(outsideListener)
        .endEvent()
        .startEvent(Event.START) 
          .listener(outsideListener)
        .endEvent()
        .startEvent(Event.TAKE) 
          .listener(outsideListener)
        .endEvent()
      .endActivity()
    .endProcess();

    ClientProcessInstance processInstance = processDefinition.startProcessInstance();

    assertEquals("[event(start) on process(p)]", 
                 processListener.events.toString());

    assertEquals(0, outsideListener.events.size());
    assertEquals(0, compositeListener.events.size());
    assertEquals(0, insideListener.events.size());
    
    assertTrue(processInstance.isActive("inside"));

    processInstance.signal();
    
    assertEquals("[event(start) on process(p)]", 
                 processListener.events.toString());

    assertEquals("[event(start) on activity(outside)]",
                 outsideListener.events.toString());
    
    assertEquals("[event(end) on activity(composite)]",
                 compositeListener.events.toString());
    
    assertEquals("[event(end) on activity(inside)]",
                 insideListener.events.toString());
  }

  public void testCompositeLeaveInheritedTransitionExtraNesting(){
    Recorder processListener = new Recorder();
    Recorder sourceOutsideListener = new Recorder();
    Recorder sourceMiddleListener = new Recorder();
    Recorder sourceInsideListener = new Recorder();
    Recorder destinationOutsideListener = new Recorder();
    Recorder destinationInsideListener = new Recorder();

    /*
    +--------------------------+ 
    | source outside           |
    |  +--------------------+  |      +--------------------------+ 
    |  | source middle      |  |      | destination outside      |
    |  |  +---------------+ |  |      |  +--------------------+  |   
    |  |  | source inside | |----------->| destination inside |  |
    |  |  +---------------+ |  |      |  +--------------------+  |
    |  +--------------------+  |      +--------------------------+
    +--------------------------+
    */

    ClientProcessDefinition processDefinition = ProcessDefinitionBuilder
    .startProcess("p")
      .startEvent(Event.END) 
        .listener(processListener)
      .endEvent()
      .startEvent(Event.START) 
        .listener(processListener)
      .endEvent()
      .startEvent(Event.TAKE) 
        .listener(processListener)
      .endEvent()
      .startActivity("source outside")
        .startEvent(Event.END) 
          .listener(sourceOutsideListener)
        .endEvent()
        .startEvent(Event.START) 
          .listener(sourceOutsideListener)
        .endEvent()
        .startEvent(Event.TAKE) 
          .listener(sourceOutsideListener)
        .endEvent()
        .startActivity("source middle")
          .transition("destination inside")
          .startEvent(Event.END) 
            .listener(sourceMiddleListener)
          .endEvent()
          .startEvent(Event.START) 
            .listener(sourceMiddleListener)
          .endEvent()
          .startEvent(Event.TAKE) 
            .listener(sourceMiddleListener)
          .endEvent()
          .startActivity("source inside", new WaitState())
            .initial()
            .startEvent(Event.END) 
              .listener(sourceInsideListener)
            .endEvent()
            .startEvent(Event.START) 
              .listener(sourceInsideListener)
            .endEvent()
            .startEvent(Event.TAKE) 
              .listener(sourceInsideListener)
            .endEvent()
          .endActivity()
        .endActivity()
      .endActivity()
      .startActivity("destination outside")
        .startEvent(Event.END) 
          .listener(destinationOutsideListener)
        .endEvent()
        .startEvent(Event.START) 
          .listener(destinationOutsideListener)
        .endEvent()
        .startEvent(Event.TAKE) 
          .listener(destinationOutsideListener)
        .endEvent()
        .startActivity("destination inside", new WaitState())
          .startEvent(Event.END) 
            .listener(destinationInsideListener)
          .endEvent()
          .startEvent(Event.START) 
            .listener(destinationInsideListener)
          .endEvent()
          .startEvent(Event.TAKE) 
            .listener(destinationInsideListener)
          .endEvent()
        .endActivity()
      .endActivity()
    .endProcess();

    
    ClientProcessInstance processInstance = processDefinition.startProcessInstance();

    assertTrue(processInstance.isActive("source inside"));

    processInstance.signal();
    
    assertEquals("[event(start) on process(p)]", 
                 processListener.events.toString());
    
    assertEquals("[event(end) on activity(source outside)]", 
                 sourceOutsideListener.events.toString());
    
    assertEquals("[event(end) on activity(source middle)]", 
                 sourceMiddleListener.events.toString());

    assertEquals("[event(end) on activity(source inside)]", 
                 sourceInsideListener.events.toString());

    assertEquals("[event(start) on activity(destination outside)]", 
                 destinationOutsideListener.events.toString());

    assertEquals("[event(start) on activity(destination inside)]", 
                 destinationInsideListener.events.toString());
  }
}
