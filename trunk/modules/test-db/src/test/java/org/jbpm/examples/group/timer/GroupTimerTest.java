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
package org.jbpm.examples.group.timer;

import java.util.List;

import org.jbpm.api.Execution;
import org.jbpm.api.ProcessInstance;
import org.jbpm.api.job.Job;
import org.jbpm.api.job.Timer;
import org.jbpm.test.JbpmTestCase;


/**
 * @author Tom Baeyens
 */
public class GroupTimerTest extends JbpmTestCase {

  String deploymentId;
  
  protected void setUp() throws Exception {
    super.setUp();
    
    deploymentId = repositoryService.createDeployment()
        .addResourceFromClasspath("org/jbpm/examples/group/timer/process.jpdl.xml")
        .deploy();
  }

  protected void tearDown() throws Exception {
    repositoryService.deleteDeploymentCascade(deploymentId);
    
    super.tearDown();
  }

  public void testGroupTimerFires() {
    ProcessInstance processInstance = executionService.startProcessInstanceByKey("GroupTimer");
    Execution approveExecution = processInstance.findActiveExecutionIn("approve");
    assertNotNull(approveExecution);
    
    List<Job> jobs = managementService
      .createJobQuery()
      .processInstanceId(processInstance.getId())
      .list();
    
    assertEquals(1, jobs.size());
    
    Timer timer = (Timer) jobs.get(0);
    
    managementService.executeJob(timer.getId());
    
    processInstance = executionService.findProcessInstanceById(processInstance.getId());
    assertNotNull(processInstance.findActiveExecutionIn("escalate") );
  }
  
}
