package org.jbpm.examples.bpmn.gateway.inclusive;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jbpm.api.NewDeployment;
import org.jbpm.api.ProcessInstance;
import org.jbpm.api.TaskQuery;
import org.jbpm.api.task.Task;
import org.jbpm.test.JbpmTestCase;


public class InclusiveGatewayTest extends JbpmTestCase {
  
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    NewDeployment deployment = repositoryService.createDeployment();
    deployment.addResourceFromClasspath("org/jbpm/examples/bpmn/gateway/inclusive/inclusive_gateway.bpmn.xml");
    registerDeployment(deployment.deploy());
  }
  
  public void testLargeDeposit() {
    Map<String, Object> vars = new HashMap<String, Object>();
    vars.put("cash", 15000);
    vars.put("bank", "local");
    ProcessInstance processInstance = executionService.startProcessInstanceByKey("inclusiveGateway", vars);
    
    TaskQuery taskQuery = taskService.createTaskQuery().processInstanceId(processInstance.getId());
    assertEquals(1, taskQuery.count());
    Task largeDepositTask = taskQuery.uniqueResult();
    assertEquals("Large deposit", largeDepositTask.getName());
    
    taskService.completeTask(largeDepositTask.getId());
    assertProcessInstanceEnded(processInstance);
  }
  
  public void testLargeForeignDeposit() {
    Map<String, Object> vars = new HashMap<String, Object>();
    vars.put("cash", 20000);
    vars.put("bank", "foreign");
    ProcessInstance processInstance = executionService.startProcessInstanceByKey("inclusiveGateway", vars);
    
    TaskQuery taskQuery = taskService.createTaskQuery()
          .processInstanceId(processInstance.getId()).orderAsc(TaskQuery.PROPERTY_NAME);
    List<Task> tasks = taskQuery.list();
    assertEquals(2, tasks.size());
    assertEquals("Foreign deposit", tasks.get(0).getName());
    assertEquals("Large deposit", tasks.get(1).getName());
   
    for (Task task : tasks) {
      taskService.completeTask(task.getId());
    }
    
    assertProcessInstanceEnded(processInstance);
  }
  
  public void testStandardDeposit() {
    Map<String, Object> vars = new HashMap<String, Object>();
    vars.put("cash", 7000);
    vars.put("bank", "local");
    ProcessInstance processInstance = executionService.startProcessInstanceByKey("inclusiveGateway", vars);
    
    TaskQuery taskQuery = taskService.createTaskQuery().processInstanceId(processInstance.getId());
    assertEquals(1, taskQuery.count());
    Task standardDeposit = taskQuery.uniqueResult();
    assertEquals("Standard deposit", standardDeposit.getName());
    
    taskService.completeTask(standardDeposit.getId());
    assertProcessInstanceEnded(processInstance);
  }
  

}
