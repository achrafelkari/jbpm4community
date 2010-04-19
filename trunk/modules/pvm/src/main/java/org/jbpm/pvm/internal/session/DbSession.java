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
package org.jbpm.pvm.internal.session;

import java.util.List;

import org.jbpm.api.Execution;
import org.jbpm.api.history.HistoryComment;
import org.jbpm.pvm.internal.client.ClientExecution;
import org.jbpm.pvm.internal.client.ClientProcessDefinition;
import org.jbpm.pvm.internal.job.JobImpl;
import org.jbpm.pvm.internal.job.StartProcessTimer;
import org.jbpm.pvm.internal.model.ExecutionImpl;
import org.jbpm.pvm.internal.model.ScopeInstanceImpl;
import org.jbpm.pvm.internal.query.DeploymentQueryImpl;
import org.jbpm.pvm.internal.query.HistoryActivityInstanceQueryImpl;
import org.jbpm.pvm.internal.query.HistoryDetailQueryImpl;
import org.jbpm.pvm.internal.query.HistoryProcessInstanceQueryImpl;
import org.jbpm.pvm.internal.query.JobQueryImpl;
import org.jbpm.pvm.internal.query.ProcessInstanceQueryImpl;
import org.jbpm.pvm.internal.query.TaskQueryImpl;
import org.jbpm.pvm.internal.task.TaskImpl;
import org.jbpm.pvm.internal.tx.JtaTransaction;
import org.jbpm.pvm.internal.tx.StandardTransaction;
import org.jbpm.pvm.internal.type.Variable;


/**
 * @author Tom Baeyens
 */
public interface DbSession {

  void save(Object entity);
  void update(Object entity);
  
  <T> T get(Class<T> entityClass, Object primaryKey);
  void delete(Object entity);
  void flush();

  /** all deployed process names. */
  List<String> findProcessDefinitionKeys();

  /** latest version of the processDefinition for the given key. */
  ClientProcessDefinition findLatestProcessDefinitionByKey(String processDefinitionKey);

  /** all versions of the processDefinition for the given key. */
  List<ClientProcessDefinition> findProcessDefinitionsByKey(String processDefinitionKey);

  /** the process definition uniquely identified by the given 
   * processDefinition id. */
  ClientProcessDefinition findProcessDefinitionById(String processDefinitionId);

  /** delete process definition */
  void deleteProcessDefinition(String processDefinitionId, boolean deleteProcessInstances, boolean deleteHistory);

  // process execution queries ////////////////////////////////////////////////

  /** create a process instance query */
  ProcessInstanceQueryImpl createProcessInstanceQuery();

  /** create a task query */
  TaskQueryImpl createTaskQuery();
  HistoryProcessInstanceQueryImpl createHistoryProcessInstanceQuery();
  HistoryActivityInstanceQueryImpl createHistoryActivityInstanceQuery();
  HistoryDetailQueryImpl createHistoryDetailQuery();
  JobQueryImpl createJobQuery();
  DeploymentQueryImpl createDeploymentQuery();

  List<HistoryComment> findCommentsByTaskId(String taskId);

  /** the execution uniquely identified by the given executionKey. 
   * this method doesn't 'see' suspended executions. */ 
  ClientExecution findExecutionById(String executionId);

  /** the process instance uniquely identified by the given executionKey. */ 
  ClientExecution findProcessInstanceById(String processInstanceId);
  
  List<String> findProcessInstanceIds(String processDefinitionId);
  
  /** deletes the history information for all process instances for 
   * the given process definition */
  void deleteProcessDefinitionHistory(String processDefinitionId);

  /** delete the process instance and optionally deletes the history. */
  void deleteProcessInstance(String processInstanceId, boolean deleteHistory);
  
  void cascadeExecutionSuspend(ExecutionImpl execution);

  void cascadeExecutionResume(ExecutionImpl execution);
  
  // task methods /////////////////////////////////////////////////////////////
  
  TaskImpl createTask();

  TaskImpl findTaskByExecution(Execution execution);

  // job methods //////////////////////////////////////////////////////////////

  /** the first job to finish among eligible and non-locked jobs or null if none */
  public JobImpl<?> findFirstAcquirableJob();

  /** the list of jobs of the process instance that mustn't be concurrent */
  public List<JobImpl<?>> findExclusiveJobs(Execution processInstance);

  /** the first job to finish among non-owned jobs or null if none */
  public JobImpl<?> findFirstDueJob();
  
  /** returns a list of start process timers for the given process definition */
  public List<StartProcessTimer> findStartProcessTimers(String processDefinitionId);
}
