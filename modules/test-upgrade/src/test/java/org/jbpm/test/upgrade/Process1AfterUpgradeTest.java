package org.jbpm.test.upgrade;

import java.util.List;
import java.util.Set;

import org.jbpm.api.Execution;
import org.jbpm.api.ProcessDefinition;
import org.jbpm.api.ProcessInstance;
import org.jbpm.api.history.HistoryProcessInstance;

public class Process1AfterUpgradeTest extends AfterUpgradeJbpmTestCase {

  private static final String PROCESS_1_KEY = "testprocess1";
  
  public void testDataValid() {

    // Check process1 deployment
    List<ProcessDefinition> procDefs = findProcessDefinitionsByKey(PROCESS_1_KEY);
    assertEquals(1, procDefs.size());

    List<ProcessInstance> procInstances = 
      findProcessInstancesByProcessDefinition(procDefs.get(0).getId());
    assertEquals(4, procInstances.size());

    // process instance 1 is in the 'print documents' state
    ProcessInstance processInstance1 = findProcessInstanceByKey(PROCESS_1_KEY + "-0");
    Set<String> activeActivities = processInstance1.findActiveActivityNames();
    assertEquals(1, activeActivities.size());
    assertActivityActive(processInstance1.getId(), "print documents");

    // process instance 2 is in the 'load truck' and 'send invoice' state
    ProcessInstance processInstance2 = findProcessInstanceByKey(PROCESS_1_KEY + "-1");
    activeActivities = processInstance2.findActiveActivityNames();
    assertActivitiesActive(processInstance2.getId(), "load truck", "send invoice");
    
    // process instance 3 is finished
    ProcessInstance processInstance3 = findProcessInstanceByKey(PROCESS_1_KEY + "-2");
    assertNull(processInstance3);
    HistoryProcessInstance histProcInst3 = findHistoryProcessInstanceByKey(PROCESS_1_KEY + "-2");
    assertNotNull(histProcInst3);
    
  }
  
  public void testContinueExistingInstance() {
    continueProcessInstance1();
    continueProcessInstance4();
  }
  
  /* Process instance 1 is waiting in the state 'print documents' */
  private void continueProcessInstance1() {
    
    ProcessInstance pi = findProcessInstanceByKey(PROCESS_1_KEY + "-0");
    String activeState = "print documents";
    Execution execution = pi.findActiveExecutionIn(activeState);
    
    // Signalling it will cause the process instance to go to 'drive truck'
    executionService.signalExecutionById(execution.getId());
    
    pi = findProcessInstanceByKey(PROCESS_1_KEY + "-0");
    activeState = "drive truck";
    assertActivityActive(pi.getId(), activeState);
    
    // Signalling the process instance will now cause it to end
    execution = pi.findActiveExecutionIn(activeState);
    executionService.signalExecutionById(execution.getId());
    assertProcessInstanceEnded(pi.getId());
  }
  
  /* Process instance 4 is waiting in all the states */
  private void continueProcessInstance4() {
    
    ProcessInstance pi = findProcessInstanceByKey(PROCESS_1_KEY + "-3");
    Set<String> activeActivities = pi.findActiveActivityNames();
    assertTrue(activeActivities.contains("send invoice"));
    assertTrue(activeActivities.contains("load truck"));
    assertTrue(activeActivities.contains("print documents"));
    
    // Signalling these states will cause the process instance to go to 'drive truck'
    for (String activeActivity : pi.findActiveActivityNames()) {
      Execution execution = pi.findActiveExecutionIn(activeActivity);
      executionService.signalExecutionById(execution.getId());
      pi = findProcessInstanceByKey(PROCESS_1_KEY + "-3");
    }
    
    assertEquals(1, pi.findActiveActivityNames().size());
    assertEquals("drive truck", pi.findActiveActivityNames().iterator().next());
  }
  
  public void testStartFreshProcessInstance() {
    ProcessInstance processInstance = executionService.startProcessInstanceByKey(PROCESS_1_KEY);
    assertActivitiesActive(processInstance.getId(), "load truck", "send invoice", "print documents");
  }
  
}
