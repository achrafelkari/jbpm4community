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
package org.jbpm.test.task;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.jbpm.api.Execution;

import org.jbpm.api.ProcessInstance;
import org.jbpm.api.task.Task;
import org.jbpm.test.JbpmTestCase;


/**
 * @author Tom Baeyens
 */
public class TaskQueryProcessTest extends JbpmTestCase {

  
  public void testTaskQueryProcessInstanceId() {
    deployJpdlXmlString(
      "<process name='VacationTrip'>" +
      "  <start>" +
      "    <transition to='f' />" +
      "  </start>" +
      "  <fork name='f'>" +
      "    <transition to='select destination' />" +
      "    <transition to='work hard for the money' />" +
      "  </fork>" +
      "  <task name='select destination' " +
      "        assignee='johndoe'>" +
      "    <transition to='wait' />" +
      "  </task>" +
      "  <task name='work hard for the money' " +
      "        assignee='johndoe'>" +
      "    <transition to='wait' />" +
      "  </task>" +
      "  <state name='wait'/>" +
      "</process>"
    );

    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("destination", "Bruges");
    executionService.startProcessInstanceByKey("VacationTrip", variables);

    variables = new HashMap<String, Object>();
    variables.put("destination", "Paris");
    ProcessInstance parisProcessInstance = executionService.startProcessInstanceByKey("VacationTrip", variables);
    
    variables = new HashMap<String, Object>();
    variables.put("destination", "Malaga");
    executionService.startProcessInstanceByKey("VacationTrip", variables);
    

    deployJpdlXmlString(
      "<process name='BusinessTrip'>" +
      "  <start>" +
      "    <transition to='try to get someone else to do it' />" +
      "  </start>" +
      "  <task name='try to get someone else to do it' " +
      "        assignee='johndoe'>" +
      "    <transition to='wait' />" +
      "  </task>" +
      "  <state name='wait'/>" +
      "</process>"
    );

    executionService.startProcessInstanceByKey("BusinessTrip");
    executionService.startProcessInstanceByKey("BusinessTrip");

    
    List<Task> parisTasks = taskService
      .createTaskQuery()
      .processInstanceId(parisProcessInstance.getId())
      .list();
    
    assertEquals("expected 2 elements, but was "+parisTasks.toString(), 2, parisTasks.size());
    
    Set<String> expectedTaskNames = new HashSet<String>();
    expectedTaskNames.add("select destination");
    expectedTaskNames.add("work hard for the money");

    Set<String> taskNames = new HashSet<String>();
    for (Task task: parisTasks) {
      taskNames.add(task.getName());
      assertEquals("Paris", taskService.getVariable(task.getId(), "destination"));
    }
    
    assertEquals(expectedTaskNames, taskNames);
  }

  public void testTaskQueryProcessDefinitionId() {
    deployJpdlXmlString(
      "<process name='VacationTrip'>" +
      "  <start>" +
      "    <transition to='select destination' />" +
      "  </start>" +
      "  <task name='select destination' " +
      "        assignee='johndoe'>" +
      "    <transition to='work hard for the money' />" +
      "  </task>" +
      "  <task name='work hard for the money' " +
      "        assignee='johndoe'>" +
      "    <transition to='wait' />" +
      "  </task>" +
      "  <state name='wait'/>" +
      "</process>"
    );

    // we bring one process instance in task book travel
    executionService.startProcessInstanceByKey("VacationTrip").getId();
    taskService.completeTask(taskService.findPersonalTasks("johndoe").get(0).getId());
    
    // and twto process instances in task select destination
    executionService.startProcessInstanceByKey("VacationTrip");
    executionService.startProcessInstanceByKey("VacationTrip");
    

    deployJpdlXmlString(
      "<process name='BusinessTrip'>" +
      "  <start>" +
      "    <transition to='find customer' />" +
      "  </start>" +
      "  <task name='find customer' " +
      "        assignee='johndoe'>" +
      "    <transition to='wait' />" +
      "  </task>" +
      "  <state name='wait'/>" +
      "</process>"
    );

    executionService.startProcessInstanceByKey("BusinessTrip");
    executionService.startProcessInstanceByKey("BusinessTrip");

    List<Task> tasks = taskService
      .createTaskQuery()
      .processDefinitionId("VacationTrip-1")
      .list();
    
    assertEquals("expected 3 elements, but was "+tasks.toString(), 3, tasks.size());
    
    int selectDestinationTasks = 0;
    int workHardForTheMoneyTasks = 0;

    for (Task task: tasks) {
      if (task.getName().equals("select destination")) {
        selectDestinationTasks++;
      }
      if (task.getName().equals("work hard for the money")) {
        workHardForTheMoneyTasks++;
      }
    }
    
    assertEquals(2, selectDestinationTasks);
    assertEquals(1, workHardForTheMoneyTasks);
  }
  
  public void testTaskQueryActivityName() {
    deployJpdlXmlString(
      "<process name='VacationTrip'>" +
      "  <start>" +
      "    <transition to='select destination' />" +
      "  </start>" +
      "  <task name='select destination' " +
      "        assignee='johndoe'>" +
      "    <transition to='work hard for the money' />" +
      "  </task>" +
      "  <task name='work hard for the money' " +
      "        assignee='johndoe'>" +
      "    <transition to='wait' />" +
      "  </task>" +
      "  <state name='wait'/>" +
      "</process>"
    );

    // we bring one process instance in task book travel
    executionService.startProcessInstanceByKey("VacationTrip").getId();
    taskService.completeTask(taskService.findPersonalTasks("johndoe").get(0).getId());
    
    // and twto process instances in task select destination
    executionService.startProcessInstanceByKey("VacationTrip");
    executionService.startProcessInstanceByKey("VacationTrip");
    

    deployJpdlXmlString(
      "<process name='BusinessTrip'>" +
      "  <start>" +
      "    <transition to='find customer' />" +
      "  </start>" +
      "  <task name='find customer' " +
      "        assignee='johndoe'>" +
      "    <transition to='wait' />" +
      "  </task>" +
      "  <state name='wait'/>" +
      "</process>"
    );

    executionService.startProcessInstanceByKey("BusinessTrip");
    executionService.startProcessInstanceByKey("BusinessTrip");

    List<Task> tasks = taskService
      .createTaskQuery()
      .processDefinitionId("VacationTrip-1")
      .activityName("select destination")
      .list();
    
    assertEquals("expected 2 elements, but was "+tasks.toString(), 2, tasks.size());
    
    int selectDestinationTasks = 0;
    int workHardForTheMoneyTasks = 0;

    for (Task task: tasks) {
      if (task.getName().equals("select destination")) {
        selectDestinationTasks++;
      }
      if (task.getName().equals("work hard for the money")) {
        workHardForTheMoneyTasks++;
      }
    }
    
    assertEquals(2, selectDestinationTasks);
    assertEquals(0, workHardForTheMoneyTasks);
  }
  

      public void testTaskQueryExecutionId() {
        deployJpdlXmlString(
                "<process name='VacationTrip'>"
                + "  <start>"
                + "    <transition to='f' />"
                + "  </start>"
                + "  <fork name='f'>"
                + "    <transition to='select destination' />"
                + "    <transition to='work hard for the money' />"
                + "  </fork>"
                + "  <task name='select destination' "
                + "        assignee='johndoe'>"
                + "    <transition to='join' />"
                + "  </task>"
                + "  <task name='work hard for the money' "
                + "        assignee='johndoe'>"
                + "    <transition to='join' />"
                + "  </task>"
                + "  <join name='join'>"
                + "    <transition to='end' />"
                + "  </join>"
                + "  <end name='end'/>"
                + "</process>");
        // start the process
        ProcessInstance process = executionService.startProcessInstanceByKey("VacationTrip");

        // we should have two tasks and two executions here:
        Collection<? extends Execution> executions = process.getExecutions();
        assertEquals(2, executions.size());

        // check if there is one task in each execution:
        for (Execution execution : executions) {
            Task task = taskService.createTaskQuery().executionId(execution.getId()).uniqueResult();
            assertNotNull(task);
            assertEquals(execution.getId(), task.getExecutionId());
}

        // complete the tasks
        process = executionService.findProcessInstanceById(process.getId());
        executions = process.getExecutions();
        for (Execution execution : executions) {
            assertFalse(execution.isEnded());
            Task task = taskService.createTaskQuery().executionId(execution.getId()).uniqueResult();
            taskService.completeTask(task.getId());
        }

        // process should be ended now
        process = executionService.findProcessInstanceById(process.getId());
        assertNull(process);
    }
}
