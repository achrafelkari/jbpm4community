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
package org.jbpm.bpmn.flownodes;

import java.util.Map;

import org.jbpm.api.activity.ActivityExecution;
import org.jbpm.internal.log.Log;
import org.jbpm.pvm.internal.el.Expression;
import org.jbpm.pvm.internal.env.EnvironmentImpl;
import org.jbpm.pvm.internal.history.HistoryEvent;
import org.jbpm.pvm.internal.history.events.TaskActivityStart;
import org.jbpm.pvm.internal.model.ActivityImpl;
import org.jbpm.pvm.internal.model.ExecutionImpl;
import org.jbpm.pvm.internal.session.DbSession;
import org.jbpm.pvm.internal.task.TaskDefinitionImpl;
import org.jbpm.pvm.internal.task.TaskImpl;

/**
 * @author Tom Baeyens
 */
public class UserTaskActivity extends BpmnExternalActivity {

  private static final long serialVersionUID = 1L;

  private static final Log LOG = Log.getLog(UserTaskActivity.class.getName());

  protected TaskDefinitionImpl taskDefinition;

  public void execute(ActivityExecution execution) {
    execute((ExecutionImpl) execution);
  }

  public void execute(ExecutionImpl execution) {
    DbSession dbSession = EnvironmentImpl.getFromCurrent(DbSession.class);
    TaskImpl task = (TaskImpl) dbSession.createTask();
    task.setTaskDefinition(taskDefinition);
    task.setExecution(execution);
    task.setProcessInstance(execution.getProcessInstance());
    task.setSignalling(true);

    Expression assigneeExpression = taskDefinition.getAssigneeExpression();
    if (assigneeExpression != null) {
      Object assignee = assigneeExpression.evaluate(execution);
      task.setAssignee((assignee!=null ? assignee.toString() : null));
    }

    // initialize the name
    if (taskDefinition.getName() != null) {
      task.setName(taskDefinition.getName());
    } else {
      task.setName(execution.getActivityName());
    }

    Expression descriptionExpression = taskDefinition.getDescription();
    if (descriptionExpression!=null) {
      String description = (String) descriptionExpression.evaluate(task);
      task.setDescription(description);
    }

    task.setPriority(taskDefinition.getPriority());
    task.setFormResourceName(taskDefinition.getFormResourceName());

    // save task so that TaskDbSession.findTaskByExecution works for assign
    // event listeners
    dbSession.save(task);
    execution.initializeAssignments(taskDefinition, task);

    HistoryEvent.fire(new TaskActivityStart(task), execution);

    execution.waitForSignal();
  }
  public void signal(ActivityExecution execution, String signalName, Map<String, ? > parameters) throws Exception {
    signal((ExecutionImpl) execution, signalName, parameters);
  }

  public void signal(ExecutionImpl execution, String signalName, Map<String, ? > parameters) throws Exception {
    ActivityImpl activity = execution.getActivity();

    if (parameters != null) {
      execution.setVariables(parameters);
    }

    execution.fire(signalName, activity);

    DbSession taskDbSession = EnvironmentImpl.getFromCurrent(DbSession.class);
    TaskImpl task = (TaskImpl) taskDbSession.findTaskByExecution(execution);
    if (task!=null) {
      task.setSignalling(false);
    }

    execution.setVariable("jbpm_outcome", signalName);
    proceed(execution, findOutgoingSequenceFlow(execution, CONDITIONS_CHECKED));
    
  }

  public TaskDefinitionImpl getTaskDefinition() {
    return taskDefinition;
  }
  public void setTaskDefinition(TaskDefinitionImpl taskDefinition) {
    this.taskDefinition = taskDefinition;
  }
}
