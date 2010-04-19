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
package org.jbpm.pvm.executionmode.object;

import junit.framework.TestCase;

import org.jbpm.pvm.activities.AutomaticActivity;
import org.jbpm.pvm.activities.WaitState;
import org.jbpm.pvm.internal.builder.ProcessDefinitionBuilder;
import org.jbpm.pvm.internal.client.ClientExecution;
import org.jbpm.pvm.internal.client.ClientProcessDefinition;
import org.jbpm.pvm.internal.client.ClientProcessInstance;

public class ObjectExecutionModeTest extends TestCase {

  
  public void testObjectExecutionMode(){
    ClientProcessDefinition processDefinition = ProcessDefinitionBuilder
    .startProcess("loan")
      .startActivity("submit loan request", new AutomaticActivity())
        .initial()
        .transition("evaluate")
      .endActivity()
      .startActivity("evaluate", new WaitState())
        .transition("wire money", "approve")
        .transition("end", "reject")
      .endActivity()
      .startActivity("wire money", new AutomaticActivity())
        .transition("archive")
      .endActivity()
      .startActivity("archive", new WaitState())
        .transition("end")
      .endActivity()
      .startActivity("end", new WaitState())
      .endActivity()
    .endProcess();
    
    ClientProcessInstance processInstance = processDefinition.startProcessInstance();
    
    assertTrue(processInstance.isActive("evaluate"));
    
    processInstance.signal("approve");
    
    processInstance.signal();
    assertTrue(processInstance.isActive("end"));
  }
}
