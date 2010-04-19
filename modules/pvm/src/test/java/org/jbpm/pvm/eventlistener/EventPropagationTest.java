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
package org.jbpm.pvm.eventlistener;

import java.util.ArrayList;
import java.util.List;

import org.jbpm.api.model.Event;
import org.jbpm.pvm.activities.DisplaySource;
import org.jbpm.pvm.activities.TestConsole;
import org.jbpm.pvm.activities.WaitState;
import org.jbpm.pvm.internal.builder.ProcessDefinitionBuilder;
import org.jbpm.pvm.internal.client.ClientExecution;
import org.jbpm.pvm.internal.client.ClientProcessDefinition;
import org.jbpm.test.BaseJbpmTestCase;


public class EventPropagationTest extends BaseJbpmTestCase {

  TestConsole testConsole;
  
  public void setUp() {
    testConsole = TestConsole.install();
  }

  public void tearDown() {
    TestConsole.uninstall();
    testConsole = null;
  }

  public void testEventPropagationEnabled() {
    ClientProcessDefinition processDefinition = ProcessDefinitionBuilder
    .startProcess("propagate")
      .startActivity("composite")
        .startEvent(Event.END)
          .listener(new DisplaySource(), true)
        .endEvent()
        .startActivity("a", new WaitState())
          .initial()
          .transition("b")
        .endActivity()
        .startActivity("b", new WaitState())
          .transition("c")
        .endActivity()
      .endActivity()
      .startActivity("c", new WaitState())
      .endActivity()
    .endProcess();

    ClientExecution execution = processDefinition.startProcessInstance();
    
    List<String> expectedLines = new ArrayList<String>(); 
    assertEquals(expectedLines, testConsole.lines);
    
    execution.signal();
    
    expectedLines.add("leaving activity(a)");
    assertEquals(expectedLines, testConsole.lines);
    
    execution.signal();

    expectedLines.add("leaving activity(b)");
    expectedLines.add("leaving activity(composite)");
    assertEquals(expectedLines, testConsole.lines);
  }
  
  public void testEventPropagationDefaultDisabled() {
    ClientProcessDefinition processDefinition = ProcessDefinitionBuilder
    .startProcess("propagate")
      .startActivity("composite")
        .startEvent(Event.END)
          .listener(new DisplaySource())
        .endEvent()
        .startActivity("a", new WaitState())
          .initial()
          .transition("b")
        .endActivity()
        .startActivity("b", new WaitState())
          .transition("c")
        .endActivity()
      .endActivity()
      .startActivity("c", new WaitState())
      .endActivity()
    .endProcess();

    ClientExecution execution = processDefinition.startProcessInstance();
    
    List<String> expectedLines = new ArrayList<String>(); 
    assertEquals(expectedLines, testConsole.lines);
    
    execution.signal();
    
    assertEquals(expectedLines, testConsole.lines);
    
    execution.signal();

    expectedLines.add("leaving activity(composite)");
    assertEquals(expectedLines, testConsole.lines);
  }
}
