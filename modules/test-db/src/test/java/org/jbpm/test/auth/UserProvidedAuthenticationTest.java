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
package org.jbpm.test.auth;

import org.jbpm.api.Execution;
import org.jbpm.api.task.Task;
import org.jbpm.test.JbpmTestCase;


/**
 * @author Tom Baeyens
 */
public class UserProvidedAuthenticationTest extends JbpmTestCase {

  public void testUserProvidedAuthentication() {
    deployJpdlXmlString(
      "<process name='UserProvidedAuthentication'>" +
      "  <start>" +
      "    <transition to='t' />" +
      "  </start>" +
      "  <task name='t' assignee='johndoe'>" +
      "    <transition to='s' />" +
      "  </task>" +
      "  <state name='s' />" +
      "</process>"
    );
  
    Execution processInstance = executionService.startProcessInstanceByKey("UserProvidedAuthentication");
    
    Task task = taskService.createTaskQuery()
      .processInstanceId(processInstance.getId())
      .uniqueResult();

    processEngine.setAuthenticatedUserId("jackblack");
    taskService.addTaskComment(task.getId(), "the lord of the ring");

    task.getId();
  }
}
