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

import org.jbpm.api.JbpmException;
import org.jbpm.api.ProcessInstance;
import org.jbpm.api.cmd.Environment;
import org.jbpm.pvm.internal.client.ClientProcessDefinition;
import org.jbpm.pvm.internal.client.ClientProcessInstance;
import org.jbpm.pvm.internal.session.RepositorySession;

/**
 * @author Tom Baeyens
 */
public class StartProcessInstanceInLatestCmd extends VariablesCmd<ProcessInstance> {

  private static final long serialVersionUID = 1L;

  protected String processDefinitionKey;
  protected String executionKey;

  public StartProcessInstanceInLatestCmd(String processDefinitionKey, Map<String, ?> variables, String executionKey) {
    if (processDefinitionKey==null) {
      throw new JbpmException("processDefinitionKey is null");
    }
    this.processDefinitionKey = processDefinitionKey;
    this.executionKey = executionKey;
    setVariables(variables);
  }
  
  public ProcessInstance execute(Environment environment) throws Exception {
    ClientProcessDefinition processDefinition = null;
    
    RepositorySession repositorySession = environment.get(RepositorySession.class);
    processDefinition = repositorySession.findProcessDefinitionByKey(processDefinitionKey);
    if (processDefinition==null) {
      throw new JbpmException("no process definition with key '"+processDefinitionKey+"'");
    }
    
    ClientProcessInstance processInstance = processDefinition.createProcessInstance(executionKey);
    processInstance.setVariables(variables);
    processInstance.start();
    
    return processInstance;
  }

  public String getExecutionKey() {
    return executionKey;
  }
  public void setExecutionKey(String executionKey) {
    this.executionKey = executionKey;
  }
}
