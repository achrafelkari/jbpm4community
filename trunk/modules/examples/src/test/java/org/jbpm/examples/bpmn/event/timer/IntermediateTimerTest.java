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
package org.jbpm.examples.bpmn.event.timer;

import java.util.Date;

import org.jbpm.api.NewDeployment;
import org.jbpm.api.ProcessInstance;
import org.jbpm.api.job.Job;
import org.jbpm.pvm.internal.util.Clock;
import org.jbpm.test.JbpmTestCase;

/**
 * 
 * @author Joram Barrez
 */
public class IntermediateTimerTest extends JbpmTestCase {

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    NewDeployment deployment = repositoryService.createDeployment();
    deployment.addResourceFromClasspath("org/jbpm/examples/bpmn/event/timer/intermediate_catch_timer_event.bpmn.xml");
    registerDeployment(deployment.deploy());
  }
  
  @Override
  protected void tearDown() throws Exception {
    Clock.setExplicitTime(null);
    super.tearDown();
  }

  public void testIntermediateTimerWithDuration() {
    Clock.setExplicitTime(new Date(0));
    
    ProcessInstance processInstance = executionService.startProcessInstanceByKey("intermediateTimerCatch");
    Job timerJob = managementService.createJobQuery()
                                    .processInstanceId(processInstance.getId())
                                    .uniqueResult();
    assertEquals(timerJob.getDuedate().getTime(), new Date(60 * 60 *1000).getTime());
    
    managementService.executeJob(timerJob.getId());
    assertProcessInstanceEnded(processInstance);
  }

}
