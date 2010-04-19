package org.jbpm.test.upgrade;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jbpm.api.Execution;
import org.jbpm.api.ProcessDefinition;
import org.jbpm.api.ProcessInstance;
import org.jbpm.api.history.HistoryProcessInstance;

public class Process2AfterUpgradeTest extends AfterUpgradeJbpmTestCase {

  private static final String PROCESS_2_KEY = "testprocess2";
  
  private static final String STATE_MORE_THAN_5 = "more than 5";
  
  private static final String VARIABLE_KEY = "var";
  
  public void testDataValid() {

    // 4 process instances should be active
    ProcessDefinition procDef = findProcessDefinitionByKey(PROCESS_2_KEY);
    List<ProcessInstance> procInstances = findProcessInstancesByProcessDefinition(procDef.getId());
    assertEquals(4, procInstances.size()); // 3 processes are already finished: 7-3=4 left
    
    // These processes should be in the 'more than 5' state
    for (ProcessInstance processInstance : procInstances) {
      Set<String> activeActivities = processInstance.findActiveActivityNames();
      assertEquals(1, activeActivities.size());
      assertActivityActive(processInstance.getId(), STATE_MORE_THAN_5);
    }
    
    // 4 process instances should have ended
    List<HistoryProcessInstance> histProcInsts = findEndedProcessInstancesByProcDef(procDef.getId());
    assertEquals(3, histProcInsts.size());
    
  }

  public void testContinueExistingInstance() {
    
    final String piKey = PROCESS_2_KEY + "-3";
    ProcessInstance pi = findProcessInstanceByKey(piKey);
    Set<String> activeActivities = pi.findActiveActivityNames();
    assertEquals(1, activeActivities.size());
    
    // Resignalling it should put it again in "more than 5"
    Execution execution = pi.findActiveExecutionIn(STATE_MORE_THAN_5);
    executionService.signalExecutionById(execution.getId());
    
    pi = findProcessInstanceByKey(piKey);
    activeActivities = pi.findActiveActivityNames();
    assertEquals(1, activeActivities.size());
    execution = pi.findActiveExecutionIn(STATE_MORE_THAN_5);
    
    // Changing the variable to < 5 and signal the execution should end the proc
    Map<String, Object> vars = new HashMap<String, Object>();
    vars.put(VARIABLE_KEY, 3);
    executionService.signalExecutionById(execution.getId(), vars);
    assertProcessInstanceEnded(pi);
    
  }

  public void testStartFreshProcessInstance() {
    
    // Start a new ProcessInstance with var < 5 -> will be in wait state
    Map<String, Object> vars = new HashMap<String, Object>();
    vars.put(VARIABLE_KEY, 7);
    ProcessInstance pi = executionService.startProcessInstanceByKey(PROCESS_2_KEY, vars);
    
    Set<String> activeActivities = pi.findActiveActivityNames();
    assertEquals(1, activeActivities.size());
    assertEquals(STATE_MORE_THAN_5, activeActivities.iterator().next());
    
    // Signalling with a var < 5 will end the process instance 
    Execution execution = pi.findActiveExecutionIn(STATE_MORE_THAN_5);
    vars.put(VARIABLE_KEY, 2);
    executionService.signalExecutionById(execution.getId(), vars);
    assertProcessInstanceEnded(pi);
    
  }
  
}
