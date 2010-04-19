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
package org.jbpm.api.task;

import java.io.Serializable;
import java.util.Date;

import org.jbpm.api.JbpmException;
import org.jbpm.api.TaskService;
import org.jbpm.api.history.HistoryTask;

/** a runtime task.
 * 
 * In contrast to {@link HistoryTask}, a Task only represents the runtime 
 * state of a task and hence it will be deleted after it is completed.
 * 
 * @author Tom Baeyens
 */
public interface Task extends Serializable {
  
  /** task is waiting for someone to work on it and completed it */
  public static final String STATE_OPEN = "open";
  
  /** task is done */
  public static final String STATE_COMPLETED = "completed";
  
  /** task doesn't show up in task lists as something is wrong 
   * with the related process instance that requires manual 
   * intervention first. */
  public static final String STATE_SUSPENDED = "suspended";
  
  /** the unique id for this task that is used as a reference in the service methods */
  String getId();

  /** the short display name of this task.  Originally, this might come 
   * from the process file, but it can be changed at runtime for individual 
   * tasks. */
  String getName();
  
  /** change the short display name of this tasks. 
   * Updates require you to invoke {@link TaskService#saveTask(Task)} afterwards. */
  void setName(String name);

  /** longer description of this task */
  String getDescription();

  /** update the longer description of this task.
   * Updates like this require you to invoke {@link TaskService#saveTask(Task)} afterwards. */
  void setDescription(String description);

  /** the person responsible for completion of this task. */
  String getAssignee();
  
  /** update the assignee of this task.
   * Updates like this require you to invoke {@link TaskService#saveTask(Task)} afterwards. */
  void setAssignee(String assignee);

  /** date and time when this task was created */
  Date getCreateTime();

  /** date and time when this task must be completed. This might be null. */
  Date getDuedate();

  /** update the date and time when this task must be completed.
   * Updates like this require you to invoke {@link TaskService#saveTask(Task)} afterwards. */
  void setDuedate(Date duedate);

  /** the priority of this task.  This is only a data item for user purposes.  
   * The engine doesn't do anything different for different priorities. */
  int getPriority();

  /** update the priority.
   * Updates like this require you to invoke {@link TaskService#saveTask(Task)} afterwards. */
  void setPriority(int priority);
  
  /** 
   * The progress of this task. This is a data item for user purposes and 
   * doesn't influence the functionality of the engine.
   */
  Integer getProgress();
  
  /**
   * Change the progress of this task.
   * Updates like this require you to invoke {@link TaskService#saveTask(Task)} afterwards.
   * 
   * <b>IMPORTANT</b>: Only values between 0 and 100 are accepted. 
   *                   Using other values will cause a {@link JbpmException}.
   */
  void setProgress(Integer progress);
  
  /** reference to the execution or null if this task is unrelated to an execution */
  String getExecutionId();
  
  /** reference to the activity or null if this task is unrelated to an activity */
  String getActivityName();
  
  /** name of the resource in the deployment for the form that is associated 
   * to this task. */
  String getFormResourceName();
}