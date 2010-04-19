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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jbpm.api.ProcessInstance;
import org.jbpm.api.model.OpenExecution;
import org.jbpm.api.task.Assignable;
import org.jbpm.api.task.AssignmentHandler;
import org.jbpm.api.task.Task;
import org.jbpm.test.JbpmTestCase;


/**
 * @author Tom Baeyens
 */
public class TaskAssignmentHandlerExprTest extends JbpmTestCase {
  
  public static class MyAssignmentHandler implements AssignmentHandler {
    private static final long serialVersionUID = 1L;
    public void assign(Assignable assignable, OpenExecution execution) throws Exception {
      assignable.setAssignee("johndoe");
    }
  }

  public void testTaskAssignmentHandlerExpr() {
    deployJpdlXmlString(
      "<process name='TaskAssignmentHandlerExpr'>" +
      "  <start>" +
      "    <transition to='review' />" +
      "  </start>" +
      "  <task name='review'>" +
      "    <assignment-handler expr='#{myassignmenthandler}' />" +
      "    <transition to='wait' />" +
      "  </task>" +
      "  <state name='wait'/>" +
      "</process>"
    );
    
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("myassignmenthandler", new MyAssignmentHandler());
    ProcessInstance processInstance = executionService.startProcessInstanceByKey("TaskAssignmentHandlerExpr", variables);

    // both johndoe and joesmoe will see the task in their *group* task list 
    List<Task> taskList = taskService.findPersonalTasks("johndoe");
    assertEquals("Expected a single task being created", 1, taskList.size());
    Task task = taskList.get(0);
    assertEquals("review", task.getName());
  }


}
