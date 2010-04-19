/**
 * 
 */
package org.jbpm.test;

import java.io.IOException;

import org.jbpm.api.Configuration;
import org.jbpm.api.ExecutionService;
import org.jbpm.api.HistoryService;
import org.jbpm.api.IdentityService;
import org.jbpm.api.ManagementService;
import org.jbpm.api.ProcessEngine;
import org.jbpm.api.RepositoryService;
import org.jbpm.api.TaskService;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.AbstractTransactionalDataSourceSpringContextTests;

/**
 * Convenient superclass for tests of processes that should occur in a
 * transaction, but normally will roll the transaction back on the completion of
 * each test.
 * 
 * It contains some convenience methods: 
 * - Deploying XML
 * - Deploying from classpath
 * 
 * Exposes all relevant services as protected fields.
 * 
 * 
 * @author Andries Inze
 * @see AbstractTransactionalDataSourceSpringContextTests
 * 
 */
public abstract class AbstractTransactionalSpringJbpmTestCase extends AbstractTransactionalDataSourceSpringContextTests {

  private Configuration configuration;
  protected ProcessEngine processEngine;

  protected RepositoryService repositoryService;
  protected ExecutionService executionService;
  protected ManagementService managementService;
  protected TaskService taskService;
  protected HistoryService historyService;
  protected IdentityService identityService;

  /**
   * Creates a new instance. Will require a transactionManager.
   */
  public AbstractTransactionalSpringJbpmTestCase() {
    super();

    // AUTOWIRE_BY_NAME is default behavior for Spring version 2.5.x
    // AUTOWIRE_BY_TYPE is default behavior for Spring version 2.0.8, but
    // fails because of Hibernate specific instances.
    setAutowireMode(AUTOWIRE_BY_NAME);
  }

  /**
   * {@inheritDoc)
   */
  protected void injectDependencies() throws Exception {
    super.injectDependencies();

    //configuration = (Configuration) applicationContext.getBean(getJbpmConfigurationName());
    processEngine = (ProcessEngine) applicationContext.getBean("processEngine");
    //processEngine = configuration.buildProcessEngine();

    repositoryService = processEngine.get(RepositoryService.class);
    executionService = processEngine.getExecutionService();
    historyService = processEngine.getHistoryService();
    managementService = processEngine.getManagementService();
    taskService = processEngine.getTaskService();
    identityService = processEngine.getIdentityService();
  }
  
  /**
   * By default, the applicationContext.xml file in the same package as the test class
   * is used to initialize the Spring container.
   * 
   * Override this method if you don't need this default behaviour.
   */
  @Override
  protected String getConfigPath() {
    return "applicationContext.xml";
  }

  /**
   * Default configuration name. Overwrite this if the jbpm configuration is
   * named different.
   * 
   * @return the jbpmConfigurationName
   */
  protected String getJbpmConfigurationName() {
    return "jbpmConfiguration";
  }

  /**
   * deploys the process.
   */
  public String deployJpdlXmlString(String jpdlXmlString) {
    String deploymentId = repositoryService.createDeployment().addResourceFromString("xmlstring.jpdl.xml", jpdlXmlString).deploy();
    return deploymentId;
  }

  /**
   * deploys the process.
   */
  public String deployJpdlFromClasspath(String jpdlXmlString) {
    String deploymentId;
    try {
      deploymentId = repositoryService.createDeployment().addResourceFromInputStream("xmlstring.jpdl.xml",
              new ClassPathResource(jpdlXmlString).getInputStream()).deploy();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    return deploymentId;
  }
}
