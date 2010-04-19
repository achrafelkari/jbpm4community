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

import junit.framework.TestCase;

import org.jbpm.api.activity.ActivityExecution;
import org.jbpm.api.activity.ExternalActivityBehaviour;
import org.jbpm.pvm.internal.builder.ProcessDefinitionBuilder;
import org.jbpm.pvm.internal.client.ClientExecution;
import org.jbpm.pvm.internal.client.ClientProcessDefinition;
import org.jbpm.pvm.internal.client.ClientProcessInstance;


/**
 * @author Tom Baeyens
 */
public class ExternalDecisionTest extends TestCase {
  
  public static class ExternalDecision implements ExternalActivityBehaviour {
    private static final long serialVersionUID = 1L;
    public void execute(ActivityExecution execution) throws Exception {
      execution.waitForSignal();
    }
    public void signal(ActivityExecution execution, String signalName, Map<String, ?> parameters) throws Exception {
      execution.take(signalName);
    }
  }
  
  public static class WaitState implements ExternalActivityBehaviour {
    private static final long serialVersionUID = 1L;
    public void execute(ActivityExecution execution) {
      execution.waitForSignal();
    }
    public void signal(ActivityExecution execution, String signalName, Map<String, ?> parameters) {
      execution.take(signalName);
    }
  }

  public static ClientProcessDefinition createCreditProcess() {
    return ProcessDefinitionBuilder
    .startProcess()
      .startActivity("creditRate?", new ExternalDecision())
        .initial()
        .transition("priority delivery", "good")
        .transition("bulk delivery", "average")
        .transition("payment upfront", "bad")
      .endActivity()
      .startActivity("priority delivery", new WaitState())
      .endActivity()
      .startActivity("bulk delivery", new WaitState())
      .endActivity()
      .startActivity("payment upfront", new WaitState())
      .endActivity()
    .endProcess();
  }

  public void testGoodRating() {
    ClientProcessDefinition processDefinition = createCreditProcess(); 
    ClientProcessInstance processInstance = processDefinition.startProcessInstance();

    processInstance.signal("good");

    assertTrue(processInstance.isActive("priority delivery"));
  }

  public void testAverageRating() {
    ClientProcessDefinition processDefinition = createCreditProcess(); 
    ClientProcessInstance processInstance = processDefinition.startProcessInstance();

    processInstance.signal("average");
    
    assertTrue(processInstance.isActive("bulk delivery"));
  }
  
  public void testBadRating() {
    ClientProcessDefinition processDefinition = createCreditProcess(); 
    ClientProcessInstance processInstance = processDefinition.startProcessInstance();

    processInstance.signal("bad");

    assertTrue(processInstance.isActive("payment upfront"));
  }
}
