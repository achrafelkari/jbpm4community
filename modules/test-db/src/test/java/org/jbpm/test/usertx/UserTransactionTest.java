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
package org.jbpm.test.usertx;

import java.util.List;

import org.jbpm.api.ExecutionService;
import org.jbpm.api.ProcessInstance;
import org.jbpm.api.TaskService;
import org.jbpm.api.cmd.Command;
import org.jbpm.api.cmd.Environment;
import org.jbpm.api.listener.EventListener;
import org.jbpm.api.listener.EventListenerExecution;
import org.jbpm.api.task.Task;
import org.jbpm.test.JbpmTestCase;


/**
 * @author Tom Baeyens
 */
public class UserTransactionTest extends JbpmTestCase {
  
  public static class TaskSignalCmd implements Command<Void> {
    private static final long serialVersionUID = 1L;
    String executionId;
    public TaskSignalCmd(String executionId) {
      this.executionId = executionId;
    }
    public Void execute(Environment environment) throws Exception {
      TaskService taskService = environment.get(TaskService.class);
      Task task = taskService.newTask();
      taskService.saveTask(task);
      
      ExecutionService executionService = environment.get(ExecutionService.class);
      executionService.signalExecutionById(executionId);
      
      return null;
    }
  }

  public void testUserTransaction() {
    deployJpdlXmlString(
      "<process name='UserTransaction'>" +
      "  <start>" +
      "    <transition to='a' />" +
      "  </start>" +
      "  <state name='a'>" +
      "    <transition to='b' />" +
      "  </state>" +
      "  <state name='b' />" +
      "</process>"
    );

    processEngine.execute(new Command<Void>() {
      private static final long serialVersionUID = 1L;
      public Void execute(Environment environment) throws Exception {
        TaskService taskService = environment.get(TaskService.class);
        Task task = taskService.newTask();
        taskService.saveTask(task);
        
        ExecutionService executionService = environment.get(ExecutionService.class);
        executionService.startProcessInstanceByKey("UserTransaction");
        
        return null;
      }
    });

    List<Task> taskList = taskService.createTaskQuery().list();
    assertEquals(1, taskList.size());
    
    List<ProcessInstance> processInstanceList = executionService.createProcessInstanceQuery().list();
    assertEquals(1, processInstanceList.size());
    
    ProcessInstance processInstance = processInstanceList.get(0);
    assertTrue(processInstance.isActive("a"));

    processEngine.execute(new TaskSignalCmd(processInstance.getId()));

    taskList = taskService.createTaskQuery().list();
    assertEquals(2, taskList.size());
    
    processInstanceList = executionService.createProcessInstanceQuery().list();
    assertEquals(1, processInstanceList.size());

    processInstance = processInstanceList.get(0);
    assertTrue(processInstance.isActive("b"));

    // delete tasks
    for (Task task: taskService.createTaskQuery().list()) {
      taskService.deleteTaskCascade(task.getId());
    }
  }
  
  public static class Thrower implements EventListener {
    private static final long serialVersionUID = 1L;
    public void notify(EventListenerExecution execution) throws Exception {
      throw new Exception("regards from inside process execution");
    }
  }
  
  public void testUserTransactionWithProcessException() {
    deployJpdlXmlString(
      "<process name='UserTransactionWithProcessException'>" +
      "  <start>" +
      "    <transition to='a' />" +
      "  </start>" +
      "  <state name='a'>" +
      "    <transition to='b'>" +
      "      <event-listener class='"+Thrower.class.getName()+"' />" +
      "    </transition>" +
      "  </state>" +
      "  <state name='b' />" +
      "</process>"
    );

    processEngine.execute(new Command<Void>() {
      private static final long serialVersionUID = 1L;
      public Void execute(Environment environment) throws Exception {
        TaskService taskService = environment.get(TaskService.class);
        Task task = taskService.newTask();
        taskService.saveTask(task);
        
        ExecutionService executionService = environment.get(ExecutionService.class);
        executionService.startProcessInstanceByKey("UserTransactionWithProcessException");
        
        return null;
      }
    });

    List<Task> taskList = taskService.createTaskQuery().list();
    assertEquals(1, taskList.size());
    
    List<ProcessInstance> processInstanceList = executionService.createProcessInstanceQuery().list();
    assertEquals(1, processInstanceList.size());
    
    ProcessInstance processInstance = processInstanceList.get(0);
    assertTrue(processInstance.isActive("a"));

    try {
      processEngine.execute(new TaskSignalCmd(processInstance.getId()));
      fail("expected exception");
    } catch (Exception e) {
      // OK
    }

    taskList = taskService.createTaskQuery().list();
    assertEquals(1, taskList.size());
    
    processInstanceList = executionService.createProcessInstanceQuery().list();
    assertEquals(1, processInstanceList.size());

    processInstance = processInstanceList.get(0);
    assertTrue(processInstance.isActive("a"));

    // delete tasks
    for (Task task: taskService.createTaskQuery().list()) {
      taskService.deleteTaskCascade(task.getId());
    }
  }

  public static class SignalWithUserExceptionCmd implements Command<Void> {
    private static final long serialVersionUID = 1L;
    String executionId;
    public SignalWithUserExceptionCmd(String executionId) {
      this.executionId = executionId;
    }
    public Void execute(Environment environment) throws Exception {
      TaskService taskService = environment.get(TaskService.class);
      Task task = taskService.newTask();
      taskService.saveTask(task);
      
      ExecutionService executionService = environment.get(ExecutionService.class);
      executionService.signalExecutionById(executionId);
      
      throw new RuntimeException("regards from inside user command"); 
    }
  }

  public void testUserTransactionWithUserException() {
    deployJpdlXmlString(
      "<process name='UserTransaction'>" +
      "  <start>" +
      "    <transition to='a' />" +
      "  </start>" +
      "  <state name='a'>" +
      "    <transition to='b' />" +
      "  </state>" +
      "  <state name='b' />" +
      "</process>"
    );

    processEngine.execute(new Command<Void>() {
      private static final long serialVersionUID = 1L;
      public Void execute(Environment environment) throws Exception {
        TaskService taskService = environment.get(TaskService.class);
        Task task = taskService.newTask();
        taskService.saveTask(task);
        
        ExecutionService executionService = environment.get(ExecutionService.class);
        executionService.startProcessInstanceByKey("UserTransaction");
        
        return null;
      }
    });

    List<Task> taskList = taskService.createTaskQuery().list();
    assertEquals(1, taskList.size());
    
    List<ProcessInstance> processInstanceList = executionService.createProcessInstanceQuery().list();
    assertEquals(1, processInstanceList.size());
    
    ProcessInstance processInstance = processInstanceList.get(0);
    assertTrue(processInstance.isActive("a"));

    try {
      processEngine.execute(new SignalWithUserExceptionCmd(processInstance.getId()));
      fail("expected exception");
    } catch (Exception e) {
      // OK
    }

    taskList = taskService.createTaskQuery().list();
    assertEquals(1, taskList.size());
    
    processInstanceList = executionService.createProcessInstanceQuery().list();
    assertEquals(1, processInstanceList.size());

    processInstance = processInstanceList.get(0);
    assertTrue(processInstance.isActive("a"));

    // delete tasks
    for (Task task: taskService.createTaskQuery().list()) {
      taskService.deleteTaskCascade(task.getId());
    }
  }

}
