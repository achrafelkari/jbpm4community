package org.jbpm.bpmn.test.task;

import java.util.List;

import org.jbpm.api.NewDeployment;
import org.jbpm.api.ProcessInstance;
import org.jbpm.api.task.Task;
import org.jbpm.test.JbpmTestCase;


public class ManualTaskTest  extends JbpmTestCase {

  @Override
  protected void setUp() throws Exception {
      super.setUp();
      NewDeployment deployment = repositoryService.createDeployment();
      deployment.addResourceFromClasspath("org/jbpm/bpmn/manualTask.bpmn.xml");
      registerDeployment(deployment.deploy());
      
      identityService.createGroup("sales");
      identityService.createUser("johndoe", "John", "Doe");
      identityService.createMembership("johndoe", "sales");
  }
  
  @Override
  protected void tearDown() throws Exception {
    identityService.deleteGroup("sales");
    identityService.deleteUser("johndoe");
    
    super.tearDown();
  }

  public void testJavaServiceTaskCall() {

      ProcessInstance pi = executionService.startProcessInstanceByKey("manualTaskProcess");

      assertNotNull(pi.getId());

      List<Task> allTasks = taskService.findGroupTasks("johndoe");
      assertEquals(1, allTasks.size());

      taskService.completeTask(allTasks.get(0).getId());
      assertProcessInstanceEnded(pi);
  }

}
