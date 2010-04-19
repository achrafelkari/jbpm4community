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
package org.jbpm.test.activity.task;

import java.util.List;

import org.jbpm.api.task.Task;
import org.jbpm.test.JbpmTestCase;


/**
 * @author Tom Baeyens
 */
public class TaskSwimlaneTest extends JbpmTestCase {
  
  public void testSwimlaneTaskEnd() {
    deployJpdlXmlString(
      "<process name='TaskSwimlane'> " +
      "  <swimlane name='sales representative' assignee='johndoe' />" +
      "  <start>" +
      "    <transition to='enter order data' />" +
      "  </start>" +
      "  <task name='enter order data'" +
      "        swimlane='sales representative'>" +
      "    <transition to='end'/>" +
      "  </task>" +
      "  <end name='end' />" +
      "</process>"
    );
    
    executionService.startProcessInstanceByKey("TaskSwimlane");

    List<Task> tasks = taskService.findPersonalTasks("johndoe");
    assertEquals(1, tasks.size());
    Task task = tasks.get(0);
    taskService.completeTask(task.getId());
    
    assertEquals(0, executionService.createProcessInstanceQuery().count());
    assertEquals(0, taskService.createTaskQuery().count());
  }

  public void testConcurrentSwimlaneTaskEnd() {
    deployJpdlXmlString(
      "<process name='TaskConcurrentSwimlane'> " +
      "  <swimlane name='sales representative' assignee='johndoe' />" +
      "  <start>" +
      "    <transition to='fork' />" +
      "  </start>" +
      "  <fork name='fork'>" +
      "    <transition to='enter order data' />" +
      "    <transition to='sign contract' />" +
      "  </fork>" +
      "  <task name='enter order data'" +
      "        swimlane='sales representative'>" +
      "    <transition to='join'/>" +
      "  </task>" +
      "  <task name='sign contract'" +
      "        swimlane='sales representative'>" +
      "    <transition to='join'/>" +
      "  </task>" +
      "  <join name='join'>" +
      "    <transition to='end' />" +
      "  </join>" +
      "  <end name='end' />" +
      "</process>"
    );
    
    executionService.startProcessInstanceByKey("TaskConcurrentSwimlane");

    List<Task> tasks = taskService.findPersonalTasks("johndoe");
    assertEquals(2, tasks.size());
    taskService.completeTask(tasks.get(0).getId());
    taskService.completeTask(tasks.get(1).getId());
    
    assertEquals(0, executionService.createProcessInstanceQuery().count());
    assertEquals(0, taskService.createTaskQuery().count());
  }
}
