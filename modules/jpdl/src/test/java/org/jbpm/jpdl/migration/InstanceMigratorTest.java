package org.jbpm.jpdl.migration;

import java.util.List;

import org.jbpm.api.Execution;
import org.jbpm.api.ProcessDefinition;
import org.jbpm.api.ProcessInstance;
import org.jbpm.api.history.HistoryDetail;
import org.jbpm.api.task.Task;
import org.jbpm.pvm.internal.history.model.HistoryProcessInstanceMigrationImpl;
import org.jbpm.test.JbpmTestCase;


public class InstanceMigratorTest extends JbpmTestCase {
  
  private String firstVersion = 
    "<process name='foobar'>" +
    "  <swimlane name='phelps' assignee='fredje'/>" +
    "  <start>" +
    "    <transition to='foo'/>" +
    "  </start>" +
    "  <state name='foo'>" +
    "    <transition to='bar'/>" +
    "  </state>" +
    "  <task name='bar' swimlane='phelps'>" +
    "    <transition to='end'/>" +
    "  </task>" +
    "  <end name='end'/>" +
    "</process>";
  
  private String secondVersion = 
    "<process name='foobar'>" +
    "  <swimlane name='baltimore bullet' assignee='fredje'/>" +
    "  <start>" +
    "    <transition to='foo'/>" +
    "  </start>" +
    "  <state name='foo'>" +
    "    <transition to='fu'/>" +
    "  </state>" +
    "  <task name='fu' swimlane='baltimore bullet'>" +
    "    <transition name='to end' to='end'/>" +
    "    <transition name='to baz' to='baz'/>" +
    "  </task>" +
    "  <task name='baz' swimlane='baltimore bullet'>" +
    "    <transition to='end'/>" +
    "  </task>" +
    "  <end name='end'/>" +
    "  <migrate-instances>" +
    "    <activity-mapping old-name='bar' new-name='fu'/>" +
    "  </migrate-instances>" +
    "</process>";
    
  public void testHistoryProcessInstanceMigration() {
    String deploymentId1 = repositoryService.createDeployment()
        .addResourceFromString("foobar.jpdl.xml", firstVersion)
        .deploy();
    ProcessDefinition processDefinition1 = repositoryService
        .createProcessDefinitionQuery()
        .deploymentId(deploymentId1)
        .uniqueResult();
    Execution execution = executionService
        .startProcessInstanceById(processDefinition1.getId())
        .findActiveExecutionIn("foo");
    executionService.signalExecutionById(execution.getId());
    
    String deploymentId2 = repositoryService.createDeployment()
        .addResourceFromString("foobar.jpdl.xml", secondVersion)
        .deploy();
    ProcessDefinition processDefinition2 = repositoryService
        .createProcessDefinitionQuery()
        .deploymentId(deploymentId2)
        .uniqueResult();
    ProcessInstance processInstance = executionService
        .createProcessInstanceQuery()
        .processDefinitionId(processDefinition2.getId())
        .uniqueResult();
    
    List<HistoryDetail> historyDetails = historyService
        .createHistoryDetailQuery()
        .processInstanceId(processInstance.getId())
        .list();
    HistoryProcessInstanceMigrationImpl historyProcessInstanceMigration = null;
    for (HistoryDetail historyDetail : historyDetails) {
      if (historyDetail instanceof HistoryProcessInstanceMigrationImpl) {
        historyProcessInstanceMigration = (HistoryProcessInstanceMigrationImpl)historyDetail;
      }
    }
    
    assertNotNull(historyProcessInstanceMigration);
    
    repositoryService.deleteDeploymentCascade(deploymentId2);
    repositoryService.deleteDeploymentCascade(deploymentId1);
    
  }

  public void testSwimlaneMigration() {
    
    identityService.createUser("fredje", "Frederik", "Deburghgraeve");
    
    String deploymentId1 = repositoryService.createDeployment()
        .addResourceFromString("foobar.jpdl.xml", firstVersion)
        .deploy();
    ProcessDefinition processDefinition1 = repositoryService
        .createProcessDefinitionQuery()
        .deploymentId(deploymentId1)
        .uniqueResult();
    Execution execution = executionService
        .startProcessInstanceById(processDefinition1.getId())
        .findActiveExecutionIn("foo");
    executionService.signalExecutionById(execution.getId());
    
    Task task = taskService.createTaskQuery().assignee("fredje").uniqueResult();
    assertNotNull(task);
    
    String deploymentId2 = repositoryService.createDeployment()
        .addResourceFromString("foobar.jpdl.xml", secondVersion)
        .deploy();
        
    taskService.completeTask(task.getId(), "to baz");
        
    task = taskService.createTaskQuery().assignee("fredje").uniqueResult();
    assertNotNull(task);

    identityService.deleteUser("fredje");
    
    repositoryService.deleteDeploymentCascade(deploymentId2);
    repositoryService.deleteDeploymentCascade(deploymentId1);
    
  }

}
