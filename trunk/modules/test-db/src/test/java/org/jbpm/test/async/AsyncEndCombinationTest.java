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
package org.jbpm.test.async;

import java.util.List;

import org.jbpm.api.Execution;
import org.jbpm.api.ProcessInstance;
import org.jbpm.api.job.Job;
import org.jbpm.test.JbpmTestCase;


/**
 * @author Tom Baeyens
 */
public class AsyncEndCombinationTest extends JbpmTestCase {

  public void testConcurrentEndScenario1() {
    deployJpdlXmlString(
      "<process name='AsyncEndCombination'>" +
      "  <start>" +
      "    <transition to='f' />" +
      "  </start>" +
      "  <fork name='f'>" +
      "    <transition to='a' continue='async' />" +
      "    <transition to='end' />" +
      "  </fork>" +
      "  <state name='a' />" +
      "  <end name='end' />" +
      "</process>"
    );

    ProcessInstance processInstance = executionService.startProcessInstanceByKey("AsyncEndCombination");
    assertEquals(Execution.STATE_ENDED, processInstance.getState());

    List<Job> jobs = managementService
        .createJobQuery()
        .processInstanceId(processInstance.getId())
        .list();
    
    assertEquals(0, jobs.size());
  }

  public void testConcurrentScenario2() {
    deployJpdlXmlString(
      "<process name='AsyncEndCombination'>" +
      "  <start>" +
      "    <transition to='f' />" +
      "  </start>" +
      "  <fork name='f'>" +
      "    <transition to='end' />" +
      "    <transition to='a' continue='async' />" +
      "  </fork>" +
      "  <state name='a' />" +
      "  <end name='end' />" +
      "</process>"
    );

    ProcessInstance processInstance = executionService.startProcessInstanceByKey("AsyncEndCombination");
    assertEquals(Execution.STATE_ENDED, processInstance.getState());
    
    List<Job> jobs = managementService
        .createJobQuery()
        .processInstanceId(processInstance.getId())
        .list();
    
    assertEquals(0, jobs.size());
  }

  public void testAsyncToEnd() {
    deployJpdlXmlString(
      "<process name='AsyncEndCombination'>" +
      "  <start>" +
      "    <transition to='s' />" +
      "  </start>" +
      "  <state name='s'>" +
      "    <transition to='end' continue='async' />" +
      "  </state>" +
      "  <end name='end' />" +
      "</process>"
    );

    ProcessInstance processInstance = executionService.startProcessInstanceByKey("AsyncEndCombination");
    String processInstanceId = processInstance.getId();
    processInstance = executionService.signalExecutionById(processInstanceId);
    
    assertEquals(Execution.STATE_ASYNC, processInstance.getState());
    
    List<Job> jobs = managementService
                        .createJobQuery()
                        .processInstanceId(processInstanceId)
                        .list();
    
    assertEquals(1, jobs.size());
    
    managementService.executeJob(jobs.get(0).getId());
    
    jobs = managementService
        .createJobQuery()
        .processInstanceId(processInstanceId)
        .list();
    
    assertEquals(0, jobs.size());

    assertNull(executionService.findProcessInstanceById(processInstanceId));
  }
}
