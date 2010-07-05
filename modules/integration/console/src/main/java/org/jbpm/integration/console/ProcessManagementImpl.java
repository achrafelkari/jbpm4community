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

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jboss.bpm.console.client.model.ProcessDefinitionRef;
import org.jboss.bpm.console.client.model.ProcessInstanceRef;
import org.jboss.bpm.console.server.integration.ProcessManagement;
import org.jbpm.api.Execution;
import org.jbpm.api.ExecutionService;
import org.jbpm.api.ProcessDefinition;
import org.jbpm.api.ProcessDefinitionQuery;
import org.jbpm.api.ProcessInstance;
import org.jbpm.api.RepositoryService;
import org.jbpm.pvm.internal.model.ExecutionImpl;
import org.jbpm.pvm.internal.model.ProcessDefinitionImpl;

/**
 * @author Heiko.Braun <heiko.braun@jboss.com>
 * @author jbarrez
 */
class ProcessManagementImpl extends JBPMIntegration implements ProcessManagement {
  
  
  public ProcessManagementImpl() {
    super();
  }

  public List<ProcessDefinitionRef> getProcessDefinitions() {
    RepositoryService repositoryService = this.processEngine.getRepositoryService();
    List<ProcessDefinitionRef> results = new ArrayList<ProcessDefinitionRef>();

    // active process definitions
    List<ProcessDefinition> activePds = 
      repositoryService.createProcessDefinitionQuery()
                       .orderAsc(ProcessDefinitionQuery.PROPERTY_NAME)
                       .list();

    for (ProcessDefinition processDefinition : activePds) {
      results.add(ModelAdaptor.adoptDefinition(processDefinition));
    }

    return results;
  }

  public ProcessDefinitionRef getProcessDefinition(String procDefId) {
    RepositoryService repositoryService = this.processEngine.getRepositoryService();
    ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().processDefinitionId(procDefId).uniqueResult();
    return ModelAdaptor.adoptDefinition(processDefinition);
  }

  public List<ProcessDefinitionRef> removeProcessDefinition(String procDefId) {
    RepositoryService repositoryService = this.processEngine.getRepositoryService();
    ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().processDefinitionId(procDefId).uniqueResult();
    if (processDefinition != null) {
      // Todo: this deletes a complete deployment. Currently, there is no way of deleting a procDef only
      repositoryService.deleteDeploymentCascade(processDefinition.getDeploymentId());
    }
    return getProcessDefinitions();
  }

  public List<ProcessInstanceRef> getProcessInstances(String procDefId) {
    ExecutionService execService = this.processEngine.getExecutionService();
    List<ProcessInstance> processInstances = execService.createProcessInstanceQuery()
                                                        .processDefinitionId(procDefId)
                                                        .list();
    // must fetch process definition first to be able to get information about activities
    RepositoryService repositoryService = this.processEngine.getRepositoryService();
    ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().processDefinitionId(procDefId).uniqueResult();
    

    List<ProcessInstanceRef> results = adoptProcessInstances(processInstances, processDefinition);

    return results;
  }

  private List<ProcessInstanceRef> adoptProcessInstances(List<ProcessInstance> processInstances, ProcessDefinition processDefinition) {
    List<ProcessInstanceRef> results = new ArrayList<ProcessInstanceRef>();
    for (Execution processInstance : processInstances) {
      if (processInstance.isEnded()) {
        continue; // JBPM-2055: Execution is already ended. Should not show up in query
      }

      if (processInstance.getIsProcessInstance()) { // parent execution
        ((ExecutionImpl) processInstance).setProcessDefinition((ProcessDefinitionImpl) processDefinition);
        results.add(ModelAdaptor.adoptExecution(processInstance));
      }
    }
    return results;
  }

  public ProcessInstanceRef getProcessInstance(String instanceId) {
    ExecutionService execService = this.processEngine.getExecutionService();
    ProcessInstance processInstance = execService.createProcessInstanceQuery()
                                                 .processInstanceId(instanceId)
                                                 .uniqueResult();
    return ModelAdaptor.adoptExecution(processInstance);
  }

  public Map<String, Object> getInstanceData(String instanceId) {
    Map<String, Object> data = new HashMap<String, Object>();
    ExecutionService execService = this.processEngine.getExecutionService();
    Set<String> keys = execService.getVariableNames(instanceId);
    data = execService.getVariables(instanceId, keys);
    return data;
  }

  public void setInstanceData(String instanceId, Map<String, Object> data) {
    throw new RuntimeException("Not implemented");
  }

  public ProcessInstanceRef newInstance(String definitionId) {
    ExecutionService execService = this.processEngine.getExecutionService();
    Execution exec = execService.startProcessInstanceById(definitionId);
    return ModelAdaptor.adoptExecution(exec);
  }

  public ProcessInstanceRef newInstance(String definitionId, Map<String, Object> processVars) {
    ExecutionService execService = this.processEngine.getExecutionService();
    Execution exec = execService.startProcessInstanceById(definitionId, processVars);
    return ModelAdaptor.adoptExecution(exec);
  }

  public void endInstance(String instanceId, ProcessInstanceRef.RESULT result) {
    ExecutionService execService = this.processEngine.getExecutionService();
    Execution exec = execService.findExecutionById(instanceId);
    if (null == exec)
      throw new IllegalArgumentException("No such execution with id " + instanceId);

    ProcessInstanceRef.RESULT actualResult = result != null ? result : ProcessInstanceRef.RESULT.COMPLETED;
    execService.endProcessInstance(instanceId, actualResult.toString());
  }

  public void deleteInstance(String instanceId) {
    ExecutionService execService = this.processEngine.getExecutionService();
    Execution exec = execService.findExecutionById(instanceId);
    
    if (null == exec)
      throw new IllegalArgumentException("No such execution with id " + instanceId);

    execService.deleteProcessInstance(instanceId);
  }

  public void setProcessState(String executionId, ProcessInstanceRef.STATE nextState) {
    throw new RuntimeException("Not implemented");
  }

  public void signalExecution(String executionId, String signal) {
    ExecutionService execService = this.processEngine.getExecutionService();

    if (null == signal)
      execService.signalExecutionById(executionId);
    else
      execService.signalExecutionById(executionId, signal);
  }

  public void deploy(String fileName, String contentType, InputStream deployment) {
    throw new RuntimeException("Not implemented");
  }
    
}
