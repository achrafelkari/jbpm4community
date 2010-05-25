package org.jbpm.test.migration;

import java.util.HashSet;

import org.jbpm.api.JbpmException;
import org.jbpm.api.ProcessDefinition;
import org.jbpm.api.ProcessInstance;
import org.jbpm.pvm.internal.migration.MigrationDescriptor;
import org.jbpm.pvm.internal.migration.MigrationHandler;
import org.jbpm.test.JbpmTestCase;


public class InstanceMigratorTest extends JbpmTestCase {
  
  private String originalVersion = 
    "<process name='foobar'>" +
    "  <start>" +
    "    <transition to='a'/>" +
    "  </start>" +
    "  <state name='a'>" +
    "    <transition to='b'/>" +
    "  </state>" +
    "  <state name='b'>" +
    "    <transition to='c'/>" +
    "  </state>" +
    "  <state name='c'>" +
    "    <transition to='end'/>" +
    "  </state>" +
    "  <end name='end'/>" +
    "</process>";
  
  private String versionWithMigrationHandler = 
    "<process name='foobar'>" +
    "  <start>" +
    "    <transition to='a'/>" +
    "  </start>" +
    "  <state name='a'>" +
    "    <transition to='b'/>" +
    "  </state>" +
    "  <state name='b'>" +
    "    <transition to='c'/>" +
    "  </state>" +
    "  <state name='c'>" +
    "    <transition to='end'/>" +
    "  </state>" +
    "  <end name='end'/>" +
    "  <migrate-instances>" +
    "    <migration-handler class='org.jbpm.test.migration.InstanceMigratorTest$TestHandler'/>" +
    "  </migrate-instances>" +
    "</process>";
  
  private String versionWithAbortion = 
    "<process name='foobar'>" +
    "  <start>" +
    "    <transition to='a'/>" +
    "  </start>" +
    "  <state name='a'>" +
    "    <transition to='b'/>" +
    "  </state>" +
    "  <state name='b'>" +
    "    <transition to='c'/>" +
    "  </state>" +
    "  <state name='c'>" +
    "    <transition to='end'/>" +
    "  </state>" +
    "  <end name='end'/>" +
    "  <migrate-instances action='end'>" +
    "    <migration-handler class='org.jbpm.test.migration.InstanceMigratorTest$TestHandler'/>" +
    "  </migrate-instances>" +
    "</process>";
  
  private String versionWithSimpleMigration = 
    "<process name='foobar'>" +
    "  <start>" +
    "    <transition to='a'/>" +
    "  </start>" +
    "  <state name='a'>" +
    "    <transition to='b'/>" +
    "  </state>" +
    "  <state name='b'>" +
    "    <transition to='c'/>" +
    "  </state>" +
    "  <state name='c'>" +
    "    <transition to='end'/>" +
    "  </state>" +
    "  <end name='end'/>" +
    "  <migrate-instances/>" +
    "</process>";
  
  private String versionWithCorrectMappings = 
    "<process name='foobar'>" +
    "  <start>" +
    "    <transition to='a'/>" +
    "  </start>" +
    "  <state name='a'>" +
    "    <transition to='b'/>" +
    "  </state>" +
    "  <state name='b'>" +
    "    <transition to='c'/>" +
    "  </state>" +
    "  <state name='c'>" +
    "    <transition to='d'/>" +
    "  </state>" +
    "  <state name='d'>" +
    "    <transition to='end'/>" +
    "  </state>" +
    "  <end name='end'/>" +
    "  <migrate-instances>" +
    "    <activity-mapping old-name='b' new-name='a'/>" +
    "    <activity-mapping old-name='c' new-name='d'/>" +
    "  </migrate-instances>" +
    "</process>";
  
  private String versionWithIncorrectMappings = 
    "<process name='foobar'>" +
    "  <start>" +
    "    <transition to='a'/>" +
    "  </start>" +
    "  <state name='a'>" +
    "    <transition to='b'/>" +
    "  </state>" +
    "  <state name='b'>" +
    "    <transition to='c'/>" +
    "  </state>" +
    "  <state name='c'>" +
    "    <transition to='d'/>" +
    "  </state>" +
    "  <end name='end'/>" +
    "  <migrate-instances>" +
    "    <activity-mapping old-name='a' new-name='z'/>" +
    "    <activity-mapping old-name='b' new-name='c'/>" +
    "  </migrate-instances>" +
    "</process>";

  private String versionWithAbsoluteVersionRange = 
    "<process name='foobar'>" +
    "  <start>" +
    "    <transition to='a'/>" +
    "  </start>" +
    "  <state name='a'>" +
    "    <transition to='b'/>" +
    "  </state>" +
    "  <state name='b'>" +
    "    <transition to='c'/>" +
    "  </state>" +
    "  <state name='c'>" +
    "    <transition to='end'/>" +
    "  </state>" +
    "  <end name='end'/>" +
    "  <migrate-instances versions='2..3'/>" +
    "</process>";
  
  private String versionWithRelativeVersionRange = 
    "<process name='foobar'>" +
    "  <start>" +
    "    <transition to='a'/>" +
    "  </start>" +
    "  <state name='a'>" +
    "    <transition to='b'/>" +
    "  </state>" +
    "  <state name='b'>" +
    "    <transition to='c'/>" +
    "  </state>" +
    "  <state name='c'>" +
    "    <transition to='end'/>" +
    "  </state>" +
    "  <end name='end'/>" +
    "  <migrate-instances versions='x-2..x'/>" +
    "</process>";

  private String versionWithMixedVersionRange = 
    "<process name='foobar'>" +
    "  <start>" +
    "    <transition to='a'/>" +
    "  </start>" +
    "  <state name='a'>" +
    "    <transition to='b'/>" +
    "  </state>" +
    "  <state name='b'>" +
    "    <transition to='c'/>" +
    "  </state>" +
    "  <state name='c'>" +
    "    <transition to='end'/>" +
    "  </state>" +
    "  <end name='end'/>" +
    "  <migrate-instances versions='1..x-3'/>" +
    "</process>";

  private String versionWithWildcardVersionRange = 
    "<process name='foobar'>" +
    "  <start>" +
    "    <transition to='a'/>" +
    "  </start>" +
    "  <state name='a'>" +
    "    <transition to='b'/>" +
    "  </state>" +
    "  <state name='b'>" +
    "    <transition to='c'/>" +
    "  </state>" +
    "  <state name='c'>" +
    "    <transition to='end'/>" +
    "  </state>" +
    "  <end name='end'/>" +
    "  <migrate-instances versions='*'/>" +
    "</process>";

  private static HashSet<String> PROCESS_INSTANCES_SET = new HashSet<String>();
  
  public static class TestHandler implements MigrationHandler {
    public void migrateInstance(
            ProcessDefinition processDefinition, 
            ProcessInstance processInstance,
            MigrationDescriptor migrationDescriptor) {
      PROCESS_INSTANCES_SET.add(processInstance.getId());
    }    
  }
  
  protected void tearDown() throws Exception {
    PROCESS_INSTANCES_SET.clear();
    super.tearDown();
  }
  
    
  private ProcessDefinition deployProcessDefinition(String name, String processDefinitionXml) {
    String deploymentId = repositoryService
        .createDeployment()
        .addResourceFromString(name + ".jpdl.xml", processDefinitionXml)
        .deploy();
    return repositoryService
        .createProcessDefinitionQuery()
        .deploymentId(deploymentId)
        .uniqueResult();
  }
  
  private ProcessInstance startAndSignal(ProcessDefinition processDefinition, String endActivityName) {
    ProcessInstance result = executionService
        .startProcessInstanceById(processDefinition.getId());
    while (result.findActiveExecutionIn(endActivityName) == null) {
      result = executionService.signalExecutionById(result.getId());
    }
    return result;
  }

  public void testNoProcessInstanceForMigration() {
    ProcessDefinition pd2 = deployProcessDefinition("foobar", versionWithWildcardVersionRange);
    repositoryService.deleteDeploymentCascade(pd2.getDeploymentId());
  }

  public void testNoMigration() {    
    ProcessDefinition pd1 = deployProcessDefinition("foobar", originalVersion);
    ProcessInstance pi1 = startAndSignal(pd1, "a");
    ProcessInstance pi2 = startAndSignal(pd1, "b");    
    ProcessDefinition pd2 = deployProcessDefinition("foobar", originalVersion);   
    pi1 = executionService.findProcessInstanceById(pi1.getId());
    pi2 = executionService.findProcessInstanceById(pi2.getId());
    assertEquals(pd1.getId(), pi1.getProcessDefinitionId());
    assertEquals(pd1.getId(), pi2.getProcessDefinitionId());    
    assertTrue(pi1.findActiveActivityNames().contains("a"));
    assertTrue(pi2.findActiveActivityNames().contains("b"));
    repositoryService.deleteDeploymentCascade(pd1.getDeploymentId());
    repositoryService.deleteDeploymentCascade(pd2.getDeploymentId());   
  }
  
  public void testMigrationHandler() {
    ProcessDefinition pd1 = deployProcessDefinition("foobar", originalVersion);
    ProcessInstance pi1 = startAndSignal(pd1, "a");
    ProcessInstance pi2 = startAndSignal(pd1, "b");    
    ProcessDefinition pd2 = deployProcessDefinition("foobar", versionWithMigrationHandler);    
    assertTrue(PROCESS_INSTANCES_SET.contains(pi1.getId()));
    assertTrue(PROCESS_INSTANCES_SET.contains(pi2.getId()));
    pi1 = executionService.findProcessInstanceById(pi1.getId());
    pi2 = executionService.findProcessInstanceById(pi2.getId());
    assertEquals(pd2.getId(), pi1.getProcessDefinitionId());
    assertEquals(pd2.getId(), pi2.getProcessDefinitionId());
    assertEquals(pi1, pi1.findActiveExecutionIn("a"));
    assertEquals(pi2, pi2.findActiveExecutionIn("b"));
    repositoryService.deleteDeploymentCascade(pd1.getDeploymentId());
    repositoryService.deleteDeploymentCascade(pd2.getDeploymentId());   
  }
  
  public void testSimpleAbortion() {
    ProcessDefinition pd1 = deployProcessDefinition("foobar", originalVersion);
    ProcessInstance pi1 = startAndSignal(pd1, "a");
    ProcessInstance pi2 = startAndSignal(pd1, "b");
    ProcessDefinition pd2 = deployProcessDefinition("foobar", versionWithAbortion);
    assertTrue(PROCESS_INSTANCES_SET.contains(pi1.getId()));
    assertTrue(PROCESS_INSTANCES_SET.contains(pi2.getId()));
    pi1 = executionService.findProcessInstanceById(pi1.getId());
    pi2 = executionService.findProcessInstanceById(pi2.getId());
    assertNull(pi1);
    assertNull(pi2);
    repositoryService.deleteDeploymentCascade(pd1.getDeploymentId());
    repositoryService.deleteDeploymentCascade(pd2.getDeploymentId());   
  }
  
  public void testSimpleMigration() {
    ProcessDefinition pd1 = deployProcessDefinition("foobar", originalVersion);
    ProcessInstance pi1 = startAndSignal(pd1, "a");
    ProcessInstance pi2 = startAndSignal(pd1, "b");
    ProcessDefinition pd2 = deployProcessDefinition("foobar", versionWithSimpleMigration);
    pi1 = executionService.findProcessInstanceById(pi1.getId());
    pi2 = executionService.findProcessInstanceById(pi2.getId());
    assertEquals(pd2.getId(), pi1.getProcessDefinitionId());
    assertEquals(pd2.getId(), pi2.getProcessDefinitionId());
    assertEquals(pi1, pi1.findActiveExecutionIn("a"));
    assertEquals(pi2, pi2.findActiveExecutionIn("b"));
    repositoryService.deleteDeploymentCascade(pd1.getDeploymentId());
    repositoryService.deleteDeploymentCascade(pd2.getDeploymentId());   
  }
  
  public void testCorrectlyMappedMigration() {
    ProcessDefinition pd1 = deployProcessDefinition("foobar", originalVersion);
    ProcessInstance pi1 = startAndSignal(pd1, "a");
    ProcessInstance pi2 = startAndSignal(pd1, "b");
    ProcessInstance pi3 = startAndSignal(pd1, "c");
    ProcessDefinition pd2 = deployProcessDefinition("foobar", versionWithCorrectMappings);
    pi1 = executionService.findProcessInstanceById(pi1.getId());
    pi2 = executionService.findProcessInstanceById(pi2.getId());
    pi3 = executionService.findProcessInstanceById(pi3.getId());
    assertEquals(pd2.getId(), pi1.getProcessDefinitionId());
    assertEquals(pd2.getId(), pi2.getProcessDefinitionId());
    assertEquals(pd2.getId(), pi3.getProcessDefinitionId());
    assertEquals(pi1, pi1.findActiveExecutionIn("a"));
    assertEquals(pi2, pi2.findActiveExecutionIn("a"));
    assertEquals(pi3, pi3.findActiveExecutionIn("d"));
    pi1 = executionService.signalExecutionById(pi1.getId());
    pi2 = executionService.signalExecutionById(pi2.getId());
    pi2 = executionService.signalExecutionById(pi2.getId());
    pi3 = executionService.signalExecutionById(pi3.getId());
    assertEquals(pi1, pi1.findActiveExecutionIn("b"));
    assertEquals(pi2, pi2.findActiveExecutionIn("c"));
    assertTrue(pi3.isEnded());
    pi3 = executionService.findProcessInstanceById(pi3.getId());
    assertNull(pi3);
    repositoryService.deleteDeploymentCascade(pd1.getDeploymentId());
    repositoryService.deleteDeploymentCascade(pd2.getDeploymentId());   
  }
  
  public void testIncorrectlyMappedMigration() {
    ProcessDefinition pd1 = deployProcessDefinition("foobar", originalVersion);
    startAndSignal(pd1, "a");
    startAndSignal(pd1, "b");
    try {
      deployProcessDefinition("foobar", versionWithIncorrectMappings);
      fail();
    } catch (JbpmException e) {
      repositoryService.deleteDeploymentCascade(pd1.getDeploymentId());
    }
  }
  
  public void testAbsoluteVersionRange() {
    ProcessDefinition pd1 = deployProcessDefinition("foobar", originalVersion);
    ProcessInstance pi1 = startAndSignal(pd1, "a");
    ProcessDefinition pd2 = deployProcessDefinition("foobar", originalVersion);
    ProcessInstance pi2 = startAndSignal(pd2, "a");
    ProcessDefinition pd3 = deployProcessDefinition("foobar", originalVersion);
    ProcessInstance pi3 = startAndSignal(pd3, "a");
    ProcessDefinition pd4 = deployProcessDefinition("foobar", originalVersion);
    ProcessInstance pi4 = startAndSignal(pd4, "a");
    ProcessDefinition pd5 = deployProcessDefinition("foobar", versionWithAbsoluteVersionRange);
    pi1 = executionService.findProcessInstanceById(pi1.getId());
    pi2 = executionService.findProcessInstanceById(pi2.getId());
    pi3 = executionService.findProcessInstanceById(pi3.getId());
    pi4 = executionService.findProcessInstanceById(pi4.getId());
    assertEquals(pd1.getId(), pi1.getProcessDefinitionId());
    assertEquals(pd5.getId(), pi2.getProcessDefinitionId());
    assertEquals(pd5.getId(), pi3.getProcessDefinitionId());
    assertEquals(pd4.getId(), pi4.getProcessDefinitionId());
    repositoryService.deleteDeploymentCascade(pd1.getDeploymentId());
    repositoryService.deleteDeploymentCascade(pd2.getDeploymentId());   
    repositoryService.deleteDeploymentCascade(pd3.getDeploymentId());   
    repositoryService.deleteDeploymentCascade(pd4.getDeploymentId());   
    repositoryService.deleteDeploymentCascade(pd5.getDeploymentId());   
  }

  public void testRelativeVersionRange() {
    ProcessDefinition pd1 = deployProcessDefinition("foobar", originalVersion);
    ProcessInstance pi1 = startAndSignal(pd1, "a");
    ProcessDefinition pd2 = deployProcessDefinition("foobar", originalVersion);
    ProcessInstance pi2 = startAndSignal(pd2, "a");
    ProcessDefinition pd3 = deployProcessDefinition("foobar", originalVersion);
    ProcessInstance pi3 = startAndSignal(pd3, "a");
    ProcessDefinition pd4 = deployProcessDefinition("foobar", originalVersion);
    ProcessInstance pi4 = startAndSignal(pd4, "a");
    ProcessDefinition pd5 = deployProcessDefinition("foobar", versionWithRelativeVersionRange);
    pi1 = executionService.findProcessInstanceById(pi1.getId());
    pi2 = executionService.findProcessInstanceById(pi2.getId());
    pi3 = executionService.findProcessInstanceById(pi3.getId());
    pi4 = executionService.findProcessInstanceById(pi4.getId());
    assertEquals(pd1.getId(), pi1.getProcessDefinitionId());
    assertEquals(pd2.getId(), pi2.getProcessDefinitionId());
    assertEquals(pd5.getId(), pi3.getProcessDefinitionId());
    assertEquals(pd5.getId(), pi4.getProcessDefinitionId());
    repositoryService.deleteDeploymentCascade(pd1.getDeploymentId());
    repositoryService.deleteDeploymentCascade(pd2.getDeploymentId());   
    repositoryService.deleteDeploymentCascade(pd3.getDeploymentId());   
    repositoryService.deleteDeploymentCascade(pd4.getDeploymentId());   
    repositoryService.deleteDeploymentCascade(pd5.getDeploymentId());   
  }

  public void testMixedVersionRange() {
    ProcessDefinition pd1 = deployProcessDefinition("foobar", originalVersion);
    ProcessInstance pi1 = startAndSignal(pd1, "a");
    ProcessDefinition pd2 = deployProcessDefinition("foobar", originalVersion);
    ProcessInstance pi2 = startAndSignal(pd2, "a");
    ProcessDefinition pd3 = deployProcessDefinition("foobar", originalVersion);
    ProcessInstance pi3 = startAndSignal(pd3, "a");
    ProcessDefinition pd4 = deployProcessDefinition("foobar", originalVersion);
    ProcessInstance pi4 = startAndSignal(pd4, "a");
    ProcessDefinition pd5 = deployProcessDefinition("foobar", versionWithMixedVersionRange);
    pi1 = executionService.findProcessInstanceById(pi1.getId());
    pi2 = executionService.findProcessInstanceById(pi2.getId());
    pi3 = executionService.findProcessInstanceById(pi3.getId());
    pi4 = executionService.findProcessInstanceById(pi4.getId());
    assertEquals(pd5.getId(), pi1.getProcessDefinitionId());
    assertEquals(pd5.getId(), pi2.getProcessDefinitionId());
    assertEquals(pd3.getId(), pi3.getProcessDefinitionId());
    assertEquals(pd4.getId(), pi4.getProcessDefinitionId());
    repositoryService.deleteDeploymentCascade(pd1.getDeploymentId());
    repositoryService.deleteDeploymentCascade(pd2.getDeploymentId());   
    repositoryService.deleteDeploymentCascade(pd3.getDeploymentId());   
    repositoryService.deleteDeploymentCascade(pd4.getDeploymentId());   
    repositoryService.deleteDeploymentCascade(pd5.getDeploymentId());   
  }

  public void testWildcardVersionRange() {
    ProcessDefinition pd1 = deployProcessDefinition("foobar", originalVersion);
    ProcessInstance pi1 = startAndSignal(pd1, "a");
    ProcessDefinition pd2 = deployProcessDefinition("foobar", originalVersion);
    ProcessInstance pi2 = startAndSignal(pd2, "a");
    ProcessDefinition pd3 = deployProcessDefinition("foobar", originalVersion);
    ProcessInstance pi3 = startAndSignal(pd3, "a");
    ProcessDefinition pd4 = deployProcessDefinition("foobar", originalVersion);
    ProcessInstance pi4 = startAndSignal(pd4, "a");
    ProcessDefinition pd5 = deployProcessDefinition("foobar", versionWithWildcardVersionRange);
    pi1 = executionService.findProcessInstanceById(pi1.getId());
    pi2 = executionService.findProcessInstanceById(pi2.getId());
    pi3 = executionService.findProcessInstanceById(pi3.getId());
    pi4 = executionService.findProcessInstanceById(pi4.getId());
    assertEquals(pd5.getId(), pi1.getProcessDefinitionId());
    assertEquals(pd5.getId(), pi2.getProcessDefinitionId());
    assertEquals(pd5.getId(), pi3.getProcessDefinitionId());
    assertEquals(pd5.getId(), pi4.getProcessDefinitionId());
    repositoryService.deleteDeploymentCascade(pd1.getDeploymentId());
    repositoryService.deleteDeploymentCascade(pd2.getDeploymentId());   
    repositoryService.deleteDeploymentCascade(pd3.getDeploymentId());   
    repositoryService.deleteDeploymentCascade(pd4.getDeploymentId());   
    repositoryService.deleteDeploymentCascade(pd5.getDeploymentId());   
  }

}
