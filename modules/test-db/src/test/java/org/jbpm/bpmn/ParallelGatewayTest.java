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
package org.jbpm.bpmn;

import java.util.Arrays;
import java.util.List;

import org.jbpm.api.ProcessInstance;
import org.jbpm.api.TaskQuery;
import org.jbpm.api.task.Task;
import org.jbpm.bpmn.parser.BpmnParser;
import org.jbpm.pvm.internal.xml.Problem;
import org.jbpm.test.JbpmTestCase;
import org.jbpm.test.assertion.CollectionAssertions;

/**
 * @author Ronald van Kuijk (kukeltje)
 */
public class ParallelGatewayTest extends JbpmTestCase {

  static BpmnParser bpmnParser = new BpmnParser();

  public List<Problem> parse(String resource) {

    List<Problem> problems = bpmnParser.createParse().setResource(resource).execute().getProblems();

    return problems;
  }

  public void testNormalParse() {

    List<Problem> problems = parse("org/jbpm/bpmn/parallelGateway.bpmn.xml");

    if (!problems.isEmpty()) {
      fail("No problems should have occured. Problems: " + problems);
    }
  }

  public void testNormalExecute() {

    String deploymentId = repositoryService.createDeployment().addResourceFromClasspath("org/jbpm/bpmn/parallelGateway.bpmn.xml").deploy();

    try {
        ProcessInstance pi = executionService.startProcessInstanceByKey("ParallelGateway");
        
        String pid = pi.getId();
                        
        TaskQuery taskQuery = taskService.createTaskQuery();
        List<Task> allTasks = taskQuery.list();

        assertEquals(2, allTasks.size());
        CollectionAssertions.assertContainsSameElements(Arrays.asList("UserTaskLeg1", "UserTaskLeg2"), 
                Arrays.asList(allTasks.get(0).getActivityName(), allTasks.get(1).getActivityName()));
        
        // specifying a transition is unnecessary, BPMN has outgoing AND semantic!
        // TODO: fix
        // Currently not passing any 'outcome'
        taskService.completeTask( allTasks.get(0).getId());
        
        pi = executionService.findProcessInstanceById(pid);
        // process instance should not be ended yet
        assertNotNull(pi);
        
        taskService.completeTask( allTasks.get(1).getId());

        assertEquals(0, taskQuery.list().size());
                
        pi = executionService.findProcessInstanceById(pid);
        // process instance is ended
        assertNull(pi);
        
    }
    finally {
        repositoryService.deleteDeploymentCascade(deploymentId);
    }
  }
  
   

  public void testInvalid() {
    List<Problem> problems = parse("org/jbpm/bpmn/parallelGatewayInvalid.bpmn.xml");

    if ((problems == null) || (problems.isEmpty())) {
      fail("expected problems during parse");
    } else {
      assertTextPresent("parallelGateway 'The Fork' has the wrong number of incomming (1) and outgoing (2) transitions for gatewayDirection='converging'", problems.get(0).getMsg());
    }
  }
}
