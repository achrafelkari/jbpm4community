package org.jbpm.bpmn;

import org.jbpm.test.JbpmTestCase;

/**
 * Test for the most basic process there is: just a start and end.
 * 
 * @author Joram Barrez
 */
public class SimplestProcessTest extends JbpmTestCase {
  
  private static final String PROCESS_NAME = "simplestProcess";
  
  private static final String PROCESS_LOCATION = "org/jbpm/bpmn/simplestProcess.bpmn.xml";
  
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    registerDeployment(repositoryService.createDeployment()
            .addResourceFromClasspath(PROCESS_LOCATION).deploy());        
  }
  
  public void testProcessStart() {
    executionService.startProcessInstanceByKey(PROCESS_NAME);
  }

}
