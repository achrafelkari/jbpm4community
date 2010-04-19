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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jbpm.api.Deployment;
import org.jbpm.api.JbpmException;
import org.jbpm.api.ProcessDefinition;
import org.jbpm.api.ProcessInstance;
import org.jbpm.api.ProcessInstanceQuery;
import org.jbpm.test.JbpmTestCase;


public class ProcessInstanceQueryTest extends JbpmTestCase {
  
  private static final String TEST_PROCESS_1_KEY = "testProcess1";
  
  private static final String TEST_PROCESS_1 = 
    "<process name='" + TEST_PROCESS_1_KEY + "'>" +
    "  <start>" +
    "    <transition to='a' />" +
    "  </start>" +
    "  <state name='a'>" +
    "    <transition to='end' />" +
    "  </state>" +
    "  <end name='end' />" +
    "</process>";
  
  private static final String TEST_PROCESS_2_KEY = "testProcess2";
  
  private static final String TEST_PROCESS_2 = 
    "<process name='" + TEST_PROCESS_2_KEY + "'>" +
    "  <start>" +
    "    <transition to='f' />" +
    "  </start>" +
    "  <fork name='f'>" +
    "    <transition to='assemble product' />" +
    "    <transition to='print documents' />" +
    "  </fork>" +
    "  <task name='assemble product' assignee='johndoe'>" +
    "    <transition to='j' />" +
    "  </task>" +
    "  <task name='print documents' assignee='johndoe'>" +
    "    <transition to='j' />" +
    "  </task>" +
    "  <join name='j'>" +
    "    <transition to='end' />" +
    "  </join>" +
    "  <end name='end' />" +
    "</process>";
  
  public void testQueryByProcessInstanceId() {
    Map<String, ArrayList<String>> processInstanceIds = startTestProcesses(3, 7);
    List<String> idsForTestProcess1 = processInstanceIds.get(TEST_PROCESS_1_KEY);
    List<String> idsForTestProcess2 = processInstanceIds.get(TEST_PROCESS_2_KEY);
    
    for (String processInstanceId : idsForTestProcess1) {
      ProcessInstance pi = executionService.createProcessInstanceQuery()
        .processInstanceId(processInstanceId).uniqueResult();
      assertNotNull(pi);
      
    }
    
    for (String processInstanceId : idsForTestProcess2) {
      ProcessInstance pi = executionService.createProcessInstanceQuery()
        .processInstanceId(processInstanceId).uniqueResult();
      assertNotNull(pi);
      
    }
  }
  
  public void testQueryByProcessInstanceKey() {
    startTestProcesses(7, 8);

    for (int i = 0; i < 7; i++) {
      ProcessInstance pi = executionService.createProcessInstanceQuery()
        .processInstanceKey(TEST_PROCESS_1_KEY + "-" + i).uniqueResult();
      assertNotNull(pi);
    }
    
    for (int i = 0; i < 8; i++) {
      ProcessInstance pi = executionService.createProcessInstanceQuery()
        .processInstanceKey(TEST_PROCESS_2_KEY + "-" + i).uniqueResult();
      assertNotNull(pi);
    }
    
  }
  
  public void testQueryByProcessDefinitionId() {
    startTestProcesses(10, 6);
    ProcessDefinition definitionProcess1 = 
      repositoryService.createProcessDefinitionQuery().processDefinitionKey(TEST_PROCESS_1_KEY).uniqueResult();
    ProcessDefinition definitionProcess2 = 
      repositoryService.createProcessDefinitionQuery().processDefinitionKey(TEST_PROCESS_2_KEY).uniqueResult();
    
    List<ProcessInstance> processInstances = 
      executionService.createProcessInstanceQuery().processDefinitionId(definitionProcess1.getId()).list();
    assertEquals(10, processInstances.size());
    
    processInstances = 
      executionService.createProcessInstanceQuery().processDefinitionId(definitionProcess2.getId()).list();
    assertEquals(6, processInstances.size());
  }
  
  public void testQueryBySuspended() {
    startTestProcesses(6, 0); // Don't start any instance of test process 2
    
    Deployment deployment = repositoryService.createDeploymentQuery().uniqueResult();
    repositoryService.suspendDeployment(deployment.getId());
    
    List<ProcessInstance> processInstances = executionService.createProcessInstanceQuery().suspended().list();
    assertEquals(6, processInstances.size());
  }
  
  public void testQueryByNotSuspended() {
    startTestProcesses(5, 5);
    
    ProcessInstanceQuery query = executionService.createProcessInstanceQuery().notSuspended();
    List<ProcessInstance> processInstances = query.list();
    assertEquals(10, processInstances.size());
    
    List<Deployment> deployments = repositoryService.createDeploymentQuery().list();
    assertEquals(2, deployments.size());
    repositoryService.suspendDeployment(deployments.get(0).getId());
    
    processInstances = query.list();
    assertEquals(5, processInstances.size());
  }
  
  public void testQueryByPage() {
    startTestProcesses(8, 12);
    
    for (int i = 0; i < 20; i += 2) {
      List<ProcessInstance> processInstances = executionService.createProcessInstanceQuery().page(i, 2).list();
      assertEquals(2, processInstances.size());
    }
  }
  
  public void testOrderByAscKey() {
    startTestProcesses(6, 10);
    
    List<ProcessInstance> processInstances = 
      executionService.createProcessInstanceQuery().orderAsc(ProcessInstanceQuery.PROPERTY_KEY).list();
    assertEquals(16, processInstances.size());
    
    for (int i = 0; i < processInstances.size(); i++) {
      
      if (i < 6 ) {
        assertTrue(processInstances.get(i).getKey().startsWith(TEST_PROCESS_1_KEY));
      } else {
        assertTrue(processInstances.get(i).getKey().startsWith(TEST_PROCESS_2_KEY));
      }
      
    }
  }
  
  public void testOrderByDescKey() {
    startTestProcesses(5, 9);
    
    List<ProcessInstance> processInstances = 
      executionService.createProcessInstanceQuery().orderDesc(ProcessInstanceQuery.PROPERTY_KEY).list();
    assertEquals(14, processInstances.size());
    
    for (int i = 0; i < processInstances.size(); i++) {
      
      if (i < 9 ) {
        assertTrue(processInstances.get(i).getKey().startsWith(TEST_PROCESS_2_KEY));
      } else {
        assertTrue(processInstances.get(i).getKey().startsWith(TEST_PROCESS_1_KEY));
      }
      
    }
  }

  public void testCount() {
    Map<String, ArrayList<String>> processInstanceIds = startTestProcesses(4, 6);
    List<String> idsForTestProcess1 = processInstanceIds.get(TEST_PROCESS_1_KEY);
    
    assertEquals(10, executionService.createProcessInstanceQuery().count());
    
    for (String processInstanceId : idsForTestProcess1) {
      assertEquals(1, executionService.createProcessInstanceQuery().processInstanceId(processInstanceId).count());
    }
    
    for (int i = 0; i < 6; i++) {
      assertEquals(1, executionService.createProcessInstanceQuery().processInstanceKey(TEST_PROCESS_2_KEY + "-" + i).count());
    }
    
    try {
      executionService.createProcessInstanceQuery().page(0, 3).count();
      fail("expected exception");
    } catch (JbpmException e) {  }
    
  }

  
  /**
   * Returns a map containing a processKey - processInstanceId mapping.
   * eg. TEST_PROCESS_1 - {"1", "5", "7", "8"}
   * 
   * Process instance will also have a business key of the form 'processDefKey-nr', where
   * nr is from 0 to the given nr of required processes.
   */
  private Map<String, ArrayList<String>> startTestProcesses(int nrOfTestProcess1, int nrOfTestProcess2) {
    if (nrOfTestProcess1 > 0) {
      deployJpdlXmlString(TEST_PROCESS_1);
    }
    if (nrOfTestProcess2 > 0) {
      deployJpdlXmlString(TEST_PROCESS_2);
    }
    
    Map<String, ArrayList<String>> result = new HashMap<String, ArrayList<String>>();
    result.put(TEST_PROCESS_1_KEY, new ArrayList<String>());
    result.put(TEST_PROCESS_2_KEY, new ArrayList<String>());
    
    for (int i = 0; i < nrOfTestProcess1; i++) {
      ProcessInstance pi = executionService.startProcessInstanceByKey(TEST_PROCESS_1_KEY, TEST_PROCESS_1_KEY + "-" + i);
      result.get(TEST_PROCESS_1_KEY).add(pi.getId());
    }
    

    for (int i = 0; i < nrOfTestProcess2; i++) {
      ProcessInstance pi = executionService.startProcessInstanceByKey(TEST_PROCESS_2_KEY, TEST_PROCESS_2_KEY + "-" + i);
      result.get(TEST_PROCESS_2_KEY).add(pi.getId());
    }
   
    return result;
  }

}
