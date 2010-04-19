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

import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.jbpm.api.task.Task;
import org.jbpm.pvm.internal.util.Clock;
import org.jbpm.test.JbpmTestCase;

/**
 * Exercises for various ways to define the deadline of a task.
 * 
 * @see <a href="https://jira.jboss.org/jira/browse/JBPM-2560">JBPM-2560</a>
 * @author Alejandro Guizar
 */
public class TaskDueDateTest extends JbpmTestCase {

  public void testBaseDate() {
    deployJpdlXmlString(
        "<process name='BaseDate'>" +
        "  <start><transition to='t'/></start>" +
        "  <task name='t' duedate='#{tomorrow}'/>" +
        "</process>");

    Calendar tomorrow = Calendar.getInstance();
    tomorrow.add(Calendar.DATE, 1);
    Map<String, ? > variables = Collections.singletonMap("tomorrow", tomorrow);
    String processInstanceId = executionService.startProcessInstanceByKey("BaseDate", variables)
        .getId();

    List<Task> taskList = taskService.createTaskQuery()
        .processInstanceId(processInstanceId)
        .list();
    assertEquals(1, taskList.size());

    Task task = taskList.get(0);
    assertEquals(tomorrow.getTime(), task.getDuedate());
  }

  public void testBaseDatePlusDuration() {
    deployJpdlXmlString(
        "<process name='BaseDatePlusDuration'>" +
        "  <start><transition to='t'/></start>" +
        "  <task name='t' duedate='#{tomorrow} + 1 hour'/>" +
        "</process>");

    Calendar tomorrow = Calendar.getInstance();
    tomorrow.add(Calendar.DATE, 1);
    Map<String, ? > variables = Collections.singletonMap("tomorrow", tomorrow);
    String processInstanceId = executionService.startProcessInstanceByKey(
        "BaseDatePlusDuration", variables).getId();

    List<Task> taskList = taskService.createTaskQuery()
        .processInstanceId(processInstanceId)
        .list();
    assertEquals(1, taskList.size());

    Task task = taskList.get(0);
    tomorrow.add(Calendar.HOUR, 1);
    assertEquals(tomorrow.getTime(), task.getDuedate());
  }

  public void testBaseDateMinusDuration() {
    deployJpdlXmlString(
        "<process name='BaseDateMinusDuration'>" +
        "  <start><transition to='t'/></start>" +
        "  <task name='t' duedate='#{tomorrow} - 1 hour'/>" +
        "</process>");

    Calendar tomorrow = Calendar.getInstance();
    tomorrow.add(Calendar.DATE, 1);
    Map<String, ? > variables = Collections.singletonMap("tomorrow", tomorrow);
    String processInstanceId = executionService.startProcessInstanceByKey(
        "BaseDateMinusDuration", variables).getId();

    List<Task> taskList = taskService.createTaskQuery()
        .processInstanceId(processInstanceId)
        .list();
    assertEquals(1, taskList.size());

    Task task = taskList.get(0);
    tomorrow.add(Calendar.HOUR, -1);
    assertEquals(tomorrow.getTime(), task.getDuedate());
  }

  public void testDuration() {
    deployJpdlXmlString(
        "<process name='Duration'>" +
        "  <start><transition to='t'/></start>" +
        "  <task name='t' duedate='1 hour'/>" +
        "</process>");

    Calendar now = Calendar.getInstance();
    // prevent wrong results in databases lacking millisecond precision
    now.set(Calendar.MILLISECOND, 0);
    Clock.setExplicitTime(now.getTime());

    String processInstanceId = executionService.startProcessInstanceByKey("Duration").getId();

    List<Task> taskList = taskService.createTaskQuery()
        .processInstanceId(processInstanceId)
        .list();
    assertEquals(1, taskList.size());

    Task task = taskList.get(0);
    now.add(Calendar.HOUR, 1);
    // because task.getDuedate() returns java.sql.Timestamp here,
    // comparison has to be made in milliseconds
    assertEquals(now.getTimeInMillis(), task.getDuedate().getTime());

    Clock.setExplicitTime(null);
  }
}
