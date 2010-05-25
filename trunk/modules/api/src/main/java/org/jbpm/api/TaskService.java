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
package org.jbpm.api;

import org.jbpm.api.history.HistoryComment;
import org.jbpm.api.history.HistoryActivityInstance;
import org.jbpm.api.task.Participation;
import org.jbpm.api.task.Task;

import java.util.List;
import java.util.Map;
import java.util.Set;

/** task management.
 *
 * @author Tom Baeyens
 * @author Alejandro Guizar
 * @author Heiko Braun <heiko.braun@jboss.com>
 */
public interface TaskService {

  /** Creates a task.
   * The returned task will be transient.
   * Use {@link #saveTask(Task)} to persist the task. Only
   * after the invocation of {@link #saveTask(Task)}, the
   * {@link Task#getId()} property will be initialized. */
  Task newTask();

  /** Creates a new subtask for the given task.
   * Make sure that the parent task is saved before the
   * {@link Task#getId() Id} is taken.
   * The returned task will be transient.
   * Use {@link #saveTask(Task)} to persist the task. */
  Task newTask(String parentTaskId);

  /** Saves the given task to persistent storage.
   * @return the taskId */
  String saveTask(Task task);

  /** Retrieves the task with the given identifier from persistent storage.
   * If no task with the given identifier exists,
   * the call returns <code>null</code>. */
  Task getTask(String taskId);

  /** assigns this task to the given assignee. */
  void assignTask(String taskId, String userId);

  /** taking this task will prevent all other candidates from
   * taking and working on this task.
   * The inverse operation of putting the task back into the group
   * for someone else to take it, can be done by providing a null value
   * for userId in the {@link #assignTask(String, String)} method:
   * <code>taskService.assignTask(taskId, null);</code>
   * @throws JbpmException if this task already has been taken. */
  void takeTask(String taskId, String userId);

  /** Deletes this task, marks the related history task as completed.
   * If the task was created in the context
   * of a process execution, this operation may result in a process instance
   * being triggered. */
  void completeTask(String taskId);

  /** Deletes this task and marks the related history task as completed. The
   * given variables are created (or they overwrite existing values) as task
   * variables. If the task was created in the context of a process execution,
   * this operation may result in a process instance being triggered to
   * continue.*/
  void completeTask(String taskId, Map<String, ?> variables);

  /** Deletes this task, marks the related history task as completed
   * with the specified outcome.  If the task was created in the context
   * of a process execution, this operation may result in a process instance
   * being triggered. The outcome in that case corresponds to an outgoing
   * transition in the process. */
  void completeTask(String taskId, String outcome);

  /** Deletes this task, marks the related history task as completed with the
   * specified outcome. The given variables are created (or they overwrite
   * existing values) as task variables. If the task was created in the context
   * of a process execution, this operation may result in a process instance
   * being triggered. The outcome in that case corresponds to an outgoing
   * transition in the process. */
  void completeTask(String taskId, String outcome, Map<String, ?> variables);

  /** Deletes the task without completing it.
   * The history information is kept in the DB.
   * If this task was created in the context of a process execution,
   * the execution remains active and the {@link ExecutionService#signalExecutionById(String)}
   * is to be given explicitly. */
  void deleteTask(String taskId);

  /** deletes this task, including all history information */
  void deleteTaskCascade(String taskId);

  /** Deletes the task without completing indicating the reason.  Example reasons
   * could be: "failed", "error", "exited", "obsolete" or "deleted".
   * The history information is kept in the DB.
   * The reason ends up as the state in the {@link HistoryActivityInstance}.
   * If this task was created in the context of a process execution,
   * the execution remains active and the {@link ExecutionService#signalExecutionById(String)}
   * is to be given explicitly. */
  void deleteTask(String taskId, String reason);

  /** add a role to a given task.
   * @param participationType specifies the kind of involvement of the participatingUser
   * in this task. see {@link Participation} for default constants. */
  void addTaskParticipatingUser(String taskId, String userId, String participationType);

  /** add a role to a given task.
   * @param participationType specifies the kind of involvement of the participatingUser
   * in this task. see {@link Participation} for default constants. */
  void addTaskParticipatingGroup(String taskId, String groupId, String participationType);

  /** get roles related to a given task. */
  List<Participation> getTaskParticipations(String taskId);

  /** removes a role to a given task.  Nothing happens (no exception) if
   * the role does not exist.
   * @param participationType specifies the kind of involvement of the participatingUser
   * in this task. see {@link Participation} for default constants. */
  void removeTaskParticipatingUser(String taskId, String userId, String participationType);

  /** removes a role to a given task.  Nothing happens (no exception) if
   * the role does not exist.
   * @param participationType specifies the kind of involvement of the participatingUser
   * in this task. see {@link Participation} for default constants. */
  void removeTaskParticipatingGroup(String taskId, String groupId, String participationType);

  /** create a new query for tasks */
  TaskQuery createTaskQuery();

  /** list of tasks that are assigned to the given user.
   * Returns an empty list in case no such tasks exist. */
  List<Task> findPersonalTasks(String userId);

  /** list of tasks that can be taken by the given user.
   * Returns an empty list in case no such tasks exist. */
  List<Task> findGroupTasks(String userId);

  /** get the subtasks for this task.  Only goes one level deep at a time. */
  List<Task> getSubTasks(String taskId);

  /** add a comment to a task */
  HistoryComment addTaskComment(String taskId, String message);

  /** get the list of comments made to a task.  this will
   * fetch all the comments and recursively all replies to those
   * comments. */
  List<HistoryComment> getTaskComments(String taskId);

  /** add a reply to another comment */
  HistoryComment addReplyComment(String commentId, String message);

  /** delete a comment.
   * this will recursively delete all replies to this comment. */
  void deleteComment(String commentId);

  /** creates or overwrites the variable values on the given task */
  void setVariables(String taskId, Map<String, ?> variables);

  /** retrieves a variable */
  Object getVariable(String taskId, String variableName);

  /** all the variables visible in the given task */
  Set<String> getVariableNames(String taskId);

  /** retrieves a map of variables */
  Map<String, Object> getVariables(String taskId, Set<String> variableNames);

  /** the set of possible valid outcomes for this task.
   * An empty set means that any value is possible. */
  Set<String> getOutcomes(String taskId);
}
