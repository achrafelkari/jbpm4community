package org.jbpm.bpmn;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jbpm.api.ProcessInstance;
import org.jbpm.api.TaskQuery;
import org.jbpm.api.task.Task;
import org.jbpm.bpmn.parser.BpmnParser;
import org.jbpm.pvm.internal.xml.Problem;
import org.jbpm.test.JbpmTestCase;


public class NewLineConditionExpressionTest extends JbpmTestCase {
  static BpmnParser bpmnParser = new BpmnParser();

  public List<Problem> parse(String resource) {

    List<Problem> problems = bpmnParser.createParse().setResource(resource).execute().getProblems();

    return problems;
  }

  public void testNormalExecuteDecisionCondition() {

    String deploymentId = repositoryService.createDeployment()
      .addResourceFromClasspath("org/jbpm/bpmn/newLineConditionExpression.bpmn.xml").deploy();

    try {
      ProcessInstance pi = executionService.startProcessInstanceByKey("newLineConditionExpression");
      String pid = pi.getId();

      TaskQuery taskQuery = taskService.createTaskQuery();
      List<Task> allTasks = taskQuery.list();

      assertEquals(1, allTasks.size());
      assertEquals("testTask1", allTasks.get(0).getActivityName());
      taskService.completeTask(allTasks.get(0).getId());

      allTasks = taskQuery.list();
      assertEquals(1, allTasks.size());
      assertEquals("testTask2", allTasks.get(0).getActivityName());

      HashMap vars = new HashMap();
      vars.put("bewertungen", 3);

      taskService.completeTask(allTasks.get(0).getId(), vars);

      allTasks = taskQuery.list();
      assertEquals(1, allTasks.size());

      taskService.completeTask(allTasks.get(0).getId());

      // process instance should be ended
      pi = executionService.findProcessInstanceById(pid);
      assertNull(pi);

    }
    finally {
      repositoryService.deleteDeploymentCascade(deploymentId);
    }
  }
}
