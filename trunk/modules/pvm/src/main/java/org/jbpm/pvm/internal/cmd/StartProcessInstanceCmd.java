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
package org.jbpm.pvm.internal.cmd;

import java.util.Map;

import org.hibernate.Session;
import org.jbpm.api.JbpmException;
import org.jbpm.api.ProcessInstance;
import org.jbpm.api.cmd.Environment;
import org.jbpm.pvm.internal.client.ClientProcessDefinition;
import org.jbpm.pvm.internal.client.ClientProcessInstance;
import org.jbpm.pvm.internal.session.RepositorySession;

/**
 * @author Tom Baeyens
 */
public class StartProcessInstanceCmd extends VariablesCmd<ProcessInstance> {

  private static final long serialVersionUID = 1L;
  
  protected String processDefinitionId;
  protected String executionKey;

  public StartProcessInstanceCmd(String processDefinitionId, Map<String, ?> variables, String executionKey) {
    this.processDefinitionId = processDefinitionId;
    this.variables = variables;
    this.executionKey = executionKey;
  }

  public ProcessInstance execute(Environment environment) throws Exception {
    RepositorySession repositorySession = environment.get(RepositorySession.class);

    ClientProcessDefinition processDefinition = repositorySession.findProcessDefinitionById(processDefinitionId);
    if (processDefinition==null) {
      throw new JbpmException("no process definition with id '"+processDefinitionId+"'");
    }
    
    ClientProcessInstance processInstance = processDefinition.createProcessInstance(executionKey);
    processInstance.setVariables(variables);
    processInstance.start();

    if (!processInstance.isEnded()) {
      Session session = environment.get(Session.class);
      session.save(processInstance);
    }

    return processInstance;
  }

  public String getProcessDefinitionKey() {
    return processDefinitionId;
  }
  public void setProcessDefinitionKey(String processDefinitionKey) {
    this.processDefinitionId = processDefinitionKey;
  }
  public String getExecutionKey() {
    return executionKey;
  }
  public void setExecutionKey(String executionKey) {
    this.executionKey = executionKey;
  }
}
