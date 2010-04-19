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

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jbpm.api.task.Task;
import org.jbpm.test.JbpmTestCase;


/**
 * @author Tom Baeyens
 */
public class SubTaskTest extends JbpmTestCase {

  public void testSubTasks() {
    
    // create top level task
    // * clean da house
    
    Task task = taskService.newTask();
    task.setName("clean da house");
    String taskId = taskService.saveTask(task);
    
    // create 3 sub tasks: 
    // * clean da house
    //   * dishes
    //   * laundry
    //   * sweep floor
    
    Task subTask = taskService.newTask(taskId);
    subTask.setName("dishes");
    String dishesTaskId = taskService.saveTask(subTask);
    
    subTask = taskService.newTask(taskId);
    subTask.setName("laundry");
    String laundryTaskId = taskService.saveTask(subTask);
    
    subTask = taskService.newTask(taskId);
    subTask.setName("sweep floor");
    String sweepFloorTaskId = taskService.saveTask(subTask);
    
    // verify 3 sub tasks of clean da house
    
    List<Task> subTasks = taskService.getSubTasks(taskId);
    Set<String> subTaskNames = getTaskNames(subTasks);
    
    Set<String> expectedTaskNames = new HashSet<String>();
    expectedTaskNames.add("dishes");
    expectedTaskNames.add("laundry");
    expectedTaskNames.add("sweep floor");
    
    assertEquals(expectedTaskNames, subTaskNames);

    // add 3 sub tasks for sweep floor: 
    // * clean da house
    //   * dishes
    //   * laundry
    //   * sweep floor
    //     * find broom
    //     * find water
    //     * scrub

    // now second level subtasks under sweep floor
    subTask = taskService.newTask(sweepFloorTaskId);
    subTask.setName("find broom");
    taskService.saveTask(subTask);

    subTask = taskService.newTask(sweepFloorTaskId);
    subTask.setName("find water");
    taskService.saveTask(subTask);

    subTask = taskService.newTask(sweepFloorTaskId);
    subTask.setName("scrub");
    taskService.saveTask(subTask);

    // verify all the sub tasks of 'clean da house' and 'sweep floor'
    
    subTaskNames = getTaskNames(taskService.getSubTasks(taskId));
    
    expectedTaskNames = new HashSet<String>();
    expectedTaskNames.add("dishes");
    expectedTaskNames.add("laundry");
    expectedTaskNames.add("sweep floor");
    
    assertEquals(expectedTaskNames, subTaskNames);
    
    subTaskNames = getTaskNames(taskService.getSubTasks(sweepFloorTaskId));
    
    expectedTaskNames = new HashSet<String>();
    expectedTaskNames.add("find broom");
    expectedTaskNames.add("find water");
    expectedTaskNames.add("scrub");
    
    assertEquals(expectedTaskNames, subTaskNames);
    
    // delete task dishes
    taskService.deleteTaskCascade(dishesTaskId);

    // verify all the sub tasks of 'clean da house' and 'sweep floor'
    
    subTaskNames = getTaskNames(taskService.getSubTasks(taskId));
    
    expectedTaskNames = new HashSet<String>();
    expectedTaskNames.add("laundry");
    expectedTaskNames.add("sweep floor");
    
    assertEquals(expectedTaskNames, subTaskNames);
    
    subTaskNames = getTaskNames(taskService.getSubTasks(sweepFloorTaskId));
    
    expectedTaskNames = new HashSet<String>();
    expectedTaskNames.add("find broom");
    expectedTaskNames.add("find water");
    expectedTaskNames.add("scrub");
    
    assertEquals(expectedTaskNames, subTaskNames);

    // delete laundry and delete sweep floor
    // NOTE: deleting sweep floor should recursively delete the subtasks
    taskService.deleteTaskCascade(laundryTaskId);
    taskService.deleteTaskCascade(sweepFloorTaskId);

    subTaskNames = getTaskNames(taskService.getSubTasks(taskId));

    expectedTaskNames = new HashSet<String>();
    assertEquals(expectedTaskNames, subTaskNames);
    
    subTaskNames = getTaskNames(taskService.getSubTasks(sweepFloorTaskId));
    
    expectedTaskNames = new HashSet<String>();
    assertEquals(expectedTaskNames, subTaskNames);
    
    taskService.deleteTaskCascade(taskId);
  }

  private Set<String> getTaskNames(List<Task> tasks) {
    Set<String> taskNames = new HashSet<String>();
    for (Task task: tasks) {
      taskNames.add(task.getName());
    }
    return taskNames;
  }
}
