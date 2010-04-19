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
package org.jbpm.examples.bpmn.usertask;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jbpm.api.NewDeployment;
import org.jbpm.api.ProcessInstance;
import org.jbpm.api.task.Task;
import org.jbpm.test.JbpmTestCase;


/**
 * @author Joram Barrez
 */
public class UserTaskTest extends JbpmTestCase {
  
  private static String[] PROCESS_LOCATIONS = 
    {"org/jbpm/examples/bpmn/task/usertask/user_task_potential_owner_user.bpmn.xml",
     "org/jbpm/examples/bpmn/task/usertask/user_task_potential_owner_group.bpmn.xml",
     "org/jbpm/examples/bpmn/task/usertask/user_task_human_performer_user.bpmn.xml",
     "org/jbpm/examples/bpmn/task/usertask/user_task_human_performer_variable.bpmn.xml"};
  
  private static final String PETER = "peter";
  private static final String MARY = "mary";
  
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    
    for (String processLocation: PROCESS_LOCATIONS) {
      NewDeployment deployment = repositoryService.createDeployment();
      deployment.addResourceFromClasspath(processLocation);
      registerDeployment(deployment.deploy());      
    }
    
    identityService.createGroup("management");
    
    identityService.createUser(PETER, "Peter", "Pan");
    identityService.createMembership(PETER, "management");
    
    identityService.createUser(MARY, "Mary", "Littlelamb");
    identityService.createMembership(MARY, "management");
  }
  
  @Override
  protected void tearDown() throws Exception {
    identityService.deleteGroup("management");
    identityService.deleteUser(PETER);
    identityService.deleteUser(MARY);
    super.tearDown();
  }
  
  public void testPotentialOwnerUser() {
    ProcessInstance processInstance = executionService.startProcessInstanceByKey("userTaskPotentialOwnerUser");
    Task task = taskService.createTaskQuery().candidate(PETER).uniqueResult();
    assertEquals("My User task", task.getName());
    taskService.completeTask(task.getId());
    assertProcessInstanceEnded(processInstance);
  }
  
  public void testPotentialOwnerGroup() {
    ProcessInstance processInstance = executionService.startProcessInstanceByKey("userTaskPotentialOwnerGroup");
    
    // Peter and Mary are both part of management, so they both should see the task
    List<Task> tasks = taskService.findGroupTasks(PETER);
    assertEquals(1, tasks.size());
    tasks = taskService.findGroupTasks(MARY);
    assertEquals(1, tasks.size());
    
    // Mary claims the task
    Task task = tasks.get(0);
    taskService.takeTask(task.getId(), MARY);
    assertNull(taskService.createTaskQuery().candidate(PETER).uniqueResult());
    
    taskService.completeTask(task.getId());
    assertProcessInstanceEnded(processInstance);
  }
  
  public void testHumanPerformerUser() {
    ProcessInstance processInstance = executionService.startProcessInstanceByKey("userTaskHumanPerformerUser");
    Task task = taskService.findPersonalTasks(MARY).get(0);    
    assertNotNull(task);
    taskService.completeTask(task.getId());
    assertProcessInstanceEnded(processInstance);
  }

  public void testHumanPerformerVariableExpression() {
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("user", PETER);
    ProcessInstance processInstance = executionService.startProcessInstanceByKey("userTaskHumanPerformerVariable", variables);
    Task task = taskService.createTaskQuery().assignee(PETER).uniqueResult();
    assertNotNull(task);
    assertNull(taskService.createTaskQuery().assignee(MARY).uniqueResult());
    taskService.completeTask(task.getId());
    assertProcessInstanceEnded(processInstance);
  }
  

}
