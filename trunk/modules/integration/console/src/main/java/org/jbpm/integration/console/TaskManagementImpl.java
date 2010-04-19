/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
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
package org.jbpm.integration.console;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.jboss.bpm.console.client.model.TaskRef;
import org.jboss.bpm.console.server.integration.TaskManagement;
import org.jbpm.api.TaskService;
import org.jbpm.api.task.Participation;
import org.jbpm.api.task.Task;

/**
 * @author Heiko.Braun <heiko.braun@jboss.com>
 */
public class TaskManagementImpl extends JBPMIntegration implements TaskManagement {
  
  
  public TaskManagementImpl() {
    super();
  }

  public List<TaskRef> getAssignedTasks(String idRef) {
    TaskService taskService = this.processEngine.get(TaskService.class);
    List<TaskRef> results = new ArrayList<TaskRef>();

    List<Task> assignedTasks = taskService.findPersonalTasks(idRef);
    adoptTasks(assignedTasks, results);
    return results;
  }

  public List<TaskRef> getUnassignedTasks(String idRef, String participationType) {
    TaskService taskService = this.processEngine.get(TaskService.class);
    List<TaskRef> results = new ArrayList<TaskRef>();

    if (null == participationType || participationType.equals(Participation.CANDIDATE)) {
      List<Task> groupTasks = taskService.findGroupTasks(idRef);
      adoptTasks(groupTasks, results);
    } else {
      throw new IllegalArgumentException("Unknown participation type: " + participationType);
    }

    return results;
  }

  private void adoptTasks(List<Task> tasks, List<TaskRef> results) {
    for (Task t0 : tasks) {
      results.add(ModelAdaptor.adoptTask(t0));
    }
  }

  public TaskRef getTaskById(long taskId) {
    TaskService taskService = this.processEngine.get(TaskService.class);
    Task t0 = taskService.getTask(Long.toString(taskId));
    return ModelAdaptor.adoptTask(t0);
  }

  public void assignTask(long taskId, String idRef, String performingUser) {
    TaskService taskService = this.processEngine.get(TaskService.class);
    taskService.assignTask(Long.toString(taskId), idRef);
  }

  public void releaseTask(long taskId, String performingUser) {
    TaskService taskService = this.processEngine.get(TaskService.class);
    taskService.assignTask(Long.toString(taskId), null);
  }

  public void completeTask(long taskId, Map data, String performingUser) {
    TaskService taskService = this.processEngine.get(TaskService.class);
    if (data != null)
      taskService.setVariables(Long.toString(taskId), data);

    taskService.completeTask(Long.toString(taskId));
  }

  public void completeTask(long taskId, String outcome, Map data, String performingUser) {
    TaskService taskService = this.processEngine.get(TaskService.class);
    if (data != null)
      taskService.setVariables(Long.toString(taskId), data);

    taskService.completeTask(Long.toString(taskId), outcome);
  }

}
