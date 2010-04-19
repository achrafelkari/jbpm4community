package org.jbpm.test.task.multiple;

import java.util.List;

import org.jbpm.api.task.Task;
import org.jbpm.test.JbpmTestCase;
import org.subethamail.wiser.Wiser;

public class MultipleTasksTest extends JbpmTestCase {

  Wiser wiser = new Wiser();
  String deploymentId;

  protected void setUp() throws Exception {
    super.setUp();
    deploymentId = repositoryService.createDeployment()
        .addResourceFromClasspath(
            "org/jbpm/test/task/multiple/process.jpdl.xml")
        .deploy();
    identityService.createUser("johndoe", "John", "Doe", "john@doe");
    wiser.setPort(2525);
    wiser.start();
  }

  protected void tearDown() throws Exception {
    wiser.stop();
    repositoryService.deleteDeploymentCascade(deploymentId);
    identityService.deleteUser("johndoe");
    super.tearDown();
  }

  public void testOltWorkflow() {
    executionService.startProcessInstanceByKey("MultipleTasks");

    List<Task> taskList = taskService.findPersonalTasks("johndoe");
    assertEquals(1, taskList.size());
    Task task = taskList.get(0);
    assertEquals("task1", task.getName());
    assertEquals("johndoe", task.getAssignee());

    taskService.completeTask(task.getId()); /*This is where it goes wrong...*/

    taskList = taskService.findPersonalTasks("johndoe");
    assertEquals(1, taskList.size());
    task = taskList.get(0);
    assertEquals("task2", task.getName());
    assertEquals("johndoe", task.getAssignee());
  }

}