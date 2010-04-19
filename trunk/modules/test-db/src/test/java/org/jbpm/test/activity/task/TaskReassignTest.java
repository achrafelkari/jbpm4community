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

import org.jbpm.api.ProcessInstance;
import org.jbpm.api.history.HistoryTask;
import org.jbpm.api.task.Task;
import org.jbpm.test.JbpmTestCase;

/**
 * @author Tom Baeyens
 */
public class TaskReassignTest extends JbpmTestCase {

  public void testReassignHistory() {
    deployJpdlXmlString(
      "<process name='ReassignTest'>" +
      "  <start>" +
      "    <transition to='write email'/>" +
      "  </start>" +
      "  <task name='write email' assignee='shekharv'>" +
      "    <transition to='end'/>" +
      "  </task>" +
      "  <end name='end'/>" +
      "</process>"
    );
    
    ProcessInstance pi = executionService.startProcessInstanceByKey("ReassignTest");

    List<Task> tasks = taskService.findPersonalTasks("shekharv");
    assertTrue(tasks.size() == 1);

    String taskId = tasks.get(0).getId();

    taskService.assignTask(taskId, "johndoe");
    
    List<Task> tasksAfterReassignment = taskService.findPersonalTasks("shekharv");
    assertTrue("shekharv should not have any tasks.", tasksAfterReassignment.size() == 0);

    List<Task> tasksForNewAssignee = taskService.findPersonalTasks("johndoe");
    assertTrue("johndoe should have 1 task.", tasksForNewAssignee.size() == 1);

    assertEquals("johndoe", taskService.getTask(taskId).getAssignee());

    taskService.completeTask(taskId);
    
    HistoryTask historyTask = historyService
      .createHistoryTaskQuery()
      .taskId(taskId)
      .uniqueResult();

    assertEquals("John Doe completed the Task, but someone else got the credit", "johndoe", historyTask.getAssignee());
  }
}
