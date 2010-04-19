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
package org.jbpm.test.deploy;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jbpm.api.Execution;
import org.jbpm.api.ProcessDefinition;
import org.jbpm.api.ProcessInstance;
import org.jbpm.test.JbpmTestCase;


/**
 * @author Tom Baeyens
 */
public class SuspendDeploymentTest extends JbpmTestCase {

  public void testSuspendDeployment() {
    deployJpdlXmlString(
      "<process name='claim'>" +
      "  <start>" +
      "    <transition to='c' />" +
      "  </start>" +
      "  <state name='c' />" +
      "</process>"
    );

    String deploymentHireId = deployJpdlXmlString(
      "<process name='hire'>" +
      "  <start>" +
      "    <transition to='h' />" +
      "  </start>" +
      "  <state name='h' />" +
      "</process>"
    );

    deployJpdlXmlString(
      "<process name='fire'>" +
      "  <start>" +
      "    <transition to='f' />" +
      "  </start>" +
      "  <state name='f' />" +
      "</process>"
    );
    
    Set<String> expectedProcessNames = new HashSet<String>();
    expectedProcessNames.add("claim");
    expectedProcessNames.add("hire");
    expectedProcessNames.add("fire");
    
    List<ProcessDefinition> processDefinitions = repositoryService
      .createProcessDefinitionQuery()
      .list();
    
    assertEquals(expectedProcessNames, getProcessDefinitionNames(processDefinitions));

    executionService.startProcessInstanceByKey("claim");
    executionService.startProcessInstanceByKey("claim");
    
    executionService.startProcessInstanceByKey("hire");
    executionService.startProcessInstanceByKey("hire");
    
    executionService.startProcessInstanceByKey("fire");
    executionService.startProcessInstanceByKey("fire");
    
    List<ProcessInstance> processInstances = executionService
      .createProcessInstanceQuery()
      .list();
    
    assertEquals(2, countProcessInstancesFor(processInstances, "claim"));
    assertEquals(2, countProcessInstancesFor(processInstances, "hire"));
    assertEquals(2, countProcessInstancesFor(processInstances, "fire"));
    assertEquals(6, processInstances.size());


    repositoryService.suspendDeployment(deploymentHireId);


    processDefinitions = repositoryService
      .createProcessDefinitionQuery()
      .list();

    assertEquals(expectedProcessNames, getProcessDefinitionNames(processDefinitions));

    processInstances = executionService
      .createProcessInstanceQuery()
      .notSuspended()
      .list();
    
    assertEquals(2, countProcessInstancesFor(processInstances, "claim"));
    assertEquals(2, countProcessInstancesFor(processInstances, "fire"));
    assertEquals(4, processInstances.size());
    
    repositoryService.resumeDeployment(deploymentHireId);

    expectedProcessNames.add("hire");

    processDefinitions = repositoryService
      .createProcessDefinitionQuery()
      .list();
  
    assertEquals(expectedProcessNames, getProcessDefinitionNames(processDefinitions));
    
    processInstances = executionService
      .createProcessInstanceQuery()
      .list();
    
    assertEquals(2, countProcessInstancesFor(processInstances, "claim"));
    assertEquals(2, countProcessInstancesFor(processInstances, "hire"));
    assertEquals(2, countProcessInstancesFor(processInstances, "fire"));
    assertEquals(6, processInstances.size());
  }

  public void testQuerySuspendDeployments() {
    deployJpdlXmlString(
      "<process name='claim'>" +
      "  <start>" +
      "    <transition to='c' />" +
      "  </start>" +
      "  <state name='c' />" +
      "</process>"
    );

    String deploymentHireId = deployJpdlXmlString(
      "<process name='hire'>" +
      "  <start>" +
      "    <transition to='h' />" +
      "  </start>" +
      "  <state name='h' />" +
      "</process>"
    );

    String deploymentFireId = deployJpdlXmlString(
      "<process name='fire'>" +
      "  <start>" +
      "    <transition to='f' />" +
      "  </start>" +
      "  <state name='f' />" +
      "</process>"
    );
    
    repositoryService.suspendDeployment(deploymentHireId);
    repositoryService.suspendDeployment(deploymentFireId);

    List<ProcessDefinition> processDefinitions = repositoryService
      .createProcessDefinitionQuery()
      .suspended()
      .list();
    
    Set<String> expectedProcessNames = new HashSet<String>();
    expectedProcessNames.add("hire");
    expectedProcessNames.add("fire");

    assertEquals(expectedProcessNames, getProcessDefinitionNames(processDefinitions));
  }

  public void testQueryNonSuspendProcessDefinitions() {
    deployJpdlXmlString(
      "<process name='claim'>" +
      "  <start>" +
      "    <transition to='c' />" +
      "  </start>" +
      "  <state name='c' />" +
      "</process>"
    );

    String deploymentHireId = deployJpdlXmlString(
      "<process name='hire'>" +
      "  <start>" +
      "    <transition to='h' />" +
      "  </start>" +
      "  <state name='h' />" +
      "</process>"
    );

    String deploymentFireId = deployJpdlXmlString(
      "<process name='fire'>" +
      "  <start>" +
      "    <transition to='f' />" +
      "  </start>" +
      "  <state name='f' />" +
      "</process>"
    );
    
    repositoryService.suspendDeployment(deploymentHireId);

    List<ProcessDefinition> processDefinitions = repositoryService
      .createProcessDefinitionQuery()
      .notSuspended()
      .list();
    
    Set<String> expectedProcessNames = new HashSet<String>();
    expectedProcessNames.add("claim");
    expectedProcessNames.add("fire");

    assertEquals(expectedProcessNames, getProcessDefinitionNames(processDefinitions));
  }

  int countProcessInstancesFor(List<ProcessInstance> processInstances, String processDefinitionKey) {
    int count = 0;
    for (Execution processInstance: processInstances) {
      if (processInstance.getProcessDefinitionId().startsWith(processDefinitionKey)) {
        count++;
      }
    }
    return count;
  }

  Object getProcessDefinitionNames(List<ProcessDefinition> processDefinitions) {
    Set<String> processNames = new HashSet<String>();
    for (ProcessDefinition processDefinition: processDefinitions) {
      processNames.add(processDefinition.getName());
    }
    return processNames;
  }
}
