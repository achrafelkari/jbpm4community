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

import java.util.Date;
import java.text.DateFormat;

import org.jbpm.api.task.Task;
import org.jbpm.test.JbpmTestCase;

/**
 * @author Alejandro Guizar
 */
public class TaskCreateUpdateDeleteTest extends JbpmTestCase {

  public void testNewTask() {
    // creation of a new non-persisted task
    Task task = taskService.newTask();
    task = taskService.getTask(task.getId());
    assertNull(task);
  }

  public void testSaveTask() {
    Task task = taskService.newTask();
    String taskId = taskService.saveTask(task);
    // task was made persistent
    task = taskService.getTask(taskId); 
    assertNotNull("expected non-null task", task);
    // make some change
    Date dueDate = new Date();
    task.setDuedate(dueDate);
    taskService.saveTask(task);
    // verify change is applied
    task = taskService.getTask(taskId);
    
    // task.getDueDate() return an java.sql.Timestamp
    DateFormat df = DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.LONG);
    assertEquals(df.format(dueDate), df.format(task.getDuedate()));
    
    taskService.deleteTaskCascade(taskId);
  }

  public void testDeleteTask() {
    Task task = taskService.newTask();
    String taskId = taskService.saveTask(task);
    
    // task was made persistent
    assertNotNull("expected non-null task", taskService.getTask(taskId));
    // delete task and verify it does not exist
    taskService.deleteTaskCascade(taskId);
    task = taskService.getTask(taskId);
    assertNull("expected null, but was " + task, task);
  }
}
