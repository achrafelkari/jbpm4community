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
import org.jbpm.api.cmd.Command;
import org.jbpm.api.cmd.Environment;
import org.jbpm.api.history.HistoryActivityInstance;
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

  static class TaskReassignCmd implements Command<Void> {
    private String taskId;
    private String newAssignee;
    public TaskReassignCmd(String taskId, String newAssignee) {
      this.taskId = taskId;
      this.newAssignee = newAssignee;
    }
    public Void execute(Environment env) {
      Task task = taskService.getTask(taskId);
      task.setAssignee(newAssignee);
      taskService.saveTask(task);

      return null;
    }
  }

  static class TaskChangePriorityCmd implements Command<Void> {
    private String taskId;
    private int newPriority;
    public TaskChangePriorityCmd(String taskId, int newPriority) {
      this.taskId = taskId;
      this.newPriority = newPriority;
    }
    public Void execute(Environment env) {
      Task task = taskService.getTask(taskId);
      task.setPriority(newPriority);
      taskService.saveTask(task);

      return null;
    }
  }

  static class TaskChangeDuedateCmd implements Command<Void> {
    private String taskId;
    private Date newDuedate;
    public TaskChangeDuedateCmd(String taskId, Date newDuedate) {
      this.taskId = taskId;
      this.newDuedate = newDuedate;
    }
    public Void execute(Environment env) {
      Task task = taskService.getTask(taskId);
      task.setDuedate(newDuedate);
      taskService.saveTask(task);

      return null;
    }
  }

}