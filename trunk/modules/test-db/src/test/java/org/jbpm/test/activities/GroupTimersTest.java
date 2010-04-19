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

import org.jbpm.api.Execution;
import org.jbpm.api.ProcessInstance;
import org.jbpm.api.job.Job;
import org.jbpm.api.job.Timer;
import org.jbpm.test.JbpmTestCase;


/**
 * @author Tom Baeyens
 */
public class GroupTimersTest extends JbpmTestCase {

  public void testGroupWithTimer() {
    deployJpdlXmlString(
      "<process name='Group'>" +
      "  <start>" +
      "    <transition to='group' />" +
      "  </start>" +
      "  <group name='group'>" +
      "    <start>" +
      "      <transition to='a' />" +
      "    </start>" +
      "    <state name='a'>" +
      "      <transition to='done' />" +
      "    </state>" +
      "    <end name='done' />" +
      "    <transition to='end' />" +
      "    <transition name='timeout' to='escalate'>" +
      "      <timer duedate='2 hours' />" +
      "    </transition>" +
      "  </group>" +
      "  <state name='end' />" +
      "  <state name='escalate' />" +
      "</process>"
    );
    
    ProcessInstance processInstance = executionService.startProcessInstanceByKey("Group");
    
    assertEquals(Execution.STATE_INACTIVE_SCOPE, processInstance.getState());
    assertEquals(1, processInstance.getExecutions().size());
    Execution groupExecution = processInstance.getExecutions().iterator().next();
    assertEquals(Execution.STATE_ACTIVE_ROOT, groupExecution.getState());
    assertTrue(groupExecution.isActive("a"));

    processInstance = executionService.signalExecutionById(groupExecution.getId());

    assertEquals(Execution.STATE_ACTIVE_ROOT, processInstance.getState());
    assertEquals(0, processInstance.getExecutions().size());

    
    processInstance = executionService.startProcessInstanceByKey("Group");

    List<Job> jobs = managementService
      .createJobQuery()
      .processInstanceId(processInstance.getId())
      .list();
    
    assertEquals(1, jobs.size());
    
    Timer timer = (Timer) jobs.get(0);
    assertEquals("timeout", timer.getSignalName());
    
    managementService.executeJob(timer.getId());
    
    processInstance = executionService.findProcessInstanceById(processInstance.getId());
    
    assertEquals(Execution.STATE_ACTIVE_ROOT, processInstance.getState());
    assertTrue(processInstance.isActive("escalate"));
    assertEquals(0, processInstance.getExecutions().size());
  }
}
