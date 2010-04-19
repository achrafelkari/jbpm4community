/*
 * JBoss, Home of Professional Open Source
 * Copyright 2005, JBoss Inc., and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jbpm.bpmn.test.subprocess;

import java.util.List;

import org.jbpm.api.ProcessInstance;
import org.jbpm.api.TaskQuery;
import org.jbpm.api.task.Task;
import org.jbpm.test.JbpmTestCase;
import org.jbpm.test.assertion.CollectionAssertions;

/**
 * @author Joram Barrez
 */
public class SubProcessTest extends JbpmTestCase {
  
  private static final String SIMPLE_SUBPROCESS = 
    "<definitions>" +
    "  <process id='simpleSubProcess'>" +
    "    <startEvent id='theStart' />" +
    "    <sequenceFlow id='flow1' sourceRef='theStart' targetRef='mySubProcess' />" +
    "    <subProcess id='mySubProcess'>" +
    "      <startEvent id='subProcessStart' />" +
    "      <sequenceFlow id='subFlow1' sourceRef='subProcessStart' targetRef='subTask' />" +
    "      <userTask id='subTask' name='importantTask' />" +
    "      <sequenceFlow id='subFlow2' sourceRef='subTask' targetRef='subEnd' />" +
    "      <endEvent id='subEnd' />" +
    "    </subProcess>" +
    "    <sequenceFlow id='flow2' sourceRef='mySubProcess' targetRef='theEnd' />" +
    "    <endEvent id='theEnd' />" +
    "  </process>" +
    "</definitions>";
  
  private static final String SIMPLE_PARALLEL_SUBPROCESS = 
    "<definitions>" +
    "  <process id='simpleParallelSubProcess'>" +
    "    <startEvent id='theStart' />" +
    "    <sequenceFlow id='flow1' sourceRef='theStart' targetRef='mySubProcess' />" +
    "    <subProcess id='mySubProcess'>" +
    "      <startEvent id='subProcessStart1' />" +
    "      <sequenceFlow id='subFlow1' sourceRef='subProcessStart1' targetRef='subTask1' />" +
    "      <userTask id='subTask1' name='importantTask1' />" +
    "      <sequenceFlow id='subFlow2' sourceRef='subTask1' targetRef='subEnd1' />" +
    "      <endEvent id='subEnd1' />" +
    "      <startEvent id='subProcessStart2' />" +
    "      <sequenceFlow id='subFlow3' sourceRef='subProcessStart2' targetRef='subTask2' />" +
    "      <userTask id='subTask2' name='importantTask2' />" +
    "      <sequenceFlow id='subFlow4' sourceRef='subTask2' targetRef='subEnd2' />" +
    "      <endEvent id='subEnd2' />" +
    "    </subProcess>" +
    "    <sequenceFlow id='flow2' sourceRef='mySubProcess' targetRef='theEnd' />" +
    "    <endEvent id='theEnd' />" +
    "  </process>" +
    "</definitions>";
  
  private static final String TWO_PARALLEL_SUBPROCESSES = 
    "<definitions>" +
    "  <process id='twoParallelSubProcesses'>" +
    "    <startEvent id='theStart' />" +
    "    <sequenceFlow id='flow1' sourceRef='theStart' targetRef='subProcess1' />" +
    "    <sequenceFlow id='flow2' sourceRef='theStart' targetRef='subProcess2' />" +
    "    <subProcess id='subProcess1'>" +
    "      <startEvent id='subProcessStart1' />" +
    "      <sequenceFlow id='subFlow1' sourceRef='subProcessStart1' targetRef='subTask1' />" +
    "      <userTask id='subTask1' name='importantTask' />" +
    "      <sequenceFlow id='subFlow2' sourceRef='subTask1' targetRef='subEnd1' />" +
    "      <endEvent id='subEnd1' />" +
    "    </subProcess>" +
    "    <subProcess id='subProcess2'>" +
    "      <startEvent id='subProcessStart2' />" +
    "      <sequenceFlow id='subFlow3' sourceRef='subProcessStart2' targetRef='subTask2' />" +
    "      <userTask id='subTask2' name='evenMoreImportantTask' />" +
    "      <sequenceFlow id='subFlow4' sourceRef='subTask2' targetRef='subEnd2' />" +
    "      <endEvent id='subEnd2' />" +
    "      <startEvent id='subProcessStart3' />" +
    "      <sequenceFlow id='subFlow5' sourceRef='subProcessStart3' targetRef='subTask3' />" +
    "      <userTask id='subTask3' name='possiblyTheMostImportantTask' />" +
    "      <sequenceFlow id='subFlow6' sourceRef='subTask3' targetRef='subEnd3' />" +
    "      <endEvent id='subEnd3' />" +
    "    </subProcess>" +
    "    <sequenceFlow id='flow3' sourceRef='subProcess1' targetRef='taskAfterSubProcess1' />" +
    "    <userTask id='taskAfterSubProcess1' name='taskAfterSubProcess1' />" +
    "    <sequenceFlow id='flow5' sourceRef='taskAfterSubProcess1' targetRef='theEnd' />" +
    "    <sequenceFlow id='flow4' sourceRef='subProcess2' targetRef='taskAfterSubProcess2' />" +
    "    <userTask id='taskAfterSubProcess2' name='taskAfterSubProcess2' />" +
    "    <sequenceFlow id='flow5' sourceRef='taskAfterSubProcess2' targetRef='theEnd' />" +
    "    <endEvent id='theEnd' />" +
    "  </process>" +
    "</definitions>";
  
  private static final String NESTED_SUBPROCESS = 
    "<definitions>" +
    "  <process id='nestedSubProcess'>" +
    "    <startEvent id='theStart' />" +
    "    <sequenceFlow id='flow1' sourceRef='theStart' targetRef='mySubProcess' />" +
    "    <subProcess id='mySubProcess'>" +
    // First path of subprocess is just a sequential start-task-end
    "      <startEvent id='subProcessStart1' />" +
    "      <sequenceFlow id='subFlow1' sourceRef='subProcessStart1' targetRef='subTask1' />" +
    "      <userTask id='subTask1' name='task1' />" +
    "      <sequenceFlow id='subFlow2' sourceRef='subTask1' targetRef='subEnd1' />" +
    "      <endEvent id='subEnd1' />" +
    // Second path has a nested subprocess
    "        <startEvent id='subProcessStart2' />" +
    "        <sequenceFlow id='subFlow3' sourceRef='subProcessStart2' targetRef='nestedSubProcess' />" +
    "      <subProcess id='nestedSubProcess'>" +
    "        <userTask id='subTask3' name='task2' />" +
    "        <sequenceFlow id='subFlow4' sourceRef='subTask3' targetRef='subTask4' />" +
    "        <userTask id='subTask4' name='task3' />" +
    "        <sequenceFlow id='subFlow5' sourceRef='subTask4' targetRef='nestedEnd' />" +
    "        <endEvent id='nestedEnd' />" +
    "      </subProcess>" +
    "      <sequenceFlow id='subFlow6' sourceRef='nestedSubProcess' targetRef='subEnd2' />" +
    "      <endEvent id='subEnd2' />" +
    "    </subProcess>" +
    "    <sequenceFlow id='flow3' sourceRef='mySubProcess' targetRef='theEnd' />" +
    "    <endEvent id='theEnd' />" +
    "  </process>" +
    "</definitions>";
  
  private static final String SUBPROCESS_WITH_UNJOINED_PATHS = 
    "<definitions>" +
    "  <process id='unjoinedPaths'>" +
    "    <startEvent id='theStart' />" +
    "    <sequenceFlow id='flow1' sourceRef='theStart' targetRef='mySubProcess' />" +
    "    <subProcess id='mySubProcess'>" +
    "      <startEvent id='subProcessStart' />" +
    "      <sequenceFlow id='subFlow1' sourceRef='subProcessStart' targetRef='splitInSubProcess' />" +
    "      <parallelGateway id='splitInSubProcess' />" +
    "      <sequenceFlow id='subFlow2' sourceRef='splitInSubProcess' targetRef='subTask1' />" +
    "      <sequenceFlow id='subFlow3' sourceRef='splitInSubProcess' targetRef='subTask2' />" +
    "      <sequenceFlow id='subFlow4' sourceRef='splitInSubProcess' targetRef='subTask3' />" +
    "      <userTask id='subTask1' name='firstTask' />" +
    "      <userTask id='subTask2' name='secondTask' />" +
    "      <userTask id='subTask3' name='thirdTask' />" +
    "      <sequenceFlow id='subFlow4' sourceRef='subTask1' targetRef='subEnd1' />" +
    "      <endEvent id='subEnd1' />" +
    "      <sequenceFlow id='subFlow5' sourceRef='subTask2' targetRef='subEnd2' />" +
    "      <endEvent id='subEnd2' />" +
    "      <sequenceFlow id='subFlow6' sourceRef='subTask3' targetRef='subEnd3' />" +
    "      <endEvent id='subEnd3' />" +
    "    </subProcess>" +
    "    <sequenceFlow id='flow2' sourceRef='mySubProcess' targetRef='theEnd' />" +
    "    <endEvent id='theEnd' />" +
    "  </process>" +
    "</definitions>";
  
  /**
   * Tests a simple sub process with sequentially one start, a user task and an end.
   */
  public void testSimpleSubProcess() {
    deployBpmn2XmlString(SIMPLE_SUBPROCESS);
    ProcessInstance processInstance = executionService.startProcessInstanceByKey("simpleSubProcess");
    
    Task task = taskService.createTaskQuery().uniqueResult();
    assertEquals("importantTask", task.getName());
    taskService.completeTask(task.getId());
    
    assertProcessInstanceEnded(processInstance);
  }
  
  /**
   * Tests a simple sub process that has 2 parallel paths which are sequential on itself.
   */
  public void testSimpleParallelSubProcess() {
    deployBpmn2XmlString(SIMPLE_PARALLEL_SUBPROCESS);
    ProcessInstance processInstance = executionService.startProcessInstanceByKey("simpleParallelSubProcess");
    
    TaskQuery query = taskService.createTaskQuery().processInstanceId(processInstance.getId()).orderAsc(TaskQuery.PROPERTY_NAME);
    List<Task> tasks = query.list();
    assertEquals(2, tasks.size());
    assertEquals("importantTask1", tasks.get(0).getName());
    assertEquals("importantTask2", tasks.get(1).getName());
    
    // After task completion, the subprocess should still be active
    taskService.completeTask(tasks.get(0).getId());
    processInstance = executionService.findProcessInstanceById(processInstance.getId());
    CollectionAssertions.assertContainsSameElements(processInstance.findActiveActivityNames(), "subTask2");
    
    taskService.completeTask(tasks.get(1).getId());
    assertProcessInstanceEnded(processInstance);
  }
  
  /**
   * Tests a process that splits after process start in two separate sub-processes
   */
  public void testTwoParallelSubProcesses() {
    deployBpmn2XmlString(TWO_PARALLEL_SUBPROCESSES);
    ProcessInstance pi = executionService.startProcessInstanceByKey("twoParallelSubProcesses");
    
    TaskQuery query = taskService.createTaskQuery().processInstanceId(pi.getId()).orderAsc(TaskQuery.PROPERTY_NAME);
    List<Task> tasks = query.list();
    assertEquals(3, tasks.size());
    assertEquals("evenMoreImportantTask", tasks.get(0).getName());
    assertEquals("importantTask", tasks.get(1).getName());
    assertEquals("possiblyTheMostImportantTask", tasks.get(2).getName());
    
    // complete evenMoreImportantTask
    taskService.completeTask(tasks.get(0).getId()); 
    pi = executionService.findProcessInstanceById(pi.getId());
    CollectionAssertions.assertContainsSameElements(pi.findActiveActivityNames(), "subTask1", "subTask3");
    
    // complete possiblyTheMostImportantTask
    taskService.completeTask(tasks.get(2).getId());
    pi = executionService.findProcessInstanceById(pi.getId());
    CollectionAssertions.assertContainsSameElements(pi.findActiveActivityNames(), "subTask1", "taskAfterSubProcess2");
    
    // complete importantTask
    taskService.completeTask(tasks.get(1).getId()); // complete importantTask
    pi = executionService.findProcessInstanceById(pi.getId());
    CollectionAssertions.assertContainsSameElements(pi.findActiveActivityNames(), "taskAfterSubProcess1", "taskAfterSubProcess2");
    
    // Complete 2 tasks after subprocesses
    tasks = query.list();
    assertEquals(2, tasks.size());
    taskService.completeTask(tasks.get(0).getId());
    assertProcessInstanceActive(pi);
    taskService.completeTask(tasks.get(1).getId());
    assertProcessInstanceEnded(pi);
  }
  
  public void testNestedSubProcess() {
    deployBpmn2XmlString(NESTED_SUBPROCESS);
    ProcessInstance pi = executionService.startProcessInstanceByKey("nestedSubProcess");
    
    TaskQuery query = taskService.createTaskQuery().processInstanceId(pi.getId()).orderAsc(TaskQuery.PROPERTY_NAME);
    List<Task> tasks = query.list();
    assertEquals(2, tasks.size());
    assertEquals("task1", tasks.get(0).getName());
    assertEquals("task2", tasks.get(1).getName());
    
    // Complete task2 -> will activate task3
    taskService.completeTask(tasks.get(1).getId());
    tasks = query.list();
    assertEquals(2, tasks.size());
    assertEquals("task1", tasks.get(0).getName());
    assertEquals("task3", tasks.get(1).getName());
    
    // Complete task3 -> will finish nested subprocess
    taskService.completeTask(tasks.get(1).getId());
    tasks = query.list();
    assertEquals(1, tasks.size());
    assertEquals("task1", tasks.get(0).getName());
    pi = executionService.findProcessInstanceById(pi.getId());
    CollectionAssertions.assertContainsSameElements(pi.findActiveActivityNames(), "subTask1");
    
    // Complete task 1 -> complete process is finished
    taskService.completeTask(tasks.get(0).getId());
    assertProcessInstanceEnded(pi);
  }
  
  /**
   * Tests a process that has a sub-process which has a splitting parallelgatway without 
   * a merging parallel gateway later on. This way, execution state juggling gets a bit trickier.
   */
  public void testSubProcessWithUnjoinedPaths() {
    deployBpmn2XmlString(SUBPROCESS_WITH_UNJOINED_PATHS);
    ProcessInstance pi = executionService.startProcessInstanceByKey("unjoinedPaths");
    
    TaskQuery query = taskService.createTaskQuery().processInstanceId(pi.getId()).orderAsc(TaskQuery.PROPERTY_NAME);
    List<Task> tasks = query.list();
    assertEquals(3, tasks.size());
    assertEquals("firstTask", tasks.get(0).getName());
    assertEquals("secondTask", tasks.get(1).getName());
    assertEquals("thirdTask", tasks.get(2).getName());
    
    taskService.completeTask(tasks.get(0).getId());
    taskService.completeTask(tasks.get(1).getId());
    taskService.completeTask(tasks.get(2).getId());
    assertProcessInstanceEnded(pi);
  }

}
