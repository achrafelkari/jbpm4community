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
package org.jbpm.test.query;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.jbpm.api.TaskQuery;
import org.jbpm.api.task.Task;
import org.jbpm.test.JbpmTestCase;
import org.jbpm.test.assertion.QueryAssertions;


/**
 * @author Tom Baeyens
 */
public class TaskQueryTest extends JbpmTestCase {
	
  public void testPropertyNames() {
	Task task = taskService.newTask();
	task.setName("write task query test");
	task.setAssignee("koen");
	task.setPriority(Integer.MAX_VALUE);
	task.setDescription("make sure the test fails if property names are incorrect");
	task.setDuedate(new Date());
	String taskId = taskService.saveTask(task);
	
    checkProperty(taskService.createTaskQuery(), task, TaskQuery.PROPERTY_ASSIGNEE);
    checkProperty(taskService.createTaskQuery(), task, TaskQuery.PROPERTY_CREATEDATE);
    checkProperty(taskService.createTaskQuery(), task, TaskQuery.PROPERTY_DUEDATE);
    checkProperty(taskService.createTaskQuery(), task, TaskQuery.PROPERTY_NAME);
    checkProperty(taskService.createTaskQuery(), task, TaskQuery.PROPERTY_PRIORITY);
    checkProperty(taskService.createTaskQuery(), task, TaskQuery.PROPERTY_PROGRESS);
    
	taskService.deleteTaskCascade(taskId);
  }
  
  private void checkProperty(TaskQuery taskQuery, Task task, String propertyName) {
	List<Task> taskList = taskQuery.orderAsc(propertyName).list();
	assertNotNull(taskList);
	assertContainsTask(taskList, task.getName());
	assertEquals(1, taskList.size());
  }

  public void testSimplestTaskQuery() {
    List<Task> tasks = createTestTasks();

    List<Task> taskList = taskService
      .createTaskQuery()
      .list();
    assertNotNull(taskList);
    
    assertContainsTask(taskList, "do laundry");
    assertContainsTask(taskList, "change dyper");
    assertContainsTask(taskList, "start new business");
    assertContainsTask(taskList, "find venture capital");

    assertEquals(4, taskList.size());
    
    deleteTasks(tasks);
  }

  public void testOrderByName() {
    testOrderBy(TaskQuery.PROPERTY_NAME, 
            new String[] {"change dyper", "do laundry", "find venture capital", "start new business"});
  }
  
  public void testOrderByAssignee() {
    testOrderBy(TaskQuery.PROPERTY_ASSIGNEE, new String[] {"Alex", "Joram", "Koen", "Tom"});
  }
  
  public void testOrderByCreateTime() {
    testOrderByResultsInNaturalOrdening(TaskQuery.PROPERTY_CREATEDATE, 4);
  }
  
  public void testOrderByDueDate() {
    testOrderByResultsInNaturalOrdening(TaskQuery.PROPERTY_DUEDATE, 4);
  }
  
  public void testOrderByPriority() {
    testOrderByResultsInNaturalOrdening(TaskQuery.PROPERTY_PRIORITY, 4);
  }
  
  public void testOrderByProgress() {
    testOrderBy(TaskQuery.PROPERTY_PROGRESS, new Integer[] {2, 15, 75, 99});
  }
  
  public void testCount() {
    List<Task> tasks = new ArrayList<Task>();
    
    final String assignee = "task1";
    for (int i= 0; i < 30; i++) {
      Task task = taskService.newTask();
      
      // assign half of the tasks
      if ( i % 2 == 0) {
        task.setAssignee(assignee);
      }
      
      taskService.saveTask(task);
      tasks.add(task);
    }
    
    TaskQuery taskQuery = taskService.createTaskQuery();
    assertEquals(30, taskQuery.count());
    assertEquals(15, taskQuery.assignee(assignee).count());
    
    deleteTasks(tasks);
  }
  
  
  /* -------------------------------------------------------------------
   * HELPER METHODS
   * ------------------------------------------------------------------- */
  
  private List<Task> createTestTasks() {
    List<Task> result = new ArrayList<Task>();

    Task task1 = taskService.newTask();
    task1.setName("do laundry");
    task1.setAssignee("Tom");
    task1.setPriority(3);
    task1.setDuedate(stringToDate("10/10/1985"));
    task1.setProgress(15);

    Task task2 = taskService.newTask();
    task2.setName("change dyper");
    task2.setAssignee("Koen");
    task2.setPriority(1);
    task2.setDuedate(stringToDate("28/06/1989"));
    task2.setProgress(2);
    
    Task task3 = taskService.newTask();
    task3.setName("start new business");
    task3.setAssignee("Joram");
    task3.setPriority(4);
    task3.setProgress(75);

    Task task4 = taskService.newTask();
    task4.setName("find venture capital");
    task4.setAssignee("Alex");
    task4.setPriority(7);
    task4.setDuedate(stringToDate("09/09/2009"));
    task4.setProgress(99);
    
    for (Task t : new Task[] {task1, task2, task3, task4}) {
      taskService.saveTask(t);
      result.add(t);
    }
    
    return result;
  }
  
  private void testOrderBy(String property, Object[] expectedValues) {
    List<Task> tasks = createTestTasks();
    
    List<Task> taskListAsc = taskService.createTaskQuery().orderAsc(property).list();
    List<Task> taskListDesc = taskService.createTaskQuery().orderDesc(property).list();

    QueryAssertions.assertOrderOnProperty(Task.class, property, taskListAsc, taskListDesc, Arrays.asList(expectedValues));

    deleteTasks(tasks);
  }

  private void testOrderByResultsInNaturalOrdening(String property, int expectedNrOfResults) {
    List<Task> tasks = createTestTasks();
    List<Task> taskListAsc = taskService.createTaskQuery().orderAsc(property).list();
    List<Task> taskListDesc = taskService.createTaskQuery().orderDesc(property).list();
    
    QueryAssertions.assertOrderIsNatural(Task.class, property, taskListAsc, taskListDesc, expectedNrOfResults);
    
    deleteTasks(tasks);
  }
  
  private void deleteTasks(List<Task> tasks) {
    for (Task t : tasks) {
      taskService.deleteTaskCascade(t.getId());
    }
  }
  
  private Date stringToDate(String dateString) {
    DateFormat dateFormat = new SimpleDateFormat("dd/mm/yyyy");
    try {
      return dateFormat.parse(dateString);
    } catch (ParseException e) {
      throw new RuntimeException("Couldn't convert " + dateString);
    }
  }
  
}
