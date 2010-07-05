package org.jbpm.test.task;

import java.util.List;

import org.jbpm.api.ProcessInstance;
import org.jbpm.api.TaskService;
import org.jbpm.api.model.OpenExecution;
import org.jbpm.api.task.Assignable;
import org.jbpm.api.task.AssignmentHandler;
import org.jbpm.api.task.Participation;
import org.jbpm.api.task.Task;
import org.jbpm.pvm.internal.env.EnvironmentImpl;
import org.jbpm.test.JbpmTestCase;

public class AssignmentHandlerTest extends JbpmTestCase {

  String deploymentId;

  protected void setUp() throws Exception {
    super.setUp();

    // XML definition
    String jpdl = "<?xml version=\"1.0\"?>"
      + "<process key=\"testProcess\" name=\"Test Process\" xmlns=\"http://jbpm.org/4.3/jpdl\">"
      + "  <start g=\"67,236,48,48\" name=\"start1\">"
      + "    <transition g=\"-43,-18\" name=\"to fork1\" to=\"fork1\"/>" + "  </start>"
      + "  <task g=\"255,144,92,52\" name=\"task1\" candidate-groups=\"firstGroup\">"
      + "    <assignment-handler class=\""
      + AutoAssignment.class.getName()
      + "\"/>"
      + "    <transition g=\"-41,-18\" name=\"to join1\" to=\"join1\"/>"
      + "  </task>"
      + "  <task g=\"258,334,92,52\" name=\"task2\" candidate-groups=\"secondGroup\">"
      + "    <assignment-handler class=\""
      + AutoAssignment.class.getName()
      + "\"/>"
      + "    <transition g=\"-41,-18\" name=\"to join1\" to=\"join1\"/>"
      + "  </task>"
      + "  <task g=\"515,228,92,52\" name=\"task3\" candidate-groups=\"thirdGroup\">"
      + "    <assignment-handler class=\""
      + AutoAssignment.class.getName()
      + "\"/>"
      + "    <transition g=\"-42,-18\" name=\"to end1\" to=\"end1\"/>"
      + "  </task>"
      + "  <end g=\"676,232,48,48\" name=\"end1\"/>"
      + "  <fork g=\"172,236,48,48\" name=\"fork1\">"
      + "    <transition g=\"-44,-18\" name=\"to task1\" to=\"task1\"/>"
      + "    <transition g=\"-44,-18\" name=\"to task2\" to=\"task2\"/>"
      + "  </fork>"
      + "  <join g=\"385,233,48,48\" name=\"join1\">"
      + "    <transition g=\"-44,-18\" name=\"to task3\" to=\"task3\"/>"
      + "  </join>"
      + "</process>";

    // Deploys the process
    deploymentId = repositoryService.createDeployment()
      .addResourceFromString("testProcess.jpdl.xml", jpdl.toString())
      .deploy();
  }

  protected void tearDown() throws Exception {
    repositoryService.deleteDeploymentCascade(deploymentId);
    super.tearDown();
  }

  /** Tests the process. */
  public void testProcess() {
    // Starts a new process instance and gets the instance id
    ProcessInstance processInstance = executionService.startProcessInstanceByKey("testProcess");
    String pid = processInstance.getId();

    // Gets the tasks auto-assigned for the first user and completes the task
    List<Task> taskList = taskService.findPersonalTasks("firstUser");
    assertEquals(1, taskList.size());
    Task task = taskList.get(0);
    taskService.completeTask(task.getId());

    // Gets the tasks auto-assigned for the second user and completes the task
    taskList = taskService.findPersonalTasks("secondUser");
    assertEquals(1, taskList.size());
    task = taskList.get(0);
    taskService.completeTask(task.getId());

    // Gets the tasks auto-assigned for the second user and completes the task
    taskList = taskService.findPersonalTasks("thirdUser");
    assertEquals(1, taskList.size());
    task = taskList.get(0);
    taskService.completeTask(task.getId());

    // Tries to load the instance and checks if it was finished
    processInstance = executionService.findProcessInstanceById(pid);
    assertNull(processInstance);
  }

  /** Auto assignment class. */
  public static class AutoAssignment implements AssignmentHandler {

    private static final long serialVersionUID = 9063679883107908899L;

    /** Auto-claim the task to the default user. */
    public void assign(Assignable assignable, OpenExecution execution) throws Exception {
      // retrieve the participations
      Task task = (Task) assignable;
      List<Participation> participations = EnvironmentImpl.getFromCurrent(TaskService.class)
        .getTaskParticipations(task.getId());

      // If the participations list is not empty, assign task to default user
      if (!participations.isEmpty()) {
        String groupId = participations.get(0).getGroupId();
        assignable.setAssignee(groupId.substring(0, groupId.indexOf("Group")) + "User");
      }
    }
  }
}
