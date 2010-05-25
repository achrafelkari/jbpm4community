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

import java.util.Date;
import java.util.List;

import org.jbpm.api.ProcessInstance;
import org.jbpm.api.job.Job;
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

  public void testSwimlaneWithMultiplicity() {
    deployJpdlXmlString(
      "<process key='join-multiplicity-test' name='join-multiplicity-test' xmlns='http://jbpm.org/4.3/jpdl'>" +
      "   <swimlane name='swimlane1' assignee='user1'/>" +
      "   <swimlane name='swimlane2' assignee='user2'/>" +
      "   <start g='165,4,48,48' name='start1'>" +
      "      <transition g='-50,-20' name='to task1' to='task1'/>" +
      "   </start>" +
      "   <end g='165,511,48,48' name='end1'/>" +
      "   <task g='143,97,92,52' name='task1' swimlane='swimlane1'>" +
      "      <transition g='-50,-20' name='to fork1' to='fork1'/>" +
      "   </task>" +
      "   <task g='9,282,92,52' name='task2' swimlane='swimlane1'>" +
      "      <transition g='-49,-20' name='to join1' to='join1'/>" +
      "   </task>" +
      "   <task g='283,282,92,52' name='task3' swimlane='swimlane2'>" +
      "      <transition g='-49,-20' name='to join1' to='join1'/>" +
      "   </task>" +
      "   <fork g='165,185,48,48' name='fork1'>" +
      "      <transition g='-50,-20' name='to task2' to='task2'/>" +
      "      <transition g='-50,-20' name='to task3' to='task3'/>" +
      "   </fork>" +
      "   <join continue='async' g='165,387,48,48' multiplicity='1' name='join1'>" +
      "      <transition g='-50,-20' name='to end1' to='end1'/>" +
      "   </join>" +
      "</process>"
    );

    ProcessInstance processInstance = executionService.startProcessInstanceByKey("join-multiplicity-test");
    String processInstanceId = processInstance.getId();

    List<Task> user1Tasks = taskService.findPersonalTasks("user1");
    assertEquals(1, user1Tasks.size());
    Task task1 = user1Tasks.get(0);
    taskService.completeTask(task1.getId(), "to fork1");

    user1Tasks = taskService.findPersonalTasks("user1");
    assertEquals(1, user1Tasks.size());
    Task task2 = user1Tasks.get(0);
    taskService.completeTask(task2.getId(), "to join1");

    List<Job> jobs = managementService.createJobQuery().processInstanceId(processInstanceId).list();
    assertEquals(1, jobs.size());
    Job join1Job = jobs.get(0);
    managementService.executeJob(join1Job.getId());

    Date endTime = historyService.createHistoryProcessInstanceQuery().processInstanceId(processInstance.getId()).uniqueResult().getEndTime();

    assertNotNull(endTime);
  }

  public void testTaskParse() {
    deployJpdlXmlString(
      "<process name='Swimlane' xmlns='http://jbpm.org/4.3/jpdl'>"
      + "<swimlane name='user'/>"
      + "<start g='168,19,48,48' name='start1'>"
      + "  <transition g='-61,-18' name='to FirstTask' to='FirstTask'/>"
      + "</start>"
      + "<task g='155,86,92,52' name='FirstTask' swimlane='user'>"
      + "  <transition g='-64,-18' name='to timerTask' to='timerTask'/>"
      + "</task>"
      + "<task g='157,228,92,52' name='timerTask' swimlane='user'>"
      + "  <transition g='-42,-18' name='to end1' to='end1'>"
      + "    <timer duedate='30 seconds' />"
      + "  </transition>"
      + "</task>"
      + "<end g='176,330,48,48' name='end1'/>"
      + "</process>");

    ProcessInstance processInstance = executionService.startProcessInstanceByKey("Swimlane");
    Task firstTask = taskService.createTaskQuery().processInstanceId(processInstance.getId()).uniqueResult();

    taskService.assignTask(firstTask.getId(), "alex");
    firstTask = taskService.getTask(firstTask.getId());
    assertEquals("alex", firstTask.getAssignee());
    assertEquals("FirstTask", firstTask.getName());
    taskService.completeTask(firstTask.getId());

    Task timerTask = taskService.createTaskQuery().processInstanceId(processInstance.getId()).uniqueResult();
    assertEquals("timerTask", timerTask.getName());
    assertEquals("alex", timerTask.getAssignee());
  }
}
