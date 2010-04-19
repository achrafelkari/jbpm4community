package org.jbpm.test.upgrade;

import java.util.List;

import org.jbpm.api.Execution;
import org.jbpm.api.ProcessDefinition;
import org.jbpm.api.ProcessInstance;
import org.jbpm.api.history.HistoryProcessInstance;
import org.jbpm.test.JbpmTestCase;

/**
 * We cannot use the {@link JbpmTestCase} directly, since it will check if the
 * database is clean. However, in the upgrade test, we expect to have data in
 * the database that was put there before the upgrade. So this class adapts the
 * {@link JbpmTestCase} where needed, so that the database is not checked to be
 * clean.
 * 
 * @author jbarrez
 */
public abstract class AfterUpgradeJbpmTestCase extends JbpmTestCase {

  protected void tearDown() throws Exception {
    // Do nothing -> the super.tearDown() will check the database to be
    // clean, which now isn't happening due to the override.
  }

  /**
   * Implementations must test here if the data that was produced before 
   * the upgrade still is valid after the upgrade.   * 
   */
  public abstract void testDataValid();
  
  /**
   * Implementations must test here if process instances that were in a given
   * state before the upgrade, still can be continued/finished after the upgrade.
   */
  public abstract void testContinueExistingInstance();
  
  /**
   * Implementations must test here if new process instances can be created
   * starting from a deployment done before the upgrade.
   */
  public abstract void testStartFreshProcessInstance();

  /*
   * -------------- HELPER METHODS --------------
   */

  protected ProcessInstance findProcessInstanceByKey(String key) {
    return executionService.createProcessInstanceQuery()
                           .processInstanceKey(key).uniqueResult();
  }

  protected HistoryProcessInstance findHistoryProcessInstanceByKey(String key) {
    return historyService.createHistoryProcessInstanceQuery()
                         .processInstanceKey(key).uniqueResult();
  }
  
  protected List<HistoryProcessInstance> findEndedProcessInstancesByProcDef(String processDefinitionId){
    return historyService.createHistoryProcessInstanceQuery()
                         .state(Execution.STATE_ENDED)
                         .processDefinitionId(processDefinitionId).list();
  }
  
  protected List<ProcessDefinition> findProcessDefinitionsByKey(String key) {
    return repositoryService.createProcessDefinitionQuery().processDefinitionKey(key).list();
  }
  
  protected ProcessDefinition findProcessDefinitionByKey(String key) {
    return repositoryService.createProcessDefinitionQuery().processDefinitionKey(key).uniqueResult();
  }
  
  protected List<ProcessInstance> findProcessInstancesByProcessDefinition(String procDefId) {
    return executionService.createProcessInstanceQuery().processDefinitionId(procDefId).list();
  }

}
