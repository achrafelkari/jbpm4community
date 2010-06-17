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

import org.jbpm.api.JbpmException;
import org.jbpm.api.cmd.Environment;
import org.jbpm.pvm.internal.client.ClientExecution;

/**
 * @author Tom Baeyens
 */
public class SetExecutionVariablesCmd extends VariablesCmd<Void> {
  
  private static final long serialVersionUID = 1L;
  
  protected String executionId = null;
  
  public SetExecutionVariablesCmd(String executionId) {
    if (executionId==null) {
      throw new JbpmException("executionId is null"); 
    }
    this.executionId = executionId;
  }

  public Void execute(Environment environment) throws Exception {
    ClientExecution execution = getExecution(environment, executionId);
    execution.setVariables(variables);
    return null;
  }

  public String getExecutionId() {
    return executionId;
  }
}
