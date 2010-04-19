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
package org.jbpm.test.process;

import java.util.List;

import org.jbpm.api.Execution;
import org.jbpm.api.JbpmException;
import org.jbpm.api.ProcessDefinition;
import org.jbpm.api.ProcessDefinitionQuery;
import org.jbpm.api.ProcessInstance;
import org.jbpm.test.JbpmTestCase;


/**
 * @author Tom Baeyens
 */
public class RepositoryServiceTest extends JbpmTestCase {

  public void testProcessWithNameOnly() {
    deployJpdlXmlString(
      "<process name='Insurance claim'>" +
      "  <start />" +
      "</process>"
    );
    
    ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery()
        .processDefinitionKey("Insurance_claim")
        .orderDesc(ProcessDefinitionQuery.PROPERTY_VERSION)
        .page(0,1)
        .uniqueResult();

    assertNotNull(processDefinition);
    assertEquals("Insurance claim", processDefinition.getName());
    assertEquals("Insurance_claim", processDefinition.getKey());
    assertEquals(1, processDefinition.getVersion());
    assertEquals("Insurance_claim-1", processDefinition.getId());
  }

  public void testProcessWithNameAndKey() {
    deployJpdlXmlString(
      "<process name='Insurance claim' key='ICL'>" +
      "  <start />" +
      "</process>"
    );

    ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery()
        .processDefinitionKey("ICL")
        .orderDesc(ProcessDefinitionQuery.PROPERTY_VERSION)
        .page(0,1)
        .uniqueResult();
    
    assertNotNull(processDefinition);
    assertEquals("Insurance claim", processDefinition.getName());
    assertEquals("ICL", processDefinition.getKey());
    assertEquals(1, processDefinition.getVersion());
    assertEquals("ICL-1", processDefinition.getId());
  }

  // interface methods ////////////////////////////////////////////////////////

  public void testFindProcessByKey() {
    deployJpdlXmlString(
      "<process name='Name with spaces'>" +
      "  <start />" +
      "</process>"
    );

    ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery()
        .processDefinitionKey("Name_with_spaces")
        .orderDesc(ProcessDefinitionQuery.PROPERTY_VERSION)
        .page(0,1)
        .uniqueResult();

    assertNotNull(processDefinition);
    assertEquals("Name with spaces", processDefinition.getName());
    assertEquals("Name_with_spaces", processDefinition.getKey());
    assertEquals(1, processDefinition.getVersion());
    assertEquals("Name_with_spaces-1", processDefinition.getId());
  }

  public void testFindProcessDefinitions() {
    deployMultipleVersionsOfProcesses();

    List<ProcessDefinition> processDefinitions = repositoryService.createProcessDefinitionQuery()
      .processDefinitionKey("nuclear_fusion")
      .orderDesc(ProcessDefinitionQuery.PROPERTY_VERSION)
      .list();
    assertNotNull(processDefinitions);

    assertEquals("expected 3 but was " + processDefinitions.size() + ": " + processDefinitions, 3, processDefinitions.size());
    assertEquals("nuclear fusion", processDefinitions.get(0).getName());
    assertEquals(3, processDefinitions.get(0).getVersion());

    assertEquals("nuclear fusion", processDefinitions.get(1).getName());
    assertEquals(2, processDefinitions.get(1).getVersion());

    assertEquals("nuclear fusion", processDefinitions.get(2).getName());
    assertEquals(1, processDefinitions.get(2).getVersion());
  }

  public void testFindLatestProcessDefinition() {
    deployMultipleVersionsOfProcesses();

    ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery()
        .processDefinitionKey("nuclear_fusion")
        .orderDesc(ProcessDefinitionQuery.PROPERTY_VERSION)
        .page(0,1)
        .uniqueResult();
    assertNotNull(processDefinition);

    assertEquals(3, processDefinition.getVersion());
    assertEquals("nuclear fusion", processDefinition.getName());
    assertEquals("nuclear_fusion", processDefinition.getKey());
  }

  public void testFindProcessDefinitionById() {
    deployJpdlXmlString(
      "<process name='given' version='33'>" +
      "  <start />" +
      "</process>"
    );

    // load it
    ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery()
        .processDefinitionId("given-33")
        .uniqueResult();

    assertNotNull(processDefinition);
    assertEquals("given", processDefinition.getName());
    assertEquals(33, processDefinition.getVersion());
  }

  public void testDeleteDeployment() {
    String deploymentDbid = repositoryService.createDeployment()
        .addResourceFromString("xmlstring.jpdl.xml", 
            "<process name='deleteme' version='33'>" +
            "  <start />" +
            "</process>")
        .deploy();

    // delete it
    repositoryService.deleteDeployment(deploymentDbid);

    // check if the db is empty
    assertEquals(0, repositoryService.createProcessDefinitionQuery().list().size());
  }

  public void testDeleteProcessDefinitionAndInstances() {
    String deploymentDbid = repositoryService.createDeployment()
        .addResourceFromString("xmlstring.jpdl.xml", 
          "<process name='deleteme' version='33'>" +
          "  <start>" +
          "    <transition to='w' />" +
          "  </start>" +
          "  <state name='w' />" +
          "</process>")
        .deploy();
    
    executionService.startProcessInstanceByKey("deleteme");
    executionService.startProcessInstanceByKey("deleteme");
    executionService.startProcessInstanceByKey("deleteme");
    executionService.startProcessInstanceByKey("deleteme");
    
    // delete it all
    repositoryService.deleteDeploymentCascade(deploymentDbid);

    // check if the db is empty
    assertEquals(0, repositoryService.createProcessDefinitionQuery().list().size());
    assertEquals(0, executionService.createProcessInstanceQuery().list().size());
  }
  
  public void testDeleteProcessDefinitionButNotInstances() {
    String deploymentDbid = deployJpdlXmlString(
      "<process name='deleteme' version='33'>" +
      "  <start>" +
      "    <transition to='w' />" +
      "  </start>" +
      "  <state name='w' />" +
      "</process>"
    );
    
    executionService.startProcessInstanceByKey("deleteme");
    executionService.startProcessInstanceByKey("deleteme");
    executionService.startProcessInstanceByKey("deleteme");
    executionService.startProcessInstanceByKey("deleteme");
    
    // delete it all
    try {
      repositoryService.deleteDeployment(deploymentDbid);
      fail("expected exception");
    } catch (JbpmException e) {
      assertTextPresent("cannot delete deployment", e.getMessage());
      assertTextPresent("still executions for process(deleteme): ", e.getMessage());
    }
  }
  
  // various other aspects ////////////////////////////////////////////////////
  
  public void testAutomaticVersioning() {
    deployJpdlXmlString(
      "<process name='versionme'>" +
      "  <start />" +
      "</process>"
    );

    // look it up again
    List<ProcessDefinition> processDefinitions = repositoryService.createProcessDefinitionQuery()
        .processDefinitionKey("versionme")
        .orderDesc(ProcessDefinitionQuery.PROPERTY_VERSION)
        .list();
    
    assertNotNull(processDefinitions);
    // verify that there is only one
    assertEquals(processDefinitions.toString(), 1, processDefinitions.size());
    ProcessDefinition processDefinition = processDefinitions.get(0);
    // and check that automatically assigned version starts with 1
    assertEquals(1, processDefinition.getVersion());

    deployJpdlXmlString(
      "<process name='versionme'>" +
      "  <start />" +
      "</process>"
    );

    // look them up again
    processDefinitions = repositoryService.createProcessDefinitionQuery()
        .processDefinitionKey("versionme")
        .orderDesc(ProcessDefinitionQuery.PROPERTY_VERSION)
        .list();
    
    // verify that there is only one
    assertEquals(processDefinitions.toString(), 2, processDefinitions.size());
    // and check that automatically assigned version starts with 1
    assertEquals(2, processDefinitions.get(0).getVersion());
    assertEquals(1, processDefinitions.get(1).getVersion());
  }

  public void testUserProvidedVersion() {
    deployJpdlXmlString(
      "<process name='takethis' version='234'>" +
      "  <start />" +
      "</process>"
    );

    // load it
    List<ProcessDefinition> processDefinitions = repositoryService.createProcessDefinitionQuery()
        .processDefinitionKey("takethis")
        .list();
    
    assertNotNull(processDefinitions);
    assertEquals(processDefinitions.toString(), 1, processDefinitions.size());
    ProcessDefinition processDefinition = processDefinitions.get(0);
    // verify that the user specified version was used
    // (and not overwritten by an automatically assigned versioning)
    assertEquals(234, processDefinition.getVersion());
  }

  public void testDuplicateUserProvidedVersion()  {
    deployJpdlXmlString(
      "<process name='takethis' version='234'>" +
      "  <start />" +
      "</process>"
    );

    try {
      deployJpdlXmlString(
        "<process name='takethis' version='234'>" +
        "  <start />" +
        "</process>"
      );
      fail("expected exception");
    } catch (JbpmException e) {
      assertTextPresent("process 'takethis-234' already exists", e.getMessage());
    }
  }

  /**
   * deploys 3 versions of process with name 'nuclear fusion', 2 versions of the processes 'ultimate seduction' and
   * 'publish book'
   */
  void deployMultipleVersionsOfProcesses() {
    deployJpdlXmlString(
      "<process name='nuclear fusion'>" +
      "  <start />" +
      "</process>"
    );

    deployJpdlXmlString(
      "<process name='ultimate seduction'>" +
      "  <start />" +
      "</process>"
    );

    deployJpdlXmlString(
      "<process name='ultimate seduction'>" +
      "  <start />" +
      "</process>"
    );

    deployJpdlXmlString(
      "<process name='ultimate seduction'>" +
      "  <start />" +
      "</process>"
    );

    deployJpdlXmlString(
      "<process name='ultimate seduction'>" +
      "  <start />" +
      "</process>"
    );

    deployJpdlXmlString(
      "<process name='publish book'>" +
      "  <start />" +
      "</process>"
    );

    deployJpdlXmlString(
      "<process name='nuclear fusion'>" +
      "  <start />" +
      "</process>"
    );

    deployJpdlXmlString(
      "<process name='nuclear fusion'>" +
      "  <start />" +
      "</process>"
    );
  }

  public void testMinimalProcess() {
    deployJpdlXmlString(
      "<process name='minimal'>" +
      "  <start>" +
      "    <transition to='end' />" +
      "  </start>" +
      "  <end name='end' />" +
      "</process>"
    );
    
    ProcessInstance processInstance = executionService.startProcessInstanceByKey("minimal");
    
    assertTrue(processInstance.isEnded());
  }

  public void testMostMinimalProcess() {
    deployJpdlXmlString(
      "<process name='minimal'>" +
      "  <start />" +
      "</process>"
    );

    Execution execution = executionService.startProcessInstanceByKey("minimal");
    
    assertTrue(execution.isEnded());
  }
}
