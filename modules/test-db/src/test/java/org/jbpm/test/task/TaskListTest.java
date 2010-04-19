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

import java.util.List;

import org.jbpm.api.task.Task;
import org.jbpm.test.JbpmTestCase;


/**
 * @author Tom Baeyens
 */
public class TaskListTest extends JbpmTestCase {

  public void testPersonalTaskList() {
    Task task = taskService.newTask();
    task.setName("do laundry");
    task.setAssignee("johndoe");
    String laundryTaskId = taskService.saveTask(task);

    task = taskService.newTask();
    task.setName("get good idea");
    task.setAssignee("joesmoe");
    String ideaTaskId = taskService.saveTask(task);

    task = taskService.newTask();
    task.setName("find venture capital");
    task.setAssignee("joesmoe");
    String capitalTaskId = taskService.saveTask(task);
    
    task = taskService.newTask();
    task.setName("start new business");
    task.setAssignee("joesmoe");
    String startBusinessTaskId = taskService.saveTask(task);

    List<Task> taskList = taskService.findPersonalTasks("johndoe");
    assertNotNull(taskList);
    
    assertEquals("do laundry", taskList.get(0).getName());
    assertEquals(1, taskList.size());

    taskList = taskService.findPersonalTasks("joesmoe");
    assertNotNull(taskList);
    
    assertContainsTask(taskList, "get good idea");
    assertContainsTask(taskList, "start new business");
    assertContainsTask(taskList, "find venture capital");

    assertEquals(3, taskList.size());
    
    taskService.deleteTaskCascade(startBusinessTaskId);
    taskService.deleteTaskCascade(capitalTaskId);
    taskService.deleteTaskCascade(ideaTaskId);
    taskService.deleteTaskCascade(laundryTaskId);
  }

  public void testPersonalTaskListDefaultSortOrder() {
    Task task = taskService.newTask();
    task.setName("get good idea");
    task.setAssignee("joesmoe");
    task.setPriority(3);
    String ideaTaskId = taskService.saveTask(task);

    task = taskService.newTask();
    task.setName("find venture capital");
    task.setAssignee("joesmoe");
    task.setPriority(2);
    String capitalTaskId = taskService.saveTask(task);
    
    task = taskService.newTask();
    task.setName("start new business");
    task.setAssignee("joesmoe");
    task.setPriority(1);
    String startBusinessTaskId = taskService.saveTask(task);

    task = taskService.newTask();
    task.setName("take a day off");
    task.setAssignee("joesmoe");
    task.setPriority(-5);
    String dayOffTaskId = taskService.saveTask(task);

    task = taskService.newTask();
    task.setName("make profit");
    task.setAssignee("joesmoe");
    task.setPriority(10);
    String profitTaskId = taskService.saveTask(task);

    List<Task> taskList = taskService.findPersonalTasks("joesmoe");
    assertNotNull(taskList);
    
    // default sort order is based on the priority
    assertEquals("make profit",          taskList.get(0).getName());
    assertEquals("get good idea",        taskList.get(1).getName());
    assertEquals("find venture capital", taskList.get(2).getName());
    assertEquals("start new business",   taskList.get(3).getName());
    assertEquals("take a day off",       taskList.get(4).getName());

    assertEquals(5, taskList.size());

    taskService.deleteTaskCascade(profitTaskId);
    taskService.deleteTaskCascade(dayOffTaskId);
    taskService.deleteTaskCascade(startBusinessTaskId);
    taskService.deleteTaskCascade(capitalTaskId);
    taskService.deleteTaskCascade(ideaTaskId);
  }
}
