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

import org.jbpm.api.model.Event;
import org.jbpm.pvm.activities.AutomaticActivity;
import org.jbpm.pvm.activities.PrintLn;
import org.jbpm.pvm.activities.TestConsoleTestCase;
import org.jbpm.pvm.activities.WaitState;
import org.jbpm.pvm.internal.builder.ProcessDefinitionBuilder;
import org.jbpm.pvm.internal.client.ClientExecution;
import org.jbpm.pvm.internal.client.ClientProcessDefinition;

public class EventListenerTest extends TestConsoleTestCase {

  public void testEventListener() {
    ClientProcessDefinition processDefinition = ProcessDefinitionBuilder
      .startProcess()
      .startActivity("a", new AutomaticActivity())
        .initial()
        .startEvent(Event.END)
          .listener(new PrintLn("leaving a"))
          .listener(new PrintLn("second message while leaving a"))
        .endEvent()
        .startFlow("b")
          .listener(new PrintLn("taking transition"))
        .endFlow()
      .endActivity()
      .startActivity("b", new WaitState())
        .startEvent(Event.START)
          .listener(new PrintLn("entering b"))
        .endEvent()
      .endActivity()
    .endProcess();

    ClientExecution execution = processDefinition.startProcessInstance();
    
    assertEquals("leaving a", testConsole.getLine(0));
    assertEquals("second message while leaving a", testConsole.getLine(1));
    assertEquals("taking transition", testConsole.getLine(2));
    assertEquals("entering b", testConsole.getLine(3));
  }
}
