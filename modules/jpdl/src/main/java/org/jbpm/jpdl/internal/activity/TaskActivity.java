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
package org.jbpm.jpdl.internal.activity;

import java.util.List;
import java.util.Map;

import org.jbpm.api.JbpmException;
import org.jbpm.api.activity.ActivityExecution;
import org.jbpm.api.model.Transition;
import org.jbpm.pvm.internal.cal.Duration;
import org.jbpm.pvm.internal.el.Expression;
import org.jbpm.pvm.internal.env.EnvironmentImpl;
import org.jbpm.pvm.internal.history.HistoryEvent;
import org.jbpm.pvm.internal.history.events.TaskActivityStart;
import org.jbpm.pvm.internal.model.ActivityImpl;
import org.jbpm.pvm.internal.model.ExecutionImpl;
import org.jbpm.pvm.internal.session.DbSession;
import org.jbpm.pvm.internal.task.ParticipationImpl;
import org.jbpm.pvm.internal.task.SwimlaneDefinitionImpl;
import org.jbpm.pvm.internal.task.SwimlaneImpl;
import org.jbpm.pvm.internal.task.TaskConstants;
import org.jbpm.pvm.internal.task.TaskDefinitionImpl;
import org.jbpm.pvm.internal.task.TaskImpl;

/**
 * @author Tom Baeyens
 * @author Alejandro Guizar
 * @author Ronald van Kuijk
 * @author Maciej Swiderski
 */
public class TaskActivity extends JpdlExternalActivity {

  private static final long serialVersionUID = 1L;
  
  protected TaskDefinitionImpl taskDefinition;
  
  public void execute(ActivityExecution execution) {
    execute((ExecutionImpl)execution);
  }

  public void execute(ExecutionImpl execution) {
    DbSession dbSession = EnvironmentImpl.getFromCurrent(DbSession.class);
    TaskImpl task = dbSession.createTask();
    task.setTaskDefinition(taskDefinition);
    task.setExecution(execution);
    task.setProcessInstance(execution.getProcessInstance());
    task.setSignalling(true);
    
    // initialize the name
    if (taskDefinition.getName()!=null) {
      task.setName(taskDefinition.getName());
    }
    else {
      task.setName(execution.getActivityName());
    }

    Expression descriptionExpression = taskDefinition.getDescription();
    if (descriptionExpression != null) {
      String description = (String) descriptionExpression.evaluate(task);
      task.setDescription(description);
    }
    task.setPriority(taskDefinition.getPriority());
    task.setFormResourceName(taskDefinition.getFormResourceName());

    // calculate the due date of the task based on the due date duration
    String dueDateDescription = taskDefinition.getDueDateDescription();
    if (dueDateDescription != null) {
      task.setDuedate(Duration.calculateDueDate(dueDateDescription));
    }

    // save task so that TaskDbSession.findTaskByExecution works for assign event listeners
    dbSession.save(task);

    SwimlaneDefinitionImpl swimlaneDefinition = taskDefinition.getSwimlaneDefinition();
    if (swimlaneDefinition!=null) {
      SwimlaneImpl swimlane = execution.getInitializedSwimlane(swimlaneDefinition);
      task.setSwimlane(swimlane);
      
      // copy the swimlane assignments to the task
      task.setAssignee(swimlane.getAssignee());
      for (ParticipationImpl participant : swimlane.getParticipations()) {
        task.addParticipation(participant.getUserId(), participant.getGroupId(),
          participant.getType());
      }
    }

    execution.initializeAssignments(taskDefinition, task);
    
    HistoryEvent.fire(new TaskActivityStart(task), execution);

    execution.waitForSignal();
  }
  
  public void signal(ActivityExecution execution, String signalName, Map<String, ?> parameters)
    throws Exception {
    signal((ExecutionImpl)execution, signalName, parameters);
  }

  public void signal(ExecutionImpl execution, String signalName, Map<String, ?> parameters)
    throws Exception {
    ActivityImpl activity = execution.getActivity();
    
    if (parameters!=null) {
      execution.setVariables(parameters);
    }
    
    execution.fire(signalName, activity);

    DbSession taskDbSession = EnvironmentImpl.getFromCurrent(DbSession.class);
    TaskImpl task = taskDbSession.findTaskByExecution(execution);
    if (task!=null) {
      task.setSignalling(false);
    }
    
    Transition transition = null;
    List<? extends Transition> outgoingTransitions = activity.getOutgoingTransitions();
    
    if (outgoingTransitions!=null && !outgoingTransitions.isEmpty()) {
      // Lookup the outgoing transition
      boolean noOutcomeSpecified = TaskConstants.NO_TASK_OUTCOME_SPECIFIED.equals(signalName);
      if (noOutcomeSpecified && activity.findOutgoingTransition(signalName) == null) {
        // When no specific outcome was specified, the unnamed transition
        // is looked up (name is null). If a null outcome was specifically
        // used, then the else clause will be used (but the result is the same)
        // Note: the second part of the if clause is to avoid the situation
        // where the user would have chosen the same name as the constant
        transition = activity.findOutgoingTransition(null);
      }
      else {
        transition = activity.findOutgoingTransition(signalName);
      }
      
      // If no transition has been found, we check if we have a special case
      // in which we can still deduce the outgoing transition
      if (transition==null) {
        // no unnamed transition found
        if (signalName == null) {
          // null was explicitly given as outcome
          throw new JbpmException("No unnamed transitions were found for the task '"
            + getTaskDefinition().getName() + "'");
        }
        else if (noOutcomeSpecified) {
          // Special case: complete(id)
          if (outgoingTransitions.size() == 1) { // If only 1 transition, take that one
            transition = outgoingTransitions.get(0);
          }
          else {
            throw new JbpmException("No unnamed transitions were found for the task '"
              + getTaskDefinition().getName() + "'");
          }
        }
        else {
          // Likely a programmatic error.
          throw new JbpmException("No transition named '" + signalName + "' was found.");
        }
      }
      
      if (task != null && !task.isCompleted()) {
        // task should be skipped since it is not completed yet !!!
        task.skip(transition.getName());
      }
      
      if (transition!=null) {
        execution.take(transition);
      }
    }
  }
  
  public TaskDefinitionImpl getTaskDefinition() {
    return taskDefinition;
  }
  public void setTaskDefinition(TaskDefinitionImpl taskDefinition) {
    this.taskDefinition = taskDefinition;
  }
}
