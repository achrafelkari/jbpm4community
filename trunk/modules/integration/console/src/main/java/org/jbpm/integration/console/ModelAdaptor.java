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
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.jboss.bpm.console.client.model.DeploymentRef;
import org.jboss.bpm.console.client.model.HistoryActivityInstanceRef;
import org.jboss.bpm.console.client.model.HistoryProcessInstanceRef;
import org.jboss.bpm.console.client.model.JobRef;
import org.jboss.bpm.console.client.model.ParticipantRef;
import org.jboss.bpm.console.client.model.ProcessDefinitionRef;
import org.jboss.bpm.console.client.model.ProcessInstanceRef;
import org.jboss.bpm.console.client.model.TaskRef;
import org.jboss.bpm.console.client.model.TokenReference;
import org.jbpm.api.Deployment;
import org.jbpm.api.Execution;
import org.jbpm.api.ExecutionService;
import org.jbpm.api.HistoryService;
import org.jbpm.api.ProcessDefinition;
import org.jbpm.api.RepositoryService;
import org.jbpm.api.TaskService;
import org.jbpm.api.history.HistoryActivityInstance;
import org.jbpm.api.history.HistoryProcessInstance;
import org.jbpm.api.job.Job;
import org.jbpm.api.model.Transition;
import org.jbpm.api.task.Participation;
import org.jbpm.api.task.Task;
import org.jbpm.pvm.internal.model.ExecutionImpl;
import org.jbpm.pvm.internal.task.TaskImpl;

/**
 * @author Heiko.Braun <heiko.braun@jboss.com>
 * @author jbarrez
 */
public class ModelAdaptor {

  public static ProcessDefinitionRef adoptDefinition(ProcessDefinition processDefinition) {
    ProcessDefinitionRef def = new ProcessDefinitionRef();
    def.setId(processDefinition.getId());
    def.setName(processDefinition.getName());
    def.setVersion(processDefinition.getVersion());
    def.setKey(processDefinition.getKey());
    def.setSuspended(processDefinition.isSuspended());
    def.setDeploymentId(processDefinition.getDeploymentId());
    
    // TODO: Are these needed?
    // ProcessDefinitionImpl cast = (ProcessDefinitionImpl) processDefinition;
    // def.setDescription(processDefinition.getDescription());
    // def.setPackageName(cast.getPackageName());
    
    return def;
  }

  public static ProcessInstanceRef adoptExecution(Execution execution) {
    ProcessInstanceRef ref = new ProcessInstanceRef();
    ref.setId(execution.getId());
    ref.setKey(execution.getKey());
    ref.setDefinitionId(execution.getProcessDefinitionId());
    
    // Start date is only available through historyService
    HistoryService historyService = ProcessEngineUtil.retrieveProcessEngine().getHistoryService();
    Date startDate = historyService.createHistoryProcessInstanceQuery()
                                   .processInstanceId(execution.getId())
                                   .uniqueResult().getStartTime();
    ref.setStartDate(startDate);

    Execution topLevelExecution = execution.getProcessInstance();
    TokenReference tok = execution2TokenReference(topLevelExecution);
      

    Collection<ExecutionImpl> childExecutions = (Collection) topLevelExecution.getExecutions();
    if (childExecutions != null) {
      for (ExecutionImpl childExecution : childExecutions) {
        // set process definition on child execution from topLevelExecution
        childExecution.setProcessDefinition(((ExecutionImpl)topLevelExecution).getProcessDefinition());
        
        TokenReference childTok = execution2TokenReference(childExecution);
        tok.getChildren().add(childTok);
      }
    }

    ref.setRootToken(tok);

    return ref;
  }

  private static TokenReference execution2TokenReference(Execution execution) {
    String executionId = execution.getId();

    TokenReference tok = new TokenReference();
    tok.setName(execution.getName());
    tok.setId(executionId);
    
    // mark execution as signalable if it is in wait state and it is active
    if (((ExecutionImpl) execution).isActive() && "state".equals(((ExecutionImpl) execution).getActivity().getType())) {
      tok.setCanBeSignaled(true);
    }


    // Only if the set of current activities has one element, we can
    // set the current node name (otherwise it's a parent execution)
    Set<String> currentActivities = execution.findActiveActivityNames();
    if (currentActivities.size() == 1) { 
      tok.setCurrentNodeName(currentActivities.iterator().next());
    }
    else if (currentActivities.size() > 1){
      StringBuilder strb = new StringBuilder();
      for (String activeActivity : currentActivities) {
        strb.append(activeActivity + ",");
      }
      tok.setCurrentNodeName(strb.deleteCharAt(strb.length() - 1).toString());      
    } else {
      tok.setCurrentNodeName(executionId);
    }
           
    if (((ExecutionImpl) execution).getActivity() !=null && "state".equals(((ExecutionImpl) execution).getActivity().getType())) {
      // transitions
      List<String> availableSignals = new ArrayList<String>();
      // fetch outgoing transitions for state activity only, it is required to allow proper
      // signaling to be made from console - it must send real signal name (outgoing transition)
      // because there is not signal name check when signaling and if wrong name was given caller will not get any error
      // and execution will remain in wait state
      ExecutionImpl openTopLevelExecution = (ExecutionImpl) execution;
      
      List<Transition> outTransitions = openTopLevelExecution.getActivity().getOutgoingTransitions();
      if (outTransitions != null) {
        for (Transition t : outTransitions) {
          // if name is not given use 'default transition' as name to be consistent with ProcessFacade check
          String transitionName = t.getName() != null ? t.getName() : "default transition";
          availableSignals.add(transitionName);
        }
      }
      tok.setAvailableSignals(availableSignals);
    }
    return tok;
  }

  public static TaskRef adoptTask(Task jbpmTask) {
    TaskRef task = new TaskRef();
    
    task.setId(((TaskImpl) jbpmTask).getDbid());
    task.setName(jbpmTask.getName());
    task.setDescription(jbpmTask.getDescription());
    task.setAssignee(jbpmTask.getAssignee());
    
    task.setPriority(jbpmTask.getPriority());
    task.setDueDate(jbpmTask.getDuedate());
    task.setCreateDate(jbpmTask.getCreateTime());
    
    ExecutionService executionService = ProcessEngineUtil.retrieveProcessEngine().getExecutionService();
    
    String executionId = jbpmTask.getExecutionId();
    if (executionId != null) { // otherwise we're dealing with a standalone task
      Execution execution = executionService.findExecutionById(executionId);
      if (execution != null) {
        Execution pi = execution.getProcessInstance();
        task.setProcessInstanceId(pi.getId());
        task.setProcessId(pi.getProcessDefinitionId());
      }
    }
    
    // cast
    //TaskImpl cast = ((TaskImpl) jbpmTask);
    //task.setSignalling(cast.isSignalling()); // TODO: Still needed?

    //ExecutionImpl execution = cast.getProcessInstance();
    //task.setProcessInstanceId(cast.getProcessInstance().getId());

    // participations
    TaskService taskService = ProcessEngineUtil.retrieveProcessEngine().getTaskService();
    List<Participation> participations = taskService.getTaskParticipations(jbpmTask.getId());
    for (Participation participation : participations) {
      
      if (participation.getType().equals(Participation.CANDIDATE)) {
        if (participation.getGroupId() != null) {
          ParticipantRef participant = new ParticipantRef("candidate", participation.getGroupId());
          participant.setGroup(true);
          task.getParticipantGroups().add(participant);
        } else if (participation.getUserId() != null) {
          ParticipantRef participant = new ParticipantRef("candidate", participation.getUserId());
          task.getParticipantUsers().add(participant);
        } else {
          throw new IllegalArgumentException("Participation doesn't have user or group: " + participation);
        }
      } else {
        throw new IllegalArgumentException("Unknown participation type: " + participation.getType());
      }

    }

    // task formResourceName url
    String url = jbpmTask.getFormResourceName() != null ? jbpmTask.getFormResourceName() : "";
    task.setUrl(url);

    return task;
  }

  public static DeploymentRef adoptDeployment(Deployment deployment) {
    DeploymentRef dRef = new DeploymentRef();
    dRef.setId(deployment.getId());
    dRef.setSuspended(deployment.getState().equals(Deployment.STATE_SUSPENDED));
    dRef.setTimestamp(deployment.getTimestamp());
    
    RepositoryService repositoryService = ProcessEngineUtil.retrieveProcessEngine().getRepositoryService();
    Set<String> resourceNames = repositoryService.getResourceNames(deployment.getId());
    dRef.getResourceNames().addAll(resourceNames);

    String name = deployment.getName();
    // strip path info
    if (name.indexOf("/") != -1) {
      name = name.substring(name.lastIndexOf("/") + 1, name.length());
    }
    dRef.setName(name);

    return dRef;
  }

  public static JobRef adoptJob(Job job) {
    JobRef jobRef = new JobRef();
    jobRef.setId(String.valueOf(job.getId()));
    
    if (job.getDuedate() != null) {
      jobRef.setTimestamp(job.getDuedate().getTime());
    }
    if (job.getException() != null) {
      jobRef.setErrMsg(job.getException());
    }

    return jobRef;
  }
  
  public static HistoryActivityInstanceRef adoptHistoryActivity(HistoryActivityInstance history) {
    HistoryActivityInstanceRef historyRef = new HistoryActivityInstanceRef();
    
    historyRef.setActivityName(history.getActivityName());
    historyRef.setDuration(history.getDuration());
    historyRef.setEndTime(history.getEndTime());
    historyRef.setExecutionId(history.getExecutionId());
    historyRef.setStartTime(history.getStartTime());

    return historyRef;
  }

  public static HistoryProcessInstanceRef adoptHistoryProcessInstance(HistoryProcessInstance history) {
    
    HistoryProcessInstanceRef historyRef = new HistoryProcessInstanceRef();
    
    historyRef.setProcessDefinitionId(history.getProcessDefinitionId());
    historyRef.setProcessInstanceId(history.getProcessInstanceId());
    historyRef.setEndTime(history.getEndTime());
    historyRef.setEndActivityName(history.getEndActivityName());
    historyRef.setStartTime(history.getStartTime());
    historyRef.setEndActivityName(history.getEndActivityName());
    historyRef.setDuration(history.getDuration());
    historyRef.setState(history.getState());
    historyRef.setKey(history.getKey());
    
    return historyRef;
  }
}
