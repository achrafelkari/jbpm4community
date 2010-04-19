package org.jbpm.test.upgrade;

import java.util.List;

import org.jbpm.api.ProcessDefinition;
import org.jbpm.api.ProcessInstance;
import org.jbpm.api.job.Job;
import org.jbpm.api.task.Task;

public class Process3AfterUpgradeTest extends AfterUpgradeJbpmTestCase {

  private static final String PROCESS_3_KEY = "testprocess3";
  
  private static final String TASK_NAME = "do something important";
  
  private static final String TASK_DONE_OUTCOME = "done";
  
  public void testDataValid() {

    // 2 process instances should be active
    ProcessDefinition procDef = findProcessDefinitionByKey(PROCESS_3_KEY);
    List<ProcessInstance> procInstances = findProcessInstancesByProcessDefinition(procDef.getId());
    assertEquals(2, procInstances.size()); 
    
    // process instance 1 should be in the 'timed out' state
    ProcessInstance processInstance = findProcessInstanceByKey(PROCESS_3_KEY + "-0");
    assertActivityActive(processInstance.getId(), "timed out");
    
    // process instance 2 should have an uncompleted task
    List<Task> tasks = taskService.createTaskQuery().assignee("johnDoe").list();
    assertEquals(2, tasks.size());
    
    // There should be one job unfinished
    List<Job> jobs = managementService.createJobQuery().list();
    assertEquals(1, jobs.size());
    assertEquals(PROCESS_3_KEY + "-1", jobs.get(0).getProcessInstance().getKey());
    
  }

  public void testContinueExistingInstance() {
    
    // There is one process instance currently waiting at the human task
    ProcessInstance pi = findProcessInstanceByKey(PROCESS_3_KEY + "-1");
    Job job = managementService.createJobQuery().processInstanceId(pi.getId()).uniqueResult();
    assertNotNull(job);
    
    Task task = taskService.createTaskQuery().processInstanceId(pi.getId()).uniqueResult();
    assertEquals(TASK_NAME, task.getName());
    taskService.completeTask(task.getId(), TASK_DONE_OUTCOME);
    
    assertProcessInstanceEnded(pi);
    List<Job> jobs = managementService.createJobQuery().processInstanceId(pi.getId()).list();
    assertEquals(0, jobs.size());
    
  }

  public void testStartFreshProcessInstance() {
    
    ProcessInstance pi = executionService.startProcessInstanceByKey(PROCESS_3_KEY);
    Task task = taskService.createTaskQuery().processInstanceId(pi.getId()).uniqueResult();
    assertEquals(TASK_NAME, task.getName());
    
    Job job = managementService.createJobQuery().processInstanceId(pi.getId()).uniqueResult();
    assertNotNull(job);
    
    taskService.completeTask(task.getId(), TASK_DONE_OUTCOME);
    assertProcessInstanceEnded(pi);
    List<Job> jobs = managementService.createJobQuery().processInstanceId(pi.getId()).list();
    assertEquals(0, jobs.size());
  }
  
}
