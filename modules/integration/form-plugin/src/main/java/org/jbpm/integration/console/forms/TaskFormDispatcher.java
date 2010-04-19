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
package org.jbpm.integration.console.forms;

import java.io.InputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.activation.DataHandler;

import org.jboss.bpm.console.server.plugin.FormAuthorityRef;
import org.jboss.bpm.console.server.plugin.FormDispatcherPlugin;
import org.jbpm.api.*;
import org.jbpm.api.task.Task;

/**
 * Processes form data to complete tasks.
 *
 * @author Heiko.Braun <heiko.braun@jboss.com>
 */
public class TaskFormDispatcher extends AbstractFormDispatcher implements FormDispatcherPlugin {
  
  public TaskFormDispatcher() {
    super();
  }

  public URL getDispatchUrl(FormAuthorityRef ref) {
    if (!taskHasForm(ref.getReferenceId()))
      return null;

    StringBuilder baseUrl = getBaseUrl();
    baseUrl.append("/form/task/");
    baseUrl.append(ref.getReferenceId());
    baseUrl.append("/render");

    try {
      return new URL(baseUrl.toString());
    } catch (MalformedURLException e) {
      throw new RuntimeException("Failed to resolve task dispatch url", e);
    }
  }

  private boolean taskHasForm(String id) {
    boolean result = false;
    TaskService taskService = processEngine.getTaskService();
    Task task = taskService.getTask(id);
    result = (task.getFormResourceName() != null);
    return result;
  }

  public DataHandler provideForm(FormAuthorityRef ref) {
    TaskService taskService = processEngine.getTaskService();
    ExecutionService executionService = processEngine.getExecutionService();
    RepositoryService repoService = processEngine.getRepositoryService();

    Task task = taskService.getTask(ref.getReferenceId());

    String executionId = task.getExecutionId();
    Execution execution = executionService.findExecutionById(executionId);
    String procInstId = execution.getProcessInstance().getId();
    ProcessDefinition procDef = repoService.createProcessDefinitionQuery()
        .processDefinitionId(execution.getProcessDefinitionId())
        .uniqueResult();

    // check if a template exists
    String name = task.getFormResourceName();
    InputStream template = repoService.getResourceAsStream(procDef.getDeploymentId(), name);

    // merge template with process variables
    if (template == null)
      throw new IllegalArgumentException("Task form resource '" + name + "' doesn't exist.");

    Map<String, Object> processContext = new HashMap<String, Object>();
    ExecutionService execService = processEngine.getExecutionService();
    Set<String> varNames = execService.getVariableNames(procInstId);

    if (varNames != null)
      processContext = execService.getVariables(procInstId, varNames);

    // plugin context
    StringBuilder action = getBaseUrl();
    action.append("/form/task/");
    action.append(ref.getReferenceId());
    action.append("/complete");

    Map<String, Object> renderContext = new HashMap<String, Object>();

    // form directive
    FormDirective formDirective = new FormDirective();
    formDirective.setAction(action.toString());
    renderContext.put(FORM_DIRECTIVE_KEY, formDirective);

    // outcome directive
    OutcomeDirective outcomeDirective = new OutcomeDirective();
    Set<String> outcomes = taskService.getOutcomes(task.getId());
    for (String outcome : outcomes) {
      outcomeDirective.getValues().add(outcome);
    }
    renderContext.put(OUTCOME_DIRECTIVE_NAME, outcomeDirective);

    // global css
    InputStream css = loadCSS(task.getExecutionId());
    if(css!=null)
        renderContext.put("CSS", streamToString(css));

    // process variables
    renderContext.putAll(processContext);

    DataHandler result = processTemplate(name, template, renderContext);
    return result;
  }

  private InputStream loadCSS(String executionId)
  {
    RepositoryService repoService = processEngine.getRepositoryService();
    ExecutionService execService = processEngine.getExecutionService();

    Execution execution = execService.findExecutionById(executionId);
    
    if (execution != null) {
      ProcessDefinition definition = repoService.createProcessDefinitionQuery()
          .processDefinitionId(execution.getProcessDefinitionId()).uniqueResult();
      InputStream in = repoService.getResourceAsStream(definition.getDeploymentId(), PROCESSFORMS_CSS);
      return in;
    }

    return null;
  }
}
