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
import java.util.Map;
import java.util.Set;

import org.jbpm.api.Execution;
import org.jbpm.api.ExecutionService;
import org.jbpm.api.ProcessInstance;
import org.jbpm.api.ProcessInstanceQuery;
import org.jbpm.pvm.internal.cmd.CreateExecutionVariablesCmd;
import org.jbpm.pvm.internal.cmd.CreateProcessInstanceQueryCmd;
import org.jbpm.pvm.internal.cmd.DeleteProcessInstance;
import org.jbpm.pvm.internal.cmd.EndProcessInstance;
import org.jbpm.pvm.internal.cmd.FindExecutionCmd;
import org.jbpm.pvm.internal.cmd.GetExecutionVariableNamesCmd;
import org.jbpm.pvm.internal.cmd.GetExecutionVariablesCmd;
import org.jbpm.pvm.internal.cmd.SetExecutionVariablesCmd;
import org.jbpm.pvm.internal.cmd.SignalCmd;
import org.jbpm.pvm.internal.cmd.StartProcessInstanceCmd;
import org.jbpm.pvm.internal.cmd.StartProcessInstanceInLatestCmd;
import org.jbpm.pvm.internal.query.ProcessInstanceQueryImpl;

/**
 * @author Tom Baeyens
 */
public class ExecutionServiceImpl extends AbstractServiceImpl implements ExecutionService {

  public ProcessInstance startProcessInstanceById(String processDefinitionId){
    return commandService.execute(new StartProcessInstanceCmd(processDefinitionId, null, null));
  }

  public ProcessInstance startProcessInstanceById(String processDefinitionId, String executionKey) {
    return commandService.execute(new StartProcessInstanceCmd(processDefinitionId, null, executionKey));
  }

  public ProcessInstance startProcessInstanceById(String processDefinitionId, Map<String, ?> variables){
    return commandService.execute(new StartProcessInstanceCmd(processDefinitionId, variables, null));
  }

  public ProcessInstance startProcessInstanceById(String processDefinitionId, Map<String, ?> variables, String executionKey){
    return commandService.execute(new StartProcessInstanceCmd(processDefinitionId, variables, executionKey));
  }
  
  public ProcessInstance startProcessInstanceByKey(String processDefinitionKey) {
    return commandService.execute(new StartProcessInstanceInLatestCmd(processDefinitionKey, null, null));
  }
  
  public ProcessInstance startProcessInstanceByKey(String processDefinitionKey, Map<String, ?> variables){
    return commandService.execute(new StartProcessInstanceInLatestCmd(processDefinitionKey, variables, null));
  }
  
  public ProcessInstance startProcessInstanceByKey(String processDefinitionKey, String executionKey) {
    return commandService.execute(new StartProcessInstanceInLatestCmd(processDefinitionKey, null, executionKey));
  }

  public ProcessInstance startProcessInstanceByKey(String processDefinitionKey, Map<String, ?> variables, String executionKey){
    return commandService.execute(new StartProcessInstanceInLatestCmd(processDefinitionKey, variables, executionKey));
  }

  
  
  public ProcessInstance signalExecutionById(String executionId) {
    return commandService.execute(new SignalCmd(executionId, null, null));
  }

  public ProcessInstance signalExecutionById(String executionId, String signalName) {
    return commandService.execute(new SignalCmd(executionId, signalName, null));
  }

  public ProcessInstance signalExecutionById(String executionId, String signalName, Map<String, ?> parameters) {
    return commandService.execute(new SignalCmd(executionId, signalName, parameters));
  }

  public ProcessInstance signalExecutionById(String executionId, Map<String, ?> parameters) {
    return commandService.execute(new SignalCmd(executionId, null, parameters));
  }

 
  public Execution findExecutionById(String executionId) {
    return commandService.execute(new FindExecutionCmd(executionId));
  }
  
  public ProcessInstance findProcessInstanceById(String executionId) {
    return (ProcessInstance) commandService.execute(new FindExecutionCmd(executionId));
  }
  

  public ProcessInstanceQuery createProcessInstanceQuery() {
    ProcessInstanceQueryImpl query = commandService.execute(new CreateProcessInstanceQueryCmd());
    query.setCommandService(commandService);
    return query;
  }

  public void endProcessInstance(String processInstanceId, String state) {
    commandService.execute(new EndProcessInstance(processInstanceId, state));
  }

  public void deleteProcessInstance(String processInstanceId) {
    commandService.execute(new DeleteProcessInstance(processInstanceId));
  }
  
  public void deleteProcessInstanceCascade(String processInstanceId) {
    commandService.execute(new DeleteProcessInstance(processInstanceId, true));
  }

  public Object getVariable(String executionId, String variableName) {
    Set<String> variableNames = new HashSet<String>();
    variableNames.add(variableName);
    Map<String, Object> variables = commandService.execute(new GetExecutionVariablesCmd(executionId, variableNames));
    return variables.get(variableName);
  }

  public Set<String> getVariableNames(String executionId) {
    return commandService.execute(new GetExecutionVariableNamesCmd(executionId));
  }

  public Map<String, Object> getVariables(String executionId, Set<String> variableNames) {
    return commandService.execute(new GetExecutionVariablesCmd(executionId, variableNames));
  }

  public void setVariable(String executionId, String name, Object value) {
    SetExecutionVariablesCmd cmd = new SetExecutionVariablesCmd(executionId);
    cmd.addVariable(name, value);
    commandService.execute(cmd);
  }

  public void setVariables(String executionId, Map<String, ?> variables) {
    SetExecutionVariablesCmd cmd = new SetExecutionVariablesCmd(executionId);
    cmd.setVariables(variables);
    commandService.execute(cmd);
  }
  
  public void createVariable(String executionId, String name, Object value, boolean historyEnabled) {
    CreateExecutionVariablesCmd cmd = new CreateExecutionVariablesCmd(executionId, historyEnabled);
    cmd.addVariable(name, value);
    commandService.execute(cmd);
  }

  public void createVariables(String executionId, Map<String, ?> variables, boolean historyEnabled) {
    CreateExecutionVariablesCmd cmd = new CreateExecutionVariablesCmd(executionId, historyEnabled);
    cmd.setVariables(variables);
    commandService.execute(cmd);
  }
}
