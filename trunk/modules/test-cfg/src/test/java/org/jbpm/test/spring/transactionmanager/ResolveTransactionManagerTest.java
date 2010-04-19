package org.jbpm.test.spring.transactionmanager;

import org.jbpm.api.ExecutionService;
import org.jbpm.api.ProcessEngine;
import org.jbpm.api.ProcessInstance;
import org.jbpm.api.RepositoryService;
import org.jbpm.test.AbstractTransactionalSpringJbpmTestCase;
import org.springframework.test.AbstractDependencyInjectionSpringContextTests;

/**
 * Note: cannot use {@link AbstractTransactionalSpringJbpmTestCase}, since this autowires
 * by name and expects a 'transactionManager' bean to be defined, which is exactly what we don't
 * want here.
 * 
 * Test case created because of JBPM-2558: when using multiple transaction managers in Spring,
 * it is required to be able to declare which transaction manager should be used.
 * 
 * @author Joram Barrez
 */
public class ResolveTransactionManagerTest extends AbstractDependencyInjectionSpringContextTests {
  
  protected ProcessEngine processEngine;
  
  private ExecutionService executionService;
  
  private RepositoryService repositoryService;
  
  
  public ResolveTransactionManagerTest() {
    setPopulateProtectedVariables(true);
  }
  
  @Override
  protected void onSetUp() throws Exception {
    super.onSetUp();
    this.executionService = processEngine.getExecutionService();
    this.repositoryService = processEngine.getRepositoryService();
  }
  
  @Override
  protected String getConfigPath() {
    return "applicationContext.xml";
  }
  
  public void testGetTransactionManagerByName() {
    assertNotNull(processEngine.get("someOtherTransactionManager"));
    assertNotNull(processEngine.get("transactionManager"));
  }
  
  // Execute a simple process to verify the correct working of the transactionManager
  public void testExecuteSimpleProcess() {
    
    String deployId = repositoryService.createDeployment()
      .addResourceFromClasspath("org/jbpm/test/spring/transactionmanager/process.jpdl.xml")
      .deploy();
    
    ProcessInstance pi = executionService.startProcessInstanceByKey("simpleProcess");
    pi = executionService.signalExecutionById(pi.findActiveExecutionIn("a").getId());
    pi = executionService.signalExecutionById(pi.findActiveExecutionIn("b").getId());
    assertTrue(executionService.findProcessInstanceById(pi.getId()) == null);
    
    repositoryService.deleteDeploymentCascade(deployId);
  } 

}
