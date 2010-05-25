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
package org.jbpm.test.timer;

import java.util.List;

import org.jbpm.api.Execution;
import org.jbpm.api.ProcessInstance;
import org.jbpm.api.history.HistoryTask;
import org.jbpm.api.job.Job;
import org.jbpm.api.task.Task;
import org.jbpm.test.JbpmTestCase;

/**
 * A triggered timer does not end the task which is left.
 * 
 * @see <a href="https://jira.jboss.org/browse/JBPM-2537">JBPM-2537</a>
 * @author Joram Barrez
 * @author Ronald van Kuijk
 * @author Maciej Swiderski
 */
public class TaskTimerTaskTest extends JbpmTestCase {
  
  public void testTaskClosedWhenTimerFires() {
    deployJpdlXmlString(
      "<process name='TaskTimer'>" +
      "  <start name='start'>" +
      "    <transition to='do work' />" +
      "  </start>" +
      "  <task name='do work' assignee='johndoe'>" +
      "    <transition name='done' to='go home' />" +
      "    <transition name='lunch' to='go to cafeteria'>" +
      "      <timer duedate='10 seconds' />" +
      "    </transition>" +
      "  </task>" +
      "  <state name='go home' />" +
      "  <state name='go to cafeteria' />" +
      "</process>");
    
    ProcessInstance processInstance = executionService.startProcessInstanceByKey("TaskTimer");

    List<Task> tasks = taskService.createTaskQuery().assignee("johndoe").list();
    assertEquals(1, tasks.size());

    Job timer = managementService
      .createJobQuery()
      .processInstanceId(processInstance.getId())
      .timers()
      .uniqueResult();
    assertNotNull(timer);
    
    managementService.executeJob(timer.getId());
    assertActivityActive(processInstance.getId(), "go to cafeteria");

    assertEquals(0, taskService.createTaskQuery().assignee("johndoe").list().size());

    String taskId = tasks.get(0).getId();
    List<HistoryTask> history = historyService.createHistoryTaskQuery().taskId(taskId).list();
    assertEquals(1, history.size());
    assertEquals(HistoryTask.STATE_OBSOLETE, history.get(0).getState());
  }

  public void testTaskClosedBySignal() {
    deployJpdlXmlString("<process name='TaskTimer'>" +
      "  <start name='start'>" +
      "    <transition to='do work' />" +
      "  </start>" +
      "  <task name='do work' assignee='johndoe'>" +
      "    <transition name='done' to='go home' />" +
      "    <transition name='lunch' to='go to cafeteria'>" +
      "      <timer duedate='10 seconds' />" +
      "    </transition>" +
      "  </task>" +
      "  <state name='go home' />" +
      "  <state name='go to cafeteria' />" +
      "</process>");

    ProcessInstance processInstance = executionService.startProcessInstanceByKey("TaskTimer");

    List<Task> tasks = taskService.createTaskQuery().assignee("johndoe").list();
    assertEquals(1, tasks.size());

    Execution exec = processInstance.findActiveExecutionIn("do work");
    processInstance = executionService.signalExecutionById(exec.getId(), "done");
    assertActivityActive(processInstance.getId(), "go home");

    assertEquals(0, taskService.createTaskQuery().assignee("johndoe").list().size());

    String taskId = tasks.get(0).getId();
    List<HistoryTask> history = historyService.createHistoryTaskQuery().taskId(taskId).list();
    assertEquals(1, history.size());
    assertEquals(HistoryTask.STATE_OBSOLETE, history.get(0).getState());
  }
}
