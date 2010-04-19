package org.jbpm.test.activity.forkjoin;

import java.util.Collection;
import java.util.List;

import org.jbpm.api.Execution;
import org.jbpm.api.ProcessDefinition;
import org.jbpm.api.ProcessDefinitionQuery;
import org.jbpm.api.model.Activity;
import org.jbpm.api.model.OpenExecution;
import org.jbpm.api.task.Task;
import org.jbpm.pvm.internal.model.OpenProcessDefinition;
import org.jbpm.test.JbpmTestCase;

public class JBPM2581ForkAndJoinTest extends JbpmTestCase {

  String deploymentId;

  public void testFullyNested() {
	  
	    deployJpdlXmlString("  <process name='FullyNested' xmlns='http://jbpm.org/4.3/jpdl'>" + 
	    		"   <start name='start1'>" + 
	    		"      <transition to='fork2'/>" + 
	    		"   </start>" + 
	    		"   <end name='end1'/>" + 
	    		"   " + 
	    		"   <task assignee='otto' name='task2'>" + 
	    		"      <transition to='join1'/>" + 
	    		"   </task>" + 
	    		"   <task assignee='otto' name='task3'>" + 
	    		"      <transition to='join2'/>" + 
	    		"   </task>" + 
	    		"   <task assignee='otto' name='task8'>" + 
	    		"      <transition to='end1'/>" + 
	    		"   </task>" + 
	    		"   <task assignee='otto' name='task9'>" + 
	    		"      <transition to='join2'/>" + 
	    		"   </task>" + 
	    		"   <task assignee='otto' name='task1'>" + 
	    		"      <transition to='join1'/>" + 
	    		"   </task>" + 
	    		"   <fork name='fork1'>" + 
	    		"      <transition to='task1'/>" + 
	    		"      <transition to='task2'/>" + 
	    		"   </fork>" + 
	    		"   <join name='join1'>" + 
	    		"      <transition to='task9'/>" + 
	    		"   </join>" + 
	    		"   <join name='join2'>" + 
	    		"      <transition to='task8'/>" + 
	    		"   </join>" + 
	    		"   <fork name='fork2'>" + 
	    		"      <transition to='task3'/>" + 
	    		"      <transition to='fork1'/>" + 
	    		"   </fork>" + 
	    		"</process> ");
	    
		Execution execution = executionService
				.startProcessInstanceByKey("FullyNested");

		OpenExecution oe = (OpenExecution) execution;
		Collection< ? > ec = oe.getExecutions();
		for (Object object : ec) {
          System.out.println(((OpenExecution) object).getActivity().getName());
        }
		
		ProcessDefinitionQuery pdq = repositoryService.createProcessDefinitionQuery().processDefinitionId(oe.getProcessDefinitionId());

		ProcessDefinition pd = pdq.uniqueResult();
		System.out.println(((OpenProcessDefinition) pd).getActivities());
		

		assertActivitiesActive(execution.getId(), "task3", "task1", "task2"); 
		
		List<Task> taskList = taskService.findPersonalTasks("otto");
		Task task = getTask(taskList, "task3");
		taskService.completeTask(task.getId());
		
		assertActivitiesActive(execution.getId(), "task1", "task2");
		
		taskList = taskService.findPersonalTasks("otto");
		task = getTask(taskList, "task2");
		taskService.completeTask(task.getId());
		
		assertActivitiesActive(execution.getId(), "task1");
		
		task = getTask(taskList, "task1");
        taskService.completeTask(task.getId());
        
        assertActivitiesActive(execution.getId(), "task9");
		
        taskList = taskService.findPersonalTasks("otto");
        task = getTask(taskList, "task9");
        taskService.completeTask(task.getId());
        
        assertActivitiesActive(execution.getId(), "task8");

        taskList = taskService.findPersonalTasks("otto");
        task = getTask(taskList, "task8");
        taskService.completeTask(task.getId());
        
        assertProcessInstanceEnded(execution.getId());

	}
  public void testWeirdNested() {

    deployJpdlXmlString("<process name=\"WeirdNested\" xmlns=\"http://jbpm.org/4.3/jpdl\">" + 
    		"   <start name=\"start1\">" + 
    		"      <transition name=\"to fork2\" to=\"fork2\"/>" + 
    		"   </start>" + 
    		"" + 
    		"   <fork name=\"fork2\">" + 
    		"      <transition name=\"to task1\" to=\"task1\"/>" + 
    		"      <transition name=\"to task2\" to=\"task2\"/>" + 
    		"   </fork>" + 
    		"" + 
    		"   <task assignee=\"otto\" name=\"task1\">" + 
    		"      <transition name=\"to fork1\" to=\"fork1\"/>" + 
    		"   </task>" + 
    		"   <task assignee=\"otto\" name=\"task2\">" + 
    		"      <transition name=\"to join1-via2\" to=\"join1\"/>" + 
    		"   </task>" + 
    		"      " + 
    		"   <fork name=\"fork1\">" + 
    		"      <transition name=\"to task3\" to=\"task3\"/>" + 
    		"      <transition name=\"to join1-via1\" to=\"join1\"/>" + 
    		"   </fork>" + 
    		"" + 
    		"   <join name=\"join1\">" + 
    		"      <transition name=\"to task4\" to=\"task4\"/>" + 
    		"   </join>" + 
    		"" + 
    		"   <task assignee=\"otto\" name=\"task3\"/>" + 
    		"   <task assignee=\"otto\" name=\"task4\"/>" + 
    		"" + 
    		"</process>");

    Execution execution = executionService.startProcessInstanceByKey("WeirdNested");

    List<Task> taskList = taskService.findPersonalTasks("otto");
    assertEquals(2, taskList.size());
    assertActivitiesActive(execution.getId(), "task1", "task2");

    Task task1 = getTask(taskList, "task1");
    taskService.completeTask(task1.getId());

    taskList = taskService.findPersonalTasks("otto");

    assertEquals(2, taskList.size());
    assertActivitiesActive(execution.getId(), "task3", "task2");

    Task task2 = getTask(taskList, "task2");
    taskService.completeTask(task2.getId());

    taskList = taskService.findPersonalTasks("otto");

    assertEquals(2, taskList.size());
    assertActivitiesActive(execution.getId(), "task3", "task4");

    assertProcessInstanceActive(execution.getId());

  }

}
