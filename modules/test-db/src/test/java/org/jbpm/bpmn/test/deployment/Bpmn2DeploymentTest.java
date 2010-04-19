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
package org.jbpm.bpmn.test.deployment;

import java.util.List;

import org.jbpm.api.JbpmException;
import org.jbpm.api.ProcessDefinition;
import org.jbpm.api.ProcessDefinitionQuery;
import org.jbpm.api.ProcessInstance;
import org.jbpm.test.JbpmTestCase;
import org.jbpm.test.assertion.CollectionAssertions;


/**
 * @author Joram Barrez
 */
public class Bpmn2DeploymentTest extends JbpmTestCase {
  
  private static final String TEST_PROCESS_ONLY_ID = 
    "<definitions>" +
    "  <process id='myProcess' >" +
    "    <startEvent id='start'/>" +
    "    <sequenceFlow id='flow1' sourceRef='start' targetRef='end' />" +
    "    <endEvent id='end'/>" +
    "  </process>" +
    "</definitions>";
  
  private static final String TEST_PROCESS_ONLY_NAME = 
    "<definitions>" +
    "  <process name='myProcess' >" +
    "    <startEvent id='start'/>" +
    "    <sequenceFlow id='flow1' sourceRef='start' targetRef='end' />" +
    "    <endEvent id='end'/>" +
    "  </process>" +
    "</definitions>";
  
  private static final String TEST_PROCESS_ID_AND_NAME = 
    "<definitions>" +
    "  <process id='myProcess' name='myFirstProcess'>" +
    "    <startEvent id='start'/>" +
    "    <sequenceFlow id='flow1' sourceRef='start' targetRef='end' />" +
    "    <endEvent id='end'/>" +
    "  </process>" +
    "</definitions>";
  
  public void testDeployProcessWithOnlyName() {
    try {
      deployBpmn2XmlString(TEST_PROCESS_ONLY_NAME);
      fail();
    } catch (JbpmException e) {
      // Exception is to be expected
    }
  }
  
  public void testDeployProcessWithOnlyId() {
    deployBpmn2XmlString(TEST_PROCESS_ONLY_ID);
    ProcessInstance processInstance = executionService.startProcessInstanceByKey("myProcess");
    assertNotNull(processInstance);
    assertProcessInstanceEnded(processInstance);
  }
  
  public void testDeployProcessWithIdAndName() {
    deployBpmn2XmlString(TEST_PROCESS_ID_AND_NAME);
    
    ProcessInstance processInstance = executionService.startProcessInstanceByKey("myFirstProcess");
    assertNotNull(processInstance);
    assertProcessInstanceEnded(processInstance);
    
    try {
      executionService.startProcessInstanceByKey("myProcess");
      fail();
    } catch (JbpmException e) {
      assertTrue(e.getMessage().contains("no process definition with key"));
      // exception expected: when a key is given, the id can't be used as a key
    }
  }
  
  /* ------------
   * REDEPLOYMENT
   * ------------
   */
  
  public void testRedeployProcessWithOnlyId() {
    deployBpmn2XmlString(TEST_PROCESS_ONLY_ID);
    
    ProcessDefinitionQuery query = repositoryService.createProcessDefinitionQuery().processDefinitionKey("myProcess");
    List<ProcessDefinition> definitions = query.list();
    assertEquals(1, definitions.size());
    
    deployBpmn2XmlString(TEST_PROCESS_ONLY_ID);
    query.orderAsc(ProcessDefinitionQuery.PROPERTY_VERSION);
    definitions = query.list();
    
    assertEquals(2, definitions.size());
    assertEquals(1, definitions.get(0).getVersion());
    assertEquals(2, definitions.get(1).getVersion());
    
    // Check if the key is replaced with the name (query should give same result)
    List<ProcessDefinition> definitionsByName = repositoryService.createProcessDefinitionQuery()
                                                         .processDefinitionName("myProcess").list();
    CollectionAssertions.assertContainsSameElements(definitions, definitionsByName);
  }
  
  public void testRedeployProcessWithIdAndName() {
    deployBpmn2XmlString(TEST_PROCESS_ID_AND_NAME);
    
    ProcessDefinitionQuery query = repositoryService.createProcessDefinitionQuery().processDefinitionName("myProcess");
    List<ProcessDefinition> definitions = query.list();
    assertEquals(1, definitions.size());
    
    deployBpmn2XmlString(TEST_PROCESS_ID_AND_NAME);
    query.orderAsc(ProcessDefinitionQuery.PROPERTY_VERSION);
    definitions = query.list();
    
    assertEquals(2, definitions.size());
    assertEquals(1, definitions.get(0).getVersion());
    assertEquals(2, definitions.get(1).getVersion());
    
    // Check if the key is replaced with the name (query should give same result)
    List<ProcessDefinition> definitionsByKey = repositoryService.createProcessDefinitionQuery()
                                                         .processDefinitionKey("myFirstProcess").list();
    CollectionAssertions.assertContainsSameElements(definitions, definitionsByKey);
  }
  
}
