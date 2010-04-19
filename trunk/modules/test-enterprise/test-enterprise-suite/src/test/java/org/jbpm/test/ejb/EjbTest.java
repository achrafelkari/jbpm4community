package org.jbpm.test.ejb;

import junit.framework.Test;

import org.apache.cactus.ServletTestSuite;
import org.jbpm.test.JbpmTestCase;

public class EjbTest extends JbpmTestCase {
  
  public static Test suite() {
    ServletTestSuite servletTestSuite = new ServletTestSuite();
    servletTestSuite.addTestSuite(EjbTest.class);
    return servletTestSuite;
  }
    
  protected void setUp() throws Exception {
    super.setUp();  
    registerDeployment(repositoryService.createDeployment()
        .addResourceFromClasspath("org/jbpm/test/ejb/process.jpdl.xml")
        .deploy());  
  }
  
  public void testEjbInvocation() throws Exception {
    String executionId = executionService
        .startProcessInstanceByKey("EJB")
        .getProcessInstance()
        .getId();
    assertEquals(63, executionService.getVariable(executionId, "answer"));
  }
}
