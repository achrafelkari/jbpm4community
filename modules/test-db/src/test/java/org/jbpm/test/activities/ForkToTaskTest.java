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
package org.jbpm.test.activities;

import java.util.List;

import org.jbpm.api.task.Task;
import org.jbpm.pvm.internal.model.ExecutionImpl;
import org.jbpm.test.JbpmTestCase;


/**
 * @author Tom Baeyens
 */
public class ForkToTaskTest extends JbpmTestCase {

  public void testForkToTasks() {
    
    // test for JBPM-2287
    
    deployJpdlXmlString(
      "<process name='ForkToTasks'>" +
      "  <start>" +
      "    <transition to='f' />" +
      "  </start>" +
      "  <fork name='f'>" +
      "    <transition to='assemble product' />" +
      "    <transition to='print documents' />" +
      "  </fork>" +
      "  <task name='assemble product' assignee='johndoe'>" +
      "    <transition to='j' />" +
      "  </task>" +
      "  <task name='print documents' assignee='johndoe'>" +
      "    <transition to='j' />" +
      "  </task>" +
      "  <join name='j'>" +
      "    <transition to='end' />" +
      "  </join>" +
      "  <end name='end' />" +
      "</process>"
    );

    executionService.startProcessInstanceByKey("ForkToTasks");
  }
  
  public void testForkWithUnfinishedPath() {
    deployJpdlXmlString(
      "<process name='forkTest'>" +
      "  <start name='start1'>" +
      "    <transition to='fork'/>" +
      "  </start>" +
      "  <fork name='fork'>" +
      "    <transition name='review1' to='review1'/>" +
      "    <transition name='review2' to='review2'/>" +
      "    <transition name='control1' to='control'/>" +
      "  </fork>" +
      "  <task candidate-users='requester' name='review1'>" +
      "    <transition name='Approve' to='join'/>" +
      "    <transition name='Reject' to='rejected1'/>" +
      "  </task>" +
      "  <task candidate-users='requester' name='review2'>" +
      "    <transition name='Approve' to='join'/>" +
      "    <transition name='Reject' to='rejected2'/>" +
      "  </task>" +
      "  <task candidate-groups='control' name='control' />" +
      "  <join name='join'>" +
      "    <transition to='end'/>" +
      "  </join>" +
      "  <end name='end'/>" +
      "  <end-cancel name='rejected2'/>" +
      "  <end-cancel name='rejected1'/>" +
      "</process>"
    );

    String processInstanceId = executionService.startProcessInstanceByKey("forkTest").getId();
    List<Task> tasks = taskService.createTaskQuery().processInstanceId(processInstanceId).list();
    assertEquals(3, tasks.size());
    
    final String requester = "requester";
    List<Task> tasksForRequester = taskService.findGroupTasks(requester);
    assertEquals(2, tasksForRequester.size());

    for (Task task : tasksForRequester) {
      taskService.takeTask(task.getId(), requester);
      taskService.completeTask(task.getId(), "Approve");
    }
    
    assertProcessInstanceEnded(processInstanceId);
  }
}
