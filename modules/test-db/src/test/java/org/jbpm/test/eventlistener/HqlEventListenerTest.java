package org.jbpm.test.eventlistener;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.jbpm.api.Execution;
import org.jbpm.api.task.Task;
import org.jbpm.test.JbpmTestCase;


public class HqlEventListenerTest extends JbpmTestCase {

  String taskLaundryId;
  String taskDishesId;
  String taskIronId;
  
  protected void setUp() throws Exception {
    super.setUp();
    
    // add task laundry
    Task task = taskService.newTask();
    task.setName("laundry");
    taskLaundryId = taskService.saveTask(task);
    
    // add task dishes
    task = taskService.newTask();
    task.setName("dishes");
    taskDishesId = taskService.saveTask(task);
    
    // add task iron
    task = taskService.newTask();
    task.setName("iron");
    taskIronId = taskService.saveTask(task);
  }

  protected void tearDown() throws Exception {
    taskService.deleteTaskCascade(taskLaundryId);
    taskService.deleteTaskCascade(taskDishesId);
    taskService.deleteTaskCascade(taskIronId);
    
    super.tearDown();
  }


  public void testHql() {
    deployJpdlXmlString(
      "<process name='HqlEventListener'>" +
      "  <start >" +
      "    <transition to='wait' />" +
      "  </start>" +
      "  <state name='wait'>" +
      "    <on event='start'>" +
      "      <hql var='tasknames with i'>" +
      "        <query>" +
      "          select task.name" +
      "          from org.jbpm.pvm.internal.task.TaskImpl as task" +
      "          where task.name like :taskName" +
      "        </query>" +
      "        <parameters>" +
      "          <string name='taskName' value='%i%' />" +
      "        </parameters>" +
      "      </hql>" +
      "    </on>" +
      "  </state>" +
      "</process>"
    );
    
    Execution execution = executionService.startProcessInstanceByKey("HqlEventListener");
    String executionId = execution.getId();

    Set<String> expectedTaskNames = new HashSet<String>();
    expectedTaskNames.add("dishes");
    expectedTaskNames.add("iron");
    Collection<String> taskNames = (Collection<String>) executionService.getVariable(executionId, "tasknames with i");
    taskNames = new HashSet<String>(taskNames);
    assertEquals(expectedTaskNames, taskNames);
  }
}
