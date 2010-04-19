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
/**
 * 
 */
package org.jbpm.test.repocache;

import org.jbpm.api.Configuration;
import org.jbpm.api.Execution;
import org.jbpm.api.ExecutionService;
import org.jbpm.api.JbpmException;
import org.jbpm.api.ProcessEngine;
import org.jbpm.api.ProcessInstance;
import org.jbpm.test.JbpmCustomCfgTestCase;
import org.jbpm.test.JbpmTestCase;


/**
 * Test case for the correct working of the repositoryCache.
 * 
 * @author Joram Barrez
 */
public class RepositoryCacheTest extends JbpmCustomCfgTestCase {
  
  private static final String PROCESS_KEY = "test_process";
  
  private static final String TEST_PROCESS =
    "<process name='" + PROCESS_KEY + "'>" +
      "  <start>" +
      "    <transition to='a' />" +
      "  </start>" +
      "  <state name='a'>" +
      "    <transition to='theEnd' />" +
      "  </state>" +
      "  <end name='theEnd' />" +
      "</process>";

  /**
   * Test for JBPM-2360:
   * 
   * When a deployment is deleted, the entry is also removed from the
   * repositoryCache. However, in a clustered environment, the deletion from the
   * cache is done only on the node on which the deletion operation is invoked.
   * This means that the other nodes are unaware of the deletion and the 
   * deployment is not deleted from the cache.
   * 
   * This test emulates this scenario using two process engines which use
   * the same database. One will delete a deployment. The second one must
   * throw an exception when it tries to use the deployment.
   *    * When starting a new process
   *    * When signalling an execution which has a deleted deployment
   */
  public void testDeleteDeploymentsUsingTwoProcessEngines() {
    ProcessEngine processEngine2 = createProcessEngineFromAlternativeConfig();
    ExecutionService executionService2 = processEngine2.getExecutionService();
    assertFalse(processEngine.equals(processEngine2));
    assertFalse(executionService.equals(executionService2));
    
    // Deploy the process through the first process engine
    String deployId = repositoryService.createDeployment()
                     .addResourceFromString("test_process.jpdl.xml", TEST_PROCESS)
                     .deploy();
    
    // Start process instance on first process engine
    ProcessInstance pi1 = executionService.startProcessInstanceByKey(PROCESS_KEY);
    Execution executionAtWaitState1 = pi1.findActiveExecutionIn("a");
    assertActivityActive(pi1.getId(), "a");
    
    // Now start process instance on second process engine
    ProcessInstance pi2 = executionService2.startProcessInstanceByKey(PROCESS_KEY);
    Execution executionAtWaitState2 = pi1.findActiveExecutionIn("a");
    assertActivityActive(pi2.getId(), "a");
    
    // Delete the deployment through the first process engine
    repositoryService.deleteDeploymentCascade(deployId);
    
    // Trying to find the two active process instances should fail now
    assertNull(executionService.findExecutionById(pi1.getId()));
    assertNull(executionService.findExecutionById(pi2.getId()));
    
    // Try to start the process through the first process engine. 
    // This should fail (since the deployment was deleted).
    try {
      executionService.startProcessInstanceByKey(PROCESS_KEY);
      fail();
    } catch (JbpmException e) { }
    
    // Try to start the process through the second process engine.
    // This should also fail (ie caches should be updated on both side).
    try {
      executionService2.startProcessInstanceByKey(PROCESS_KEY);
      fail();
    } catch (JbpmException e) { }
    
    // Try to signal a process that was active before the deletion. This should also fail.
    try {
      executionService.signalExecutionById(executionAtWaitState1.getId());
      fail();
    } catch (JbpmException e) { }
    try {
      executionService2.signalExecutionById(executionAtWaitState2.getId());
      fail();
    } catch (JbpmException e) { }
  }
   
  private ProcessEngine createProcessEngineFromAlternativeConfig() {
    Configuration configuration = new Configuration().setResource("org/jbpm/test/repocache/jbpm_alternative.cfg.xml");
    return configuration.buildProcessEngine();
  }

}
