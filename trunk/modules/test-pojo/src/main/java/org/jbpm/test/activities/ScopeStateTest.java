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
import org.jbpm.api.activity.ActivityBehaviour;
import org.jbpm.api.activity.ActivityExecution;
import org.jbpm.api.activity.ExternalActivityBehaviour;
import org.jbpm.pvm.internal.builder.ProcessDefinitionBuilder;
import org.jbpm.pvm.internal.client.ClientProcessDefinition;
import org.jbpm.pvm.internal.client.ClientProcessInstance;
import org.jbpm.test.BaseJbpmTestCase;

/**
 * @author Tom Baeyens
 */
public class ScopeStateTest extends BaseJbpmTestCase
{

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
    public void signal(ActivityExecution execution, String signalName, Map<String, ?> parameters) throws Exception {
      assertEquals(Execution.STATE_ACTIVE_ROOT, execution.getState());
      execution.take(signalName);
    }
  }

  public void testInactivationWhenCreatingNestedExecution() {
    ClientProcessDefinition processDefinition = ProcessDefinitionBuilder
    .startProcess()
      .startActivity("a", new AutomaticActivity())
        .initial()
        .transition("b")
      .endActivity()
      .startActivity("b", new WaitState())
        .transition("c")
        .variable("makesSureThisBecomesALocalScope")
      .endActivity()
      .startActivity("c", new WaitState())
      .endActivity()
    .endProcess();
  
    ClientProcessInstance processInstance = processDefinition.createProcessInstance();
    
    assertEquals(Execution.STATE_CREATED, processInstance.getState());
    
    processInstance.start();
    Execution scopeExecution = processInstance.getExecutions().iterator().next();

    assertEquals(Execution.STATE_INACTIVE_SCOPE, processInstance.getState());
    assertEquals(Execution.STATE_ACTIVE_ROOT, scopeExecution.getState());
    
    processInstance.signal(scopeExecution);

    assertEquals(Execution.STATE_ENDED, scopeExecution.getState());
    assertEquals(Execution.STATE_ACTIVE_ROOT, processInstance.getState());
  }
}
