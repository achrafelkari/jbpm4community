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

import java.util.Map;
import java.util.Set;


/** manages runtime process executions.
 *
 * @author Tom Baeyens
 */
public interface ExecutionService {

  /** starts a new process instance for the ProcessDefinition with the given processDefinitionDbid.
   * @param processDefinitionId the {@link ProcessDefinition#getId() unique id} of the process definition. */
  ProcessInstance startProcessInstanceById(String processDefinitionId);

  /** starts a new process instance for the ProcessDefinition with the given processDefinitionDbid. 
   * @param processDefinitionId the {@link ProcessDefinition#getId() unique id} of the process definition.
   * @param processInstanceKey is a user provided reference for the new process instance that must be unique over all 
   *    process definition versions with the same name. */
  ProcessInstance startProcessInstanceById(String processDefinitionId, String processInstanceKey);

  /** starts a new process instance for the ProcessDefinition with the given processDefinitionDbid. 
   * @param processDefinitionId the {@link ProcessDefinition#getId() unique id} of the process definition.
   * @param variables are the initial values of the process variables that will be set before the execution starts. */
  ProcessInstance startProcessInstanceById(String processDefinitionId, Map<String, ?> variables);

  /** starts a new process instance for the ProcessDefinition with the given processDefinitionDbid.
   * @param processDefinitionId the {@link ProcessDefinition#getId() unique id} of the process definition.
   * @param variables are the initial values of the process variables that will be set before the execution starts.
   * @param processInstanceKey is a user provided reference for the new process instance that must be unique over all 
   *    process versions with the same name. */
  ProcessInstance startProcessInstanceById(String processDefinitionId, Map<String, ?> variables, String processInstanceKey);

  /** starts a new process instance in the latest version of the given process definition.
   * @param processDefinitionKey is the key of the process definition for which the latest version will be taken. */
  ProcessInstance startProcessInstanceByKey(String processDefinitionKey);

  /** starts a new process instance in the latest version of the given processDefinitionName.
   * @param processDefinitionKey is the key of the process definition
   *   for which the latest version will be taken.
   * @param processInstanceKey is a user provided reference for the new process instance
   *   that must be unique over all process versions with the same name. */
  ProcessInstance startProcessInstanceByKey(String processDefinitionKey, String processInstanceKey);

  /** starts a new process instance in the latest version of the given processDefinitionName.
   * @param processDefinitionKey is the key of the process definition
   *   for which the latest version will be taken.
   * @param variables are the initial values of the process variables that
   *   will be set before the execution starts (read: before the initial
   *   activity is executed). */
  ProcessInstance startProcessInstanceByKey(String processDefinitionKey, Map<String, ?> variables);

  /** starts a new process instance in the latest version of the given processDefinitionName.
   * @param processDefinitionKey is the key of the process definition for which the latest version will be taken.
   * @param variables are the initial values of the process variables that will be set before the execution starts.
   * @param processInstanceKey is a user provided reference for the new execution that must be unique over all
   *    process versions with the same name. */
  ProcessInstance startProcessInstanceByKey(String processDefinitionKey, Map<String, ?> variables, String processInstanceKey);

  /** the path of execution that is uniquely defined by the execution id. */
  Execution findExecutionById(String executionId);

  /** the process instance that is uniquely defined by the process execution id. */
  ProcessInstance findProcessInstanceById(String processInstanceId);

  /** provides an external trigger to an execution. */
  ProcessInstance signalExecutionById(String executionId);

  /** provides a named external trigger to an execution. */
  ProcessInstance signalExecutionById(String executionId, String signalName);

  /** provides a named external trigger to an execution with parameters. */
  ProcessInstance signalExecutionById(String executionId, String signalName, Map<String, ?> parameters);

  /** provides a external trigger to an execution with parameters. */
  ProcessInstance signalExecutionById(String executionId, Map<String, ?> parameters);


  /** search for process instances with criteria.
   * be aware that this query only sees ongoing process instances.
   * refer to {@link HistoryService#createHistoryTaskQuery()} for
   * queries that include finished process instances. */
  ProcessInstanceQuery createProcessInstanceQuery();

  /** creates or overwrites a variable value in the referenced execution */
  void setVariable(String executionId, String name, Object value);

  /** creates or overwrites variable values in the referenced execution */
  void setVariables(String executionId, Map<String, ?> variables);
  
  /** creates a variable value in the referenced execution. optionally enables variable history tracking. */
  void createVariable(String executionId, String name, Object value, boolean historyEnabled);
  
  /** creates variable values in the referenced execution. optionally enables variable history tracking. */
  void createVariables(String executionId, Map<String, ?> variables, boolean historyEnabled);

  /** retrieves a variable */
  Object getVariable(String executionId, String variableName);

  /** all the variables visible in the given execution scope */
  Set<String> getVariableNames(String executionId);

  /** retrieves a map of variables */
  Map<String, Object> getVariables(String executionId, Set<String> variableNames);
  
  /** end a process instance */
  void endProcessInstance(String processInstanceId, String state);

  /** delete a process instance.  The history information will still be in the database. 
   * @throws JbpmException if the given processInstanceId doesn't exist*/
  void deleteProcessInstance(String processInstanceId);

  /** delete a process instance, including the history information.
   *  @throws JbpmException if the given processInstanceId doesn't exist */
  void deleteProcessInstanceCascade(String processInstanceId);
}
