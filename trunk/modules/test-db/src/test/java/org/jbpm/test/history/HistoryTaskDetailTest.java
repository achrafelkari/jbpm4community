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
package org.jbpm.test.history;

import java.util.Date;
import java.util.List;

import org.jbpm.api.TaskService;
import org.jbpm.api.cmd.Environment;
import org.jbpm.api.cmd.VoidCommand;
import org.jbpm.api.task.Task;
import org.jbpm.test.JbpmTestCase;

/**
 * @author Huisheng Xu
 */
public class HistoryTaskDetailTest extends JbpmTestCase {
  private static final String PROCESS_XML =
      "<process name='HistoryTaskDetail'>" +
      "  <start>" +
      "    <transition to='review' />" +
      "  </start>" +
      "  <task name='review' " +
      "        assignee='johndoe'>" +
      "    <transition to='wait' />" +
      "  </task>" +
      "  <state name='wait'/>" +
      "</process>";

  protected void setUp() throws Exception {
    super.setUp();
    deployJpdlXmlString(PROCESS_XML);
  }

  public void testHistoryAssignment() {
    executionService.startProcessInstanceByKey("HistoryTaskDetail");

    List<Task> tasks = taskService.findPersonalTasks("johndoe");
    assertEquals(1, tasks.size());
    String taskId = tasks.get(0).getId();
    processEngine.execute(new TaskReassignCmd(taskId, "Lingo"));

    assertEquals(1, taskService.findPersonalTasks("Lingo").size());
    assertEquals(1, historyService.createHistoryDetailQuery().list().size());
  }

  public void testHistoryPriority() {
    executionService.startProcessInstanceByKey("HistoryTaskDetail");

    List<Task> tasks = taskService.findPersonalTasks("johndoe");
    assertEquals(1, tasks.size());
    String taskId = tasks.get(0).getId();
    processEngine.execute(new TaskChangePriorityCmd(taskId, 10));

    assertEquals(1, historyService.createHistoryDetailQuery().list().size());
  }

  public void testHistoryDuedate() {
    executionService.startProcessInstanceByKey("HistoryTaskDetail");

    List<Task> tasks = taskService.findPersonalTasks("johndoe");
    assertEquals(1, tasks.size());
    String taskId = tasks.get(0).getId();
    processEngine.execute(new TaskChangeDuedateCmd(taskId, new Date()));

    assertEquals(1, historyService.createHistoryDetailQuery().list().size());
  }

  private static class TaskReassignCmd extends VoidCommand {
    private String taskId;
    private String newAssignee;
    private static final long serialVersionUID = 1L;

    TaskReassignCmd(String taskId, String newAssignee) {
      this.taskId = taskId;
      this.newAssignee = newAssignee;
    }
    @Override
    protected void executeVoid(Environment environment) throws Exception {
      TaskService taskService = environment.get(TaskService.class);
      Task task = taskService.getTask(taskId);
      task.setAssignee(newAssignee);
      taskService.saveTask(task);
    }
  }

  private static class TaskChangePriorityCmd extends VoidCommand {
    private String taskId;
    private int newPriority;
    private static final long serialVersionUID = 1L;

    TaskChangePriorityCmd(String taskId, int newPriority) {
      this.taskId = taskId;
      this.newPriority = newPriority;
    }
    @Override
    protected void executeVoid(Environment environment) throws Exception {
      TaskService taskService = environment.get(TaskService.class);
      Task task = taskService.getTask(taskId);
      task.setPriority(newPriority);
      taskService.saveTask(task);
    }
  }

  private static class TaskChangeDuedateCmd extends VoidCommand {
    private String taskId;
    private Date newDuedate;
    private static final long serialVersionUID = 1L;

    TaskChangeDuedateCmd(String taskId, Date newDuedate) {
      this.taskId = taskId;
      this.newDuedate = newDuedate;
    }
    @Override
    protected void executeVoid(Environment environment) throws Exception {
      TaskService taskService = environment.get(TaskService.class);
      Task task = taskService.getTask(taskId);
      task.setDuedate(newDuedate);
      taskService.saveTask(task);
    }
  }

}
