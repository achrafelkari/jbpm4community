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
package org.jbpm.examples.goup.concurrency;

import org.jbpm.api.ProcessInstance;
import org.jbpm.test.JbpmTestCase;


/**
 * @author Tom Baeyens
 */
public class GroupConcurrencyTest extends JbpmTestCase {

  String deploymentId;
  
  protected void setUp() throws Exception {
    super.setUp();
    
    deploymentId = repositoryService.createDeployment()
        .addResourceFromClasspath("org/jbpm/examples/group/concurrency/process.jpdl.xml")
        .deploy();
  }

  protected void tearDown() throws Exception {
    repositoryService.deleteDeploymentCascade(deploymentId);
    
    super.tearDown();
  }

  public void testOneFeedbackLoop() {
    ProcessInstance pi = executionService
        .startProcessInstanceByKey("GroupConcurrency");
    
    String documentExecutionId = pi
        .findActiveExecutionIn("distribute document").getId();
    
    String planningExecutionId = pi
        .findActiveExecutionIn("make planning").getId();
    
    pi = executionService.signalExecutionById(documentExecutionId);
    assertNotNull(pi.findActiveExecutionIn("collect feedback"));
    assertNotNull(pi.findActiveExecutionIn("make planning"));
    
    pi = executionService.signalExecutionById(planningExecutionId);
    assertNotNull(pi.findActiveExecutionIn("collect feedback"));
    assertNotNull(pi.findActiveExecutionIn("estimate budget"));
    
    pi = executionService.signalExecutionById(planningExecutionId);
    assertNotNull(pi.findActiveExecutionIn("collect feedback"));
    
    pi = executionService.signalExecutionById(documentExecutionId);
    assertNotNull(pi.findActiveExecutionIn("public project announcement"));
  }
}
