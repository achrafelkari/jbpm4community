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
package org.jbpm.test.idgenerator;

import java.util.List;

import javax.persistence.Lob;

import org.jbpm.api.Deployment;
import org.jbpm.api.NewDeployment;
import org.jbpm.api.ProcessEngine;
import org.jbpm.api.ProcessInstance;
import org.jbpm.pvm.internal.id.DbidGenerator;
import org.jbpm.pvm.internal.id.MemoryDbidGenerator;
import org.jbpm.pvm.internal.repository.DeploymentImpl;
import org.jbpm.test.JbpmCustomCfgTestCase;


/**
 * @author Joram Barrez
 */
public class DeploymentIdGenerationTest extends JbpmCustomCfgTestCase {
  
  /**
   * Test for a specific problem discovered after changing the idGeneration:
   * 
   * After deploying some processes, the server is restarted and the
   * {@link ProcessEngine} is recreated. At that point, when trying to deploy a
   * process from the classpath, a new {@link Lob} object is created.
   * 
   * This Lob object required a new dbId from the current DbIdGenerator.
   * However, the id acquirement happened outside an environment block which
   * means that the default DbIdGenerator was used: the
   * {@link MemoryDbidGenerator}.
   * 
   * Of course, after a restart the member fields of the
   * {@link MemoryDbidGenerator} were reset, leading to generated ids that were
   * used before, causing all kind of strange Hibernate errors.
   * 
   * This test emulates this situation by closing and nullifying the
   * {@link ProcessEngine} and deploying and continuing a given process.
   */
  public void testDeploymentIdGenerationAfterProcessEngineClose() {
    
    // Start a simple process
    deployTestProcess();
    String pid = executionService.startProcessInstanceByKey("simpleProcess").getId();
    
    // reset process engine and redeploy
    resetProcessEngineAndDbidGenerator();
    deployTestProcess();
    
    // There should now be 2 deployments in the database, with a different id
    List<Deployment> deployments = repositoryService.createDeploymentQuery().list();
    assertEquals(2, deployments.size());
    assertNotSame(deployments.get(0).getId(), deployments.get(1).getId());

    String execId = executionService.findProcessInstanceById(pid).findActiveExecutionIn("a").getId();
    executionService.signalExecutionById(execId);
    assertActivityActive(pid, "b");
    
    resetProcessEngineAndDbidGenerator();
    execId = executionService.findProcessInstanceById(pid).findActiveExecutionIn("b").getId();
    executionService.signalExecutionById(execId);
    assertProcessInstanceEnded(pid);
  }
  
  private void deployTestProcess() {
    NewDeployment deployment = repositoryService.createDeployment()
      .addResourceFromClasspath(getClass().getPackage().getName().replace(".", "/")+"/process.jpdl.xml");
    deployment.deploy();
    registerDeployment(deployment.getId());
  }
  
  private void resetProcessEngineAndDbidGenerator() {
    // Close the process engine and recreate it
    ProcessEngine oldProcessEngine = processEngine;
    processEngine.close();
    processEngine = null;
    
    initialize(); // creates a new ProcessEngine and services
    assertNotSame(oldProcessEngine, processEngine);
   
    // Reset the in memory generator and redeploy the process
    MemoryDbidGenerator memoryDbidGenerator = (MemoryDbidGenerator) DbidGenerator.getDefaultIdGenerator();
    memoryDbidGenerator.reset();
  }

}
