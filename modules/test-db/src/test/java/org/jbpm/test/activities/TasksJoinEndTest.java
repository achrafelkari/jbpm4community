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

import org.jbpm.api.ProcessInstance;
import org.jbpm.api.task.Task;
import org.jbpm.internal.log.Log;
import org.jbpm.test.JbpmTestCase;


/**
 * @author Tom Baeyens
 */
public class TasksJoinEndTest extends JbpmTestCase {
  
  private static Log log = Log.getLog(TasksJoinEndTest.class.getName());
  

  public void testTasksJoinEnd() {
    log.debug("");
    log.debug("");
    log.debug("### DEPLOYING PROCESS DEFINITION ######################################");
    log.debug("");
    log.debug("");
    
    deployJpdlXmlString(
      "<process name='TasksJoinEnd'>" +
      "  <start>" +
      "    <transition to='theFork' />" +
      "  </start>" +
      "  <fork name='theFork'>" +
      "    <transition to='taskOne' />" +
      "    <transition to='taskTwo' />" +
      "    <transition to='extraState' />" +
      "  </fork>" +
      "  <task name='taskOne'>" +
      "    <transition to='theJoin' />" +
      "  </task> " +
      "  <task name='taskTwo'>" +
      "    <transition to='theJoin' />" +
      "  </task> " +
      "  <state name='extraState' />" +
      "  <join name='theJoin'>" +
      "    <transition to='end' />" +
      "  </join>" +
      "  <end name='end' />" +
      "</process>"
    );
    
    log.debug("");
    log.debug("");
    log.debug("### STARTING PROCESS INSTANCE ######################################");
    log.debug("");
    log.debug("");

    executionService.startProcessInstanceByKey("TasksJoinEnd");

    List<Task> tasks = taskService.createTaskQuery().list();
    
    log.debug("");
    log.debug("");
    log.debug("### COMPLETING TASK ONE ######################################");
    log.debug("");
    log.debug("");

    taskService.completeTask(tasks.get(0).getId());
    
    log.debug("");
    log.debug("");
    log.debug("### COMPLETING TASK TWO ######################################");
    log.debug("");
    log.debug("");

    taskService.completeTask(tasks.get(1).getId());
  }
}
