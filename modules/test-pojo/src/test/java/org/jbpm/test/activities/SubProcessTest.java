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
import org.jbpm.pvm.internal.builder.ProcessDefinitionBuilder;
import org.jbpm.pvm.internal.client.ClientExecution;
import org.jbpm.pvm.internal.client.ClientProcessDefinition;
import org.jbpm.pvm.internal.client.ClientProcessInstance;
import org.jbpm.pvm.internal.model.ExecutionImpl;
import org.jbpm.test.BaseJbpmTestCase;

/**
 * @author Tom Baeyens
 */
public class SubProcessTest extends BaseJbpmTestCase
{

  public static class SubProcess implements ExternalActivityBehaviour {
    private static final long serialVersionUID = 1L;
    ClientProcessDefinition subProcess;
    public SubProcess(ClientProcessDefinition subProcess) {
      this.subProcess = subProcess;
    }
    public void execute(ActivityExecution execution) throws Exception {
      ExecutionImpl executionImpl = (ExecutionImpl) execution;
      ClientExecution subProcessInstance = executionImpl.startSubProcessInstance(subProcess);
      if (!subProcessInstance.isEnded()) {
        execution.waitForSignal();
      }
    }
    public void signal(ActivityExecution execution, String signalName, Map<String, ?> parameters) throws Exception {
    }
  }
  
  public static class AutomaticActivity implements ActivityBehaviour {
    private static final long serialVersionUID = 1L;
    public void execute(ActivityExecution execution) {
    }
  }
  
  public static class WaitState implements ExternalActivityBehaviour {
    private static final long serialVersionUID = 1L;
    public void execute(ActivityExecution execution) {
      execution.waitForSignal();
    }
    public void signal(ActivityExecution execution, String signalName, Map<String, ?> parameters){
    }
  }

  public static class EndState implements ActivityBehaviour {
    private static final long serialVersionUID = 1L;
    public void execute(ActivityExecution execution) throws Exception {
      execution.end();
    }
  }

  public void testSubProcess() {
    ClientProcessDefinition subProcess = ProcessDefinitionBuilder
    .startProcess("sub")
      .startActivity("sub1", new AutomaticActivity())
        .initial()
        .transition("sub2")
      .endActivity()
      .startActivity("sub2", new WaitState())
        .transition("sub3")
      .endActivity()
      .startActivity("sub3", new EndState())
      .endActivity()
    .endProcess();
    
    ClientProcessDefinition superProcess = ProcessDefinitionBuilder
    .startProcess("super")
      .startActivity("super1", new AutomaticActivity())
        .initial()
        .transition("super2")
      .endActivity()
      .startActivity("super2", new SubProcess(subProcess))
        .transition("super3")
      .endActivity()
      .startActivity("super3", new WaitState())
      .endActivity()
    .endProcess();
    
    ClientProcessInstance superProcessInstance = superProcess.startProcessInstance();
    assertTrue(superProcessInstance.isActive("super2"));

    ClientProcessInstance subProcessInstance = (ClientProcessInstance) superProcessInstance.getSubProcessInstance(); 
    assertNotNull(subProcessInstance);
    assertTrue(subProcessInstance.isActive("sub2"));
    
    subProcessInstance.signal();
    
    assertTrue(subProcessInstance.isEnded());
    
    assertTrue(superProcessInstance.isActive("super3"));
  }
}
