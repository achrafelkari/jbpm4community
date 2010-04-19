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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jbpm.api.Execution;
import org.jbpm.api.ProcessInstance;
import org.jbpm.api.activity.ActivityBehaviour;
import org.jbpm.api.activity.ActivityExecution;
import org.jbpm.api.job.Job;
import org.jbpm.test.JbpmTestCase;


/**
 * @author Tom Baeyens
 * @author Ronald van Kuijk
 */
public class AsyncBasicsTest extends JbpmTestCase {
  
  public static class DoABit implements ActivityBehaviour {
    public void execute(ActivityExecution execution) {
      execution.setVariable("done", "a bit");
    }
  }

  public static class DoALot implements ActivityBehaviour {
    public void execute(ActivityExecution execution) {
      execution.setVariable("done", "a lot");
    }
  }

  public void testAsyncAutomaticSequence() {
    deployJpdlXmlString(
      "<process name='TwoAsyncStates'>" +
      "  <start>" +
      "    <transition to='a' />" +
      "  </start>" +
      "  <custom name='a' continue='async' class='"+DoABit.class.getName()+"'>" +
      "    <transition to='b' />" +
      "  </custom>" +
      "  <custom name='b' continue='async' class='"+DoALot.class.getName()+"'>" +
      "    <transition to='end' />" +
      "  </custom>" +
      "  <state name='end' />" +
      "</process>"
    );
    
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("done", "nothing");
    ProcessInstance processInstance = executionService.startProcessInstanceByKey("TwoAsyncStates", variables);
    String processInstanceId = processInstance.getId();

    assertEquals(Execution.STATE_ASYNC, processInstance.getState());

    assertEquals("nothing", executionService.getVariable(processInstanceId, "done"));
    
    List<Job> jobs = managementService
      .createJobQuery()
      .processInstanceId(processInstance.getId())
      .list();
    
    assertEquals(1, jobs.size());
    
    Job job = jobs.get(0);
    
    managementService.executeJob(job.getId());

    assertEquals("a bit", executionService.getVariable(processInstanceId, "done"));

    processInstance = executionService.findProcessInstanceById(processInstance.getId());
    assertEquals(Execution.STATE_ASYNC, processInstance.getState());

    jobs = managementService
      .createJobQuery()
      .processInstanceId(processInstance.getId())
      .list();
    
    assertEquals(1, jobs.size());
    
    job = jobs.get(0);
    
    managementService.executeJob(job.getId());

    assertEquals("a lot", executionService.getVariable(processInstanceId, "done"));

    processInstance = executionService.findProcessInstanceById(processInstanceId);
    assertTrue(processInstance.isActive("end"));
  }

  public void testAsyncTransitions() {
    deployJpdlXmlString(
      "<process name='TwoAsyncStates'>" +
      "  <start>" +
      "    <transition to='a' />" +
      "  </start>" +
      "  <custom name='a' class='"+DoABit.class.getName()+"'>" +
      "    <transition continue='async' to='b' />" +
      "  </custom>" +
      "  <custom name='b' class='"+DoALot.class.getName()+"'>" +
      "    <transition continue='async' to='end' />" +
      "  </custom>" +
      "  <state name='end' />" +
      "</process>"
    );
    
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("done", "nothing");
    ProcessInstance processInstance = executionService.startProcessInstanceByKey("TwoAsyncStates", variables);
    String processInstanceId = processInstance.getId();

    assertEquals(Execution.STATE_ASYNC, processInstance.getState());

    assertEquals("a bit", executionService.getVariable(processInstanceId, "done"));

    List<Job> jobs = managementService
      .createJobQuery()
      .processInstanceId(processInstance.getId())
      .list();
    
    assertEquals(1, jobs.size());
    
    Job job = jobs.get(0);
    
    managementService.executeJob(job.getId());

    assertEquals("a lot", executionService.getVariable(processInstanceId, "done"));

    processInstance = executionService.findProcessInstanceById(processInstance.getId());
    assertEquals(Execution.STATE_ASYNC, processInstance.getState());

    jobs = managementService
      .createJobQuery()
      .processInstanceId(processInstance.getId())
      .list();
    
    assertEquals(1, jobs.size());
    
    job = jobs.get(0);
    
    managementService.executeJob(job.getId());

    processInstance = executionService.findProcessInstanceById(processInstanceId);
    assertTrue(processInstance.isActive("end"));
  }


  public void testExecutionBlockedDuringAsync() {
    deployJpdlXmlString(
      "<process name='AsyncState'>" +
      "  <start>" +
      "    <transition to='a' />" +
      "  </start>" +
      "  <state name='a' continue='async'>" +
      "    <transition to='end' />" +
      "  </state>" +
      "  <end name='end' />" +
      "</process>"
    );
    
    ProcessInstance processInstance = executionService.startProcessInstanceByKey("AsyncState");
    
    try {
      executionService.signalExecutionById(processInstance.getId());
      fail("expected exception");
    } catch (Exception e) {
      assertTextPresent("execution", e.getMessage());
      assertTextPresent("is not active: async", e.getMessage());
    }
  }
  
    public void testAsyncStart() {
    deployJpdlXmlString(
      "<process name='AsyncStart'>" +
      "  <start continue='async' name='start'>" +
      "    <transition to='end' />" +
      "  </start>" +
      "  <end name='end' />" +
      "</process>"
    );
    
    ProcessInstance processInstance = executionService.startProcessInstanceByKey("AsyncStart");
    String processInstanceId = processInstance.getId();

    assertEquals(Execution.STATE_ASYNC, processInstance.getState());

    List<Job> jobs = managementService
      .createJobQuery()
      .processInstanceId(processInstanceId)
      .list();
    
    assertEquals(1, jobs.size());
    managementService.executeJob(jobs.get(0).getId());
    
    assertProcessInstanceEnded(processInstanceId);

  }
}
