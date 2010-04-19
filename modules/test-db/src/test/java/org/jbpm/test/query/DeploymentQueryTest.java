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
package org.jbpm.test.query;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jbpm.api.Deployment;
import org.jbpm.api.DeploymentQuery;
import org.jbpm.test.JbpmTestCase;
import org.jbpm.test.assertion.QueryAssertions;


/**
 * @author Tom Baeyens
 * @author Joram Barrez
 */
public class DeploymentQueryTest extends JbpmTestCase {

  public void testSuspendDeployment() {
    List<String> deploymentIds = deployTestProcesses();

    repositoryService.suspendDeployment(deploymentIds.get(2));   
    
    // find all deployments
    
    List<Deployment> deployments = repositoryService
      .createDeploymentQuery()
      .list();
    
    Set<String> expectedDeploymentNames = new HashSet<String>();
    expectedDeploymentNames.add("Claim");
    expectedDeploymentNames.add("Hire");
    expectedDeploymentNames.add("Fire");
    
    assertEquals(expectedDeploymentNames, getDeploymentNames(deployments));
    
    // find suspended deployments
    
    deployments = repositoryService
      .createDeploymentQuery()
      .suspended()
      .list();
  
    expectedDeploymentNames = new HashSet<String>();
    expectedDeploymentNames.add("Hire");
  
    assertEquals(expectedDeploymentNames, getDeploymentNames(deployments));

    // find active deployments
    
    deployments = repositoryService
      .createDeploymentQuery()
      .notSuspended()
      .list();
  
    expectedDeploymentNames = new HashSet<String>();
    expectedDeploymentNames.add("Claim");
    expectedDeploymentNames.add("Fire");
  
    assertEquals(expectedDeploymentNames, getDeploymentNames(deployments));
    
    deleteCascade(deploymentIds);
  }

  
  
  private void deleteCascade(List<String> deploymentIds) {
    for (String deploymentId : deploymentIds) {
      repositoryService.deleteDeploymentCascade(deploymentId);
    }
  }
  
  public void testOrderByTimeStamp() {
    testOrderByNaturalOrdening(DeploymentQuery.PROPERTY_TIMESTAMP, 3);
  }
  
  public void testCount() {
    List<String> deploymentIds = deployTestProcesses();
    
    assertEquals(3, repositoryService.createDeploymentQuery().count());
    for (String deploymentId : deploymentIds) {
      assertEquals(1, repositoryService.createDeploymentQuery().deploymentId(deploymentId).count());      
    }
    
    Deployment deployment = repositoryService.createDeploymentQuery().deploymentId(deploymentIds.get(0)).uniqueResult();
    repositoryService.suspendDeployment(deployment.getId());
    
    assertEquals(1, repositoryService.createDeploymentQuery().suspended().count());
    assertEquals(2, repositoryService.createDeploymentQuery().notSuspended().count());
    
    deleteCascade(deploymentIds);
  }
  
  /* --------------
   * HELPER METHODS
   * -------------- */
  
  private Set<String> getDeploymentNames(List<Deployment> deployments) {
    Set<String> deploymentNames = new HashSet<String>();
    for (Deployment deployment: deployments) {
      deploymentNames.add(deployment.getName());
    }
    return deploymentNames;
  }
  
  private List<String> deployTestProcesses() {
    List<String> processIds = new ArrayList<String>();
    
    String deploymentClaimId = repositoryService
    .createDeployment()
    .setName("Claim")
    .addResourceFromString("process.jpdl.xml", 
      "<process name='claim'>" +
      "  <start>" +
      "    <transition to='c' />" +
      "  </start>" +
      "  <state name='c' />" +
      "</process>"
    )
    .deploy();

    String deploymentHireId = repositoryService
      .createDeployment()
      .setName("Hire")
      .addResourceFromString("process.jpdl.xml", 
        "<process name='hire'>" +
        "  <start>" +
        "    <transition to='h' />" +
        "  </start>" +
        "  <state name='h' />" +
        "</process>"
      )
      .deploy();
    
    String deploymentFireId = repositoryService
    .createDeployment()
    .setName("Fire")
    .addResourceFromString("process.jpdl.xml", 
      "<process name='fire'>" +
      "  <start>" +
      "    <transition to='f' />" +
      "  </start>" +
      "  <state name='f' />" +
      "</process>"
    )
    .deploy();
    
    processIds.add(deploymentClaimId);
    processIds.add(deploymentFireId);
    processIds.add(deploymentHireId);
    
    return processIds;
  }
  
  private void testOrderBy(String property, List expectedValues) {
    testOrderBy(property, expectedValues, null, false);
  }

  private void testOrderByNaturalOrdening(String property, int expectedNrOfResults) {
    testOrderBy(property, null, expectedNrOfResults, true);
  }
  
  @SuppressWarnings("unchecked")
  private void testOrderBy(String property, List expectedValues, 
          Integer expectedNrOfResults, boolean naturalOrderCheck) {
    
    List<String> deploymentIds = deployTestProcesses();
    
    List<Deployment> deploymentsAsc = 
      repositoryService.createDeploymentQuery().orderAsc(property).list();
    
    List<Deployment> deploymentsDesc = 
      repositoryService.createDeploymentQuery().orderDesc(property).list();

    if (naturalOrderCheck) {
      QueryAssertions.assertOrderIsNatural(Deployment.class, property, deploymentsAsc, deploymentsDesc, 3);      
    } else {
      QueryAssertions.assertOrderOnProperty(Deployment.class, property, deploymentsAsc, deploymentsDesc, expectedValues);
    }
    
    deleteCascade(deploymentIds);
  }
  
}
