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
package org.jbpm.pvm.internal.svc;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jbpm.api.TaskQuery;
import org.jbpm.api.TaskService;
import org.jbpm.api.history.HistoryComment;
import org.jbpm.api.task.Participation;
import org.jbpm.api.task.Task;
import org.jbpm.pvm.internal.cmd.AddParticipationCmd;
import org.jbpm.pvm.internal.cmd.AddReplyCommentCmd;
import org.jbpm.pvm.internal.cmd.AddTaskCommentCmd;
import org.jbpm.pvm.internal.cmd.AssignTaskCmd;
import org.jbpm.pvm.internal.cmd.CompositeCmd;
import org.jbpm.pvm.internal.cmd.DeleteTaskCmd;
import org.jbpm.pvm.internal.cmd.CompleteTaskCmd;
import org.jbpm.pvm.internal.cmd.CreateTaskQueryCmd;
import org.jbpm.pvm.internal.cmd.DeleteCommentCmd;
import org.jbpm.pvm.internal.cmd.GetOutcomes;
import org.jbpm.pvm.internal.cmd.GetParticipantsCmd;
import org.jbpm.pvm.internal.cmd.GetSubTasksCmd;
import org.jbpm.pvm.internal.cmd.GetTaskCmd;
import org.jbpm.pvm.internal.cmd.GetTaskCommentsCmd;
import org.jbpm.pvm.internal.cmd.GetTaskVariableNamesCmd;
import org.jbpm.pvm.internal.cmd.GetTaskVariablesCmd;
import org.jbpm.pvm.internal.cmd.NewTaskCmd;
import org.jbpm.pvm.internal.cmd.RemoveParticipantCmd;
import org.jbpm.pvm.internal.cmd.SaveTaskCmd;
import org.jbpm.pvm.internal.cmd.SetTaskVariablesCmd;
import org.jbpm.pvm.internal.query.TaskQueryImpl;
import org.jbpm.pvm.internal.task.TaskImpl;

/**
 * @author Alejandro Guizar, Tom Baeyens
 * @author Heiko Braun <heiko.braun@jboss.com>
 */
public class TaskServiceImpl extends AbstractServiceImpl implements TaskService {

  public Task newTask() {
    return commandService.execute(new NewTaskCmd(null));
  }

  public Task getTask(String taskId) {
    return commandService.execute(new GetTaskCmd(taskId));
  }

  public String saveTask(Task task) {
    return commandService.execute(new SaveTaskCmd((TaskImpl) task));
  }

  public void deleteTask(String taskId) {
    commandService.execute(new DeleteTaskCmd(taskId));
  }

  public void deleteTask(String taskId, String reason) {
    commandService.execute(new DeleteTaskCmd(taskId, reason));
  }
  
  public void deleteTaskCascade(String taskId) {
    commandService.execute(new DeleteTaskCmd(taskId, true));
  }

  public void completeTask(String taskId) {
    commandService.execute(new CompleteTaskCmd(taskId));
  }
  
  public void completeTask(String taskId, Map<String, ?> variables) {
    completeTask(taskId, null, variables);
  }

  public void completeTask(String taskId, String outcome) {
    commandService.execute(new CompleteTaskCmd(taskId, outcome));
  }
  
  public void completeTask(String taskId, String outcome, Map<String, ?> variables) {
    SetTaskVariablesCmd setTaskVariablesCmd = new SetTaskVariablesCmd(taskId);
    setTaskVariablesCmd.setVariables(variables);
    CompositeCmd compositeCmd = new CompositeCmd();
    compositeCmd.addCommand(setTaskVariablesCmd);
    compositeCmd.addCommand(new CompleteTaskCmd(taskId, outcome));
    commandService.execute(compositeCmd);
  }

  public void addTaskParticipatingUser(String taskId, String userId, String participation) {
    commandService.execute(new AddParticipationCmd(taskId, null, userId, null, participation));
  }

  public void addTaskParticipatingGroup(String taskId, String groupId, String participation) {
    commandService.execute(new AddParticipationCmd(taskId, null, null, groupId, participation));
  }

  public List<Participation> getTaskParticipations(String taskId) {
    return commandService.execute(new GetParticipantsCmd(taskId, null));
  }

  public void removeTaskParticipatingUser(String taskId, String userId, String participation) {
    commandService.execute(new RemoveParticipantCmd(taskId, null, userId, null, participation));
  }

  public void removeTaskParticipatingGroup(String taskId, String groupId, String participation) {
    commandService.execute(new RemoveParticipantCmd(taskId, null, null, groupId, participation));
  }


  public List<Task> findPersonalTasks(String userId) {
    return createTaskQuery()
        .assignee(userId)
        .orderDesc(TaskQuery.PROPERTY_PRIORITY)        
        .list();
  }

  public List<Task> findGroupTasks(String userId) {
    return createTaskQuery()
        .candidate(userId)
        .orderDesc(TaskQuery.PROPERTY_PRIORITY)        
        .list();
  }

  public TaskQuery createTaskQuery() {
    TaskQueryImpl query = commandService.execute(new CreateTaskQueryCmd());
    query.setCommandService(commandService);
    return query;
  }

  public List<Task> getSubTasks(String taskId) {
    return commandService.execute(new GetSubTasksCmd(taskId));
  }

  public Task newTask(String parentTaskId) {
    return commandService.execute(new NewTaskCmd(parentTaskId));
  }

  public HistoryComment addTaskComment(String taskId, String message) {
    return commandService.execute(new AddTaskCommentCmd(taskId, message));
  }

  public List<HistoryComment> getTaskComments(String taskId) {
    return commandService.execute(new GetTaskCommentsCmd(taskId));
  }

  public void deleteComment(String commentId) {
    commandService.execute(new DeleteCommentCmd(commentId));
  }

  public HistoryComment addReplyComment(String commentId, String message) {
    return commandService.execute(new AddReplyCommentCmd(commentId, message));
  }

  public void assignTask(String taskId, String userId) {
    commandService.execute(new AssignTaskCmd(taskId, userId));
  }

  public void takeTask(String taskId, String userId) {
    commandService.execute(new AssignTaskCmd(taskId, userId, true));
  }

  public Object getVariable(String taskId, String variableName) {
    Set<String> variableNames = new HashSet<String>();
    variableNames.add(variableName);
    GetTaskVariablesCmd cmd = new GetTaskVariablesCmd(taskId, variableNames);
    Map<String, Object> variables = commandService.execute(cmd);
    return variables.get(variableName);
  }

  public Set<String> getVariableNames(String taskDbid) {
    return commandService.execute(new GetTaskVariableNamesCmd(taskDbid));
  }

  public Map<String, Object> getVariables(String taskDbid, Set<String> variableNames) {
    return commandService.execute(new GetTaskVariablesCmd(taskDbid, variableNames));
  }

  public void setVariables(String taskDbid, Map<String, ?> variables) {
    SetTaskVariablesCmd cmd = new SetTaskVariablesCmd(taskDbid);
    cmd.setVariables(variables);
    commandService.execute(cmd);
  }

  public Set<String> getOutcomes(String taskId) {
    return commandService.execute(new GetOutcomes(taskId));
  }
}
